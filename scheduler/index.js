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
    return { newDeals: [], apiDealIds: [] };
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

  return { newDeals, apiDealIds };
}

/**
 * REWRITTEN: Collects all matching deals for each user and builds a map.
 * This follows the correct flow of checking each user against all new deals.
 * @returns {Map<string, Array<Object>>} A map where keys are user IDs and values are their matching deals.
 */
async function createInAppNotifications(newDeals) {
  const userDealsMap = new Map();
  
  // 1. Get all users who have notifications enabled.
  const usersSnapshot = await db.collection('users').where('notificationsEnabled', '==', true).get();

  if (usersSnapshot.empty) {
    console.log("No users have notifications enabled. Skipping deal matching.");
    return userDealsMap;
  }
  
  console.log(`Matching ${newDeals.length} new deals against ${usersSnapshot.size} users.`);

  // 2. For each user, iterate through all new deals to find matches.
  for (const userDoc of usersSnapshot.docs) {
    const user = userDoc.data();
    const userId = userDoc.id;
    const userPlatforms = new Set((user.preferredGamePlatforms || []).map(p => p.toLowerCase()));
    
    if (userPlatforms.size === 0) {
      continue; // Skip user if they haven't set any preferred platforms.
    }

    const matchingDealsForUser = [];
    
    for (const deal of newDeals) {
      const dealPlatforms = deal.platforms.split(',').map(p => p.trim().toLowerCase());
      const hasMatchingPlatform = dealPlatforms.some(p => userPlatforms.has(p));
      
      if (hasMatchingPlatform) {
        matchingDealsForUser.push(deal);
      }
    }
    
    // 3. If matches were found, add them to the user's list in the map.
    if (matchingDealsForUser.length > 0) {
      console.log(`User ${userId} has ${matchingDealsForUser.length} matching deals.`);
      userDealsMap.set(userId, matchingDealsForUser);

      // Also update their in-app notification inbox in a single batch.
      const batch = db.batch();
      const notificationsCollection = db.collection('users').doc(userId).collection('notifications');
      matchingDealsForUser.forEach(deal => {
        const notificationRef = notificationsCollection.doc(String(deal.id));
        batch.set(notificationRef, { ...deal, receivedAt: new Date(), read: false });
      });
      await batch.commit();
    }
  }
  
  return userDealsMap;
}


/**
 * REWRITTEN: Sends ONE personalized data message per user, containing ALL their deals.
 */
async function sendPersonalizedNotifications(userDealsMap) {
  if (userDealsMap.size === 0) {
    console.log('No users with matching deals to notify.');
    return;
  }

  console.log(`Starting to send ONE notification each to ${userDealsMap.size} users.`);

  const userIds = Array.from(userDealsMap.keys());
  // Fetch all required user documents at once to get their tokens
  const userDocs = await db.collection('users').where(admin.firestore.FieldPath.documentId(), 'in', userIds).get();
  
  const userIdToTokens = new Map();
  userDocs.forEach(doc => {
      const data = doc.data();
      if (data.notificationTokens && data.notificationTokens.length > 0) {
          userIdToTokens.set(doc.id, data.notificationTokens);
      }
  });

  // For each user in the map, send ONE message containing ALL their deals.
  for (const [userId, deals] of userDealsMap.entries()) {
    const tokens = userIdToTokens.get(userId);

    if (tokens && tokens.length > 0) {
      const message = {
        data: {
          // The 'deals' field now contains the full array of deals as a JSON string.
          deals: JSON.stringify(deals)
        },
        tokens: tokens,
      };

      console.log(`Sending ONE message with ${deals.length} deals to user ${userId}.`);
      // This sends a single multicast message to all of a user's devices.
      await messaging.sendEachForMulticast(message);
    } else {
        console.log(`User ${userId} had matching deals but no registered notification tokens.`);
    }
  }
  
  console.log('Finished sending all personalized data messages.');
}


/**
 * Deletes notifications for deals that are no longer active.
 */
async function cleanupExpiredNotifications(validDealIds) {
  console.log('Starting expired notifications cleanup...');
  const validDealIdsSet = new Set(validDealIds.map(String));
  const usersSnapshot = await db.collection('users').get();

  if (usersSnapshot.empty) {
    console.log('No users found for cleanup.');
    return;
  }

  const cleanupPromises = [];
  usersSnapshot.forEach(userDoc => {
    const notificationsCollection = userDoc.ref.collection('notifications');
    const promise = notificationsCollection.get().then(notificationsSnapshot => {
      if (notificationsSnapshot.empty) {
        return;
      }
      const batch = db.batch();
      let deletedCountForUser = 0;
      notificationsSnapshot.forEach(notificationDoc => {
        if (!validDealIdsSet.has(notificationDoc.id)) {
          batch.delete(notificationDoc.ref);
          deletedCountForUser++;
        }
      });
      if (deletedCountForUser > 0) {
        console.log(`Deleting ${deletedCountForUser} expired notifications for user ${userDoc.id}.`);
        return batch.commit();
      }
    });
    cleanupPromises.push(promise);
  });

  await Promise.all(cleanupPromises);
  console.log('Expired notifications cleanup finished.');
}


/**
 * Main function to run the entire process.
 */
async function main() {
  try {
    const { newDeals, apiDealIds } = await importDeals();
    await cleanupExpiredNotifications(apiDealIds);

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