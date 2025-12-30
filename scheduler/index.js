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
 * This version handles a variable number of API results by chunking queries.
 * @returns {Array} A list of the new deals that were just imported.
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

  // --- CHUNKING LOGIC --- //
  const CHUNK_SIZE = 30; // Firestore 'in' query limit
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
  // --- END CHUNKING LOGIC --- //

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
 * Finds users whose preferences match a given deal and sends them a notification.
 * @param {Object} deal - The game deal to check against user preferences.
 */
async function notifyUsersForDeal(deal) {
  if (!deal || !deal.platforms) {
    return;
  }

  const dealPlatforms = deal.platforms.split(',').map(p => p.trim().toLowerCase());
  const dealType = deal.type.toLowerCase();

  // This query is already efficient as it targets only relevant users.
  const usersSnapshot = await db.collection('users')
    .where('notificationsEnabled', '==', true)
    .where('preferredGamePlatforms', 'array-contains-any', dealPlatforms)
    .get();

  if (usersSnapshot.empty) {
    console.log(`No users found for platforms: ${dealPlatforms.join(', ')}`);
    return;
  }

  const notificationsToSend = [];

  usersSnapshot.forEach(doc => {
    const user = doc.data();
    const userWantsType = user.preferredGameTypes.includes(dealType);

    if (userWantsType && user.notificationTokens && user.notificationTokens.length > 0) {
      const message = {
        notification: {
          title: 'New Free Game Alert!',
          body: `A new deal is available: ${deal.title}`,
        },
        tokens: user.notificationTokens,
      };
      notificationsToSend.push(messaging.sendEachForMulticast(message));
    }
  });

  if (notificationsToSend.length > 0) {
    console.log(`Sending notifications to ${notificationsToSend.length} users for deal: ${deal.title}`);
    await Promise.all(notificationsToSend);
  }
}

/**
 * Main function to run the entire process.
 */
async function main() {
  try {
    const newDeals = await importDeals();
    if (newDeals.length > 0) {
      console.log('Checking new deals against user preferences...');
      for (const deal of newDeals) {
        await notifyUsersForDeal(deal);
      }
    }
    console.log('Scheduled job finished successfully.');
  } catch (error) {
    console.error('Error running scheduled job:', error);
  }
}

main();