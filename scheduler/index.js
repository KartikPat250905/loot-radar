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
  if (!platformString) return [];
  return platformString.split(',').map(p => {
    const key = p.trim().toLowerCase();
    return PLATFORM_MAP[key] || key;
  });
}

async function importDeals() {
  const { data: dealsFromApi } = await axios.get(GAME_API_URL);
  if (!Array.isArray(dealsFromApi) || dealsFromApi.length === 0) return;

  const dealsCollection = db.collection('deals');
  const apiDealIds = dealsFromApi.map(d => String(d.id));

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
      // Initialize with sent: false
      batch.set(dealsCollection.doc(String(deal.id)), { ...deal, sent: false });
    });
    await batch.commit();
    console.log(`Imported ${newDeals.length} new deals.`);
  }
}

async function getUnsentDeals() {
  const snapshot = await db.collection('deals').where('sent', '==', false).get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

async function removeStaleTokens(userId, tokens) {
  if (!tokens.length) return;
  await db.collection('users').doc(userId).update({
    notificationTokens: admin.firestore.FieldValue.arrayRemove(...tokens)
  });
}

async function notifyUsers(dealsToNotify) {
  const usersSnapshot = await db.collection('users')
    .where('notificationsEnabled', '==', true)
    .get();

  if (usersSnapshot.empty) {
    console.log('No users with notifications enabled.');
  } else {
    let totalNotificationsSent = 0;
    const notificationPromises = [];

    usersSnapshot.forEach(doc => {
      const user = doc.data();
      if (!user.notificationTokens?.length) return;

      const { preferredGamePlatforms: userPlatforms = [], preferredGameTypes: userTypes = [] } = user;

      const matchingDeals = dealsToNotify.filter(deal => {
        if (!userPlatforms.length && !userTypes.length) return true;
        const platformMatch = !userPlatforms.length || userPlatforms.some(p => normalizePlatforms(deal.platforms).includes(p));
        const typeMatch = !userTypes.length || userTypes.includes(deal.type.toLowerCase());
        return platformMatch && typeMatch;
      });

      if (matchingDeals.length === 0) return;

      const message = {
        data: { deal_ids: matchingDeals.map(d => d.id).join(',') },
        tokens: user.notificationTokens,
      };

      notificationPromises.push(
        messaging.sendEachForMulticast(message).then(async response => {
          totalNotificationsSent += response.successCount;
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

    await Promise.all(notificationPromises);
    console.log(`Notifications sent: ${totalNotificationsSent}`);
  }

  // Mark all these deals as sent in Firestore so they aren't processed again next run
  const batch = db.batch();
  dealsToNotify.forEach(deal => {
    batch.update(db.collection('deals').doc(String(deal.id)), { sent: true });
  });
  await batch.commit();
}

async function main() {
  try {
    await importDeals();
    const unsentDeals = await getUnsentDeals();
    
    if (unsentDeals.length > 0) {
      console.log(`Processing ${unsentDeals.length} unsent deals...`);
      await notifyUsers(unsentDeals);
    } else {
      console.log('No unsent deals to process.');
    }
    console.log('Job finished successfully.');
  } catch (err) {
    console.error('Job failed:', err);
    process.exit(1);
  }
}

main();
