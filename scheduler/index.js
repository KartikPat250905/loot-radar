const admin = require('firebase-admin');
const axios = require('axios');

// The service account key will be provided as an environment variable
// This is more secure than keeping a file in the repository
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
 * Fetches all deals from the API and saves/updates them in Firestore.
 * @returns {Array} A list of the new deals that were just imported.
 */
async function importDeals() {
  console.log('Starting deal import...');
  const response = await axios.get(GAME_API_URL);
  const deals = response.data;

  if (!Array.isArray(deals)) {
    console.error('API did not return an array. Aborting.');
    return [];
  }

  const dealsCollection = db.collection('deals');
  const newDeals = [];

  for (const deal of deals) {
    const docRef = dealsCollection.doc(String(deal.id));
    const doc = await docRef.get();

    if (!doc.exists) {
      // This is a brand new deal
      newDeals.push(deal);
      await docRef.set(deal);
    }
  }

  console.log(`Import complete. Found ${newDeals.length} new deals.`);
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

  // Find users who want notifications for this platform and type
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
          body: `A new deal is available: ${deal.title}`
        },
        tokens: user.notificationTokens // Send to all of the user's devices
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
