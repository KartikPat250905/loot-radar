const admin = require('firebase-admin');
const axios = require('axios');

if (!process.env.SERVICE_ACCOUNT_KEY_JSON) {
  throw new Error('SERVICE_ACCOUNT_KEY_JSON is not set.');
}

admin.initializeApp({
  credential: admin.credential.cert(JSON.parse(process.env.SERVICE_ACCOUNT_KEY_JSON))
});

const db = admin.firestore();
const messaging = admin.messaging();
const GAME_API_URL = 'https://www.gamerpower.com/api/giveaways';
const CHUNK_SIZE = 30;

const PLATFORM_MAP = {
  'epic games store': 'epic-games-store',
  'nintendo switch': 'switch',
  'playstation 4': 'ps4',
  'playstation 5': 'ps5',
  'xbox one': 'xbox-one',
  'xbox series x|s': 'xbox-series-xs',
  'xbox series xs': 'xbox-series-xs',
  'xbox 360': 'xbox-360',
  'battle.net': 'battlenet'
};

function normalizePlatforms(platformString) {
  return platformString.split(',').map(p => {
    const key = p.trim().toLowerCase();
    return PLATFORM_MAP[key] || key;
  });
}

async function importDeals() {
  const { data: dealsFromApi } = await axios.get(GAME_API_URL);
  if (!Array.isArray(dealsFromApi) || dealsFromApi.length === 0) return [];

  const dealsCollection = db.collection('deals');
  const apiDealIds = dealsFromApi.map(d => String(d.id));

  // Chunk IDs and query existing ones in parallel
  const chunks = [];
  for (let i = 0; i < apiDealIds.length; i += CHUNK_SIZE) {
    chunks.push(apiDealIds.slice(i, i + CHUNK_SIZE));
  }

  const snapshots = await Promise.all(
    chunks.map(chunk =>
      dealsCollection.where(admin.firestore.FieldPath.documentId(), 'in', chunk).get()
    )
  );

  const existingIds = new Set(snapshots.flatMap(s => s.docs.map(d => d.id)));

  const newDeals = dealsFromApi.filter(d => !existingIds.has(String(d.id)));

  if (newDeals.length > 0) {
    const batch = db.batch();
    newDeals.forEach(deal => {
      batch.set(dealsCollection.doc(String(deal.id)), deal);
    });
    await batch.commit();
  }

  return newDeals;
}

async function removeStaleTokens(userId, tokens) {
  if (!tokens.length) return;
  await db.collection('users').doc(userId).update({
    notificationTokens: admin.firestore.FieldValue.arrayRemove(...tokens)
  });
}

async function notifyUsers(newDeals) {
  const usersSnapshot = await db.collection('users')
    .where('notificationsEnabled', '==', true)
    .get();

  if (usersSnapshot.empty) return 0;

  let notificationsSent = 0;

  const notifications = [];
  usersSnapshot.forEach(doc => {
    const user = doc.data();
    if (!user.notificationTokens?.length) return;

    const { preferredGamePlatforms: userPlatforms = [], preferredGameTypes: userTypes = [] } = user;

    const matchingDeals = newDeals.filter(deal => {
      if (!userPlatforms.length && !userTypes.length) return true;
      const platformMatch = !userPlatforms.length || userPlatforms.some(p => normalizePlatforms(deal.platforms).includes(p));
      const typeMatch = !userTypes.length || userTypes.includes(deal.type.toLowerCase());
      return platformMatch && typeMatch;
    });

    if (!matchingDeals.length) return;

    const message = {
      data: { deal_ids: matchingDeals.map(d => d.id).join(',') },
      tokens: user.notificationTokens,
    };

    notifications.push(
      messaging.sendEachForMulticast(message).then(async response => {
        notificationsSent += response.successCount;
        if (response.failureCount > 0) {
          const stale = response.responses
            .map((r, i) => (!r.success &&
              ['messaging/invalid-registration-token', 'messaging/registration-token-not-registered']
              .includes(r.error?.code)) ? user.notificationTokens[i] : null)
            .filter(Boolean);
          await removeStaleTokens(doc.id, stale);
        }
      })
    );
  });

  await Promise.all(notifications);
  return notificationsSent;
}

async function main() {
  try {
    const newDeals = await importDeals();
    let sent = 0;
    if (newDeals.length > 0) {
      sent = await notifyUsers(newDeals);
    }
    console.log(`Done. New deals: ${newDeals.length} | Notifications sent: ${sent}`);
  } catch (err) {
    console.error('Job failed:', err);
    process.exit(1);
  }
}

main();