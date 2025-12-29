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
 * For each new deal, finds matching users and adds the deal to their personal notification inbox.
 * @returns {Map<string, Array<Object>>} A map where keys are user IDs and values are the new deals for that user.
 */
async function createInAppNotifications(newDeals) {
  const userDealsMap = new Map();

  for (const deal of newDeals) {
    const dealPlatforms = deal.platforms.split(',').map(p => p.trim().toLowerCase());
    const dealType = deal.type.toLowerCase();

    const usersSnapshot = await db.collection('users')
      .where('notificationsEnabled', '==', true)
      .where('preferredGamePlatforms', 'array-contains-any', dealPlatforms)
      .get();

    if (usersSnapshot.empty) {
      continue;
    }

    for (const userDoc of usersSnapshot.docs) {
      const user = userDoc.data();
      const userWantsType = user.preferredGameTypes.includes(dealType);

      if (userWantsType) {
        // Add this deal to the user's personal notification inbox
        const notificationRef = db.collection('users').doc(userDoc.id).collection('notifications').doc(String(deal.id));
        await notificationRef.set({ ...deal, receivedAt: new Date(), read: false });
        
        // Track which deals belong to which user
        if (!userDealsMap.has(userDoc.id)) {
          userDealsMap.set(userDoc.id, []);
        }
        userDealsMap.get(userDoc.id).push(deal);
      }
    }
  }
  return userDealsMap;
}

/**
 * Sends a personalized, summary push notification to each user who has new deals.
 */
async function sendPersonalizedNotifications(userDealsMap) {
  if (userDealsMap.size === 0) {
    console.log('No users to notify.');
    return;
  }

  console.log(`Preparing to send personalized notifications to ${userDealsMap.size} users.`);

  for (const [userId, deals] of userDealsMap.entries()) {
    const userDoc = await db.collection('users').doc(userId).get();
    const user = userDoc.data();

    if (user && user.notificationTokens && user.notificationTokens.length > 0) {
      const dealCount = deals.length;
      let title, body;

      if (dealCount === 1) {
        title = 'Your Loot Radar is beeping!';
        body = 'A new free game deal matching your preferences just dropped!';
      } else {
        title = 'Heads up, new loot spotted!';
        body = `You\'ve got ${dealCount} new free game deals waiting for you!`;
      }

      const message = {
        notification: { title, body },
        tokens: user.notificationTokens,
      };

      console.log(`Sending notification to user ${userId} for ${dealCount} deals.`);
      await messaging.sendEachForMulticast(message);
    }
  }
  console.log('Personalized notifications sent successfully.');
}

/**
 * Main function to run the entire process.
 */
async function main() {
  try {
    const newDeals = await importDeals();
    if (newDeals.length > 0) {
      const userDealsMap = await createInAppNotifications(newDeals);
      await sendPersonalizedNotifications(userDealsMap);
    }
    console.log('Scheduled job finished successfully.');
  } catch (error) {
    console.error('Error running scheduled job:', error);
  }
}

main();
