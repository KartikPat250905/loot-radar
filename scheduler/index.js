const admin = require('firebase-admin');
const axios = require('axios');

// The service account key will be provided as an environment variable
if (!process.env.SERVICE_ACCOUNT_KEY_JSON) {
  throw new Error('The SERVICE_ACCOUNT_KEY_JSON environment variable is not set.');
}
const serviceAccount = JSON.parse(process.env.SERVICE_ACCOUNT_KEY_JSON);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const messaging = admin.messaging();
const GAME_API_URL = 'https://www.gamerpower.com/api/giveaways';

/**
 * Efficiently fetches deals, identifies only the new ones, and saves them to Firestore.
 */
async function importDeals() {
  console.log('Starting deal import...');
  const response = await axios.get(GAME_API_URL);
  const dealsFromApi = response.data;

  if (!Array.isArray(dealsFromApi) || dealsFromApi.length === 0) {
    console.log('API did not return an array or it was empty. Aborting.');
    return [];
  }

  const dealsCollection = db.collection('deals');
  const apiDealIds = dealsFromApi.map(deal => String(deal.id));

  const CHUNK_SIZE = 30; 
  const idChunks = [];
  for (let i = 0; i < apiDealIds.length; i += CHUNK_SIZE) {
    idChunks.push(apiDealIds.slice(i, i + CHUNK_SIZE));
  }

  const existingDealIds = new Set();
  const queryPromises = idChunks.map(chunk =>
    dealsCollection.where(admin.firestore.FieldPath.documentId(), 'in', chunk).get()
  );

  const snapshots = await Promise.all(queryPromises);
  snapshots.forEach(snapshot => {
    snapshot.docs.forEach(doc => existingDealIds.add(doc.id));
  });

  console.log(`API returned ${apiDealIds.length} deals. Found ${existingDealIds.size} existing deals in DB.`);

  const newDeals = [];
  const batch = db.batch();

  dealsFromApi.forEach(deal => {
    const dealId = String(deal.id);
    if (!existingDealIds.has(dealId)) {
      newDeals.push(deal);
      const docRef = dealsCollection.doc(dealId);
      batch.set(docRef, deal);
    }
  });

  if (newDeals.length > 0) {
    await batch.commit();
    console.log(`Successfully imported ${newDeals.length} new deals.`);
  } else {
    console.log('No new deals to import.');
  }

  return newDeals;
}

/**
 * Efficiently finds all users who should be notified about new deals and sends them a 
 * single notification containing only the relevant deal IDs.
 * @param {Array} newDeals - A list of the new deals that were just imported.
 */
async function notifyUsersAboutNewDeals(newDeals) {
  const usersSnapshot = await db.collection('users')
    .where('notificationsEnabled', '==', true)
    .get();

  if (usersSnapshot.empty) {
    console.log('No users with notifications enabled.');
    return;
  }

  const allNotifications = [];

  usersSnapshot.forEach(doc => {
    const user = doc.data();
    const userPlatforms = user.preferredGamePlatforms || [];
    const userTypes = user.preferredGameTypes || [];

    const matchingDealsForUser = newDeals.filter(deal => {
      const dealPlatforms = deal.platforms.split(',').map(p => p.trim().toLowerCase());
      const dealType = deal.type.toLowerCase();
      const platformMatch = userPlatforms.some(p => dealPlatforms.includes(p));
      const typeMatch = userTypes.includes(dealType);
      return platformMatch && typeMatch;
    });

    if (matchingDealsForUser.length > 0 && user.notificationTokens && user.notificationTokens.length > 0) {
      // Per your recommendation, we now only send the IDs.
      const dealIds = matchingDealsForUser.map(deal => deal.id).join(',');

      const message = {
        data: {
          deal_ids: dealIds
        },
        tokens: user.notificationTokens,
      };

      allNotifications.push(messaging.sendEachForMulticast(message));
      console.log(`Preparing to send ${matchingDealsForUser.length} deal IDs to user ${doc.id}`);
    }
  });

  if (allNotifications.length > 0) {
    console.log(`Sending notifications to users.`);
    await Promise.all(allNotifications);
  }
}

/**
 * Main function to run the entire process.
 */
async function main() {
  try {
    const newDeals = await importDeals();
    if (newDeals.length > 0) {
      // This is now much more efficient, sending one notification per user.
      console.log('Notifying users about new deals...');
      await notifyUsersAboutNewDeals(newDeals);
    }
    console.log('Scheduled job finished successfully.');
  } catch (error) {
    console.error('Error running scheduled job:', error);
  }
}

main();