const admin = require('firebase-admin');
const axios = require('axios');

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

async function createInAppNotifications(newDeals) {
  const userDealsMap = new Map();

  const usersSnapshot = await db.collection('users')
    .where('notificationsEnabled', '==', true)
    .get();

  if (usersSnapshot.empty) {
    console.log("No users have notifications enabled.");
    return userDealsMap;
  }

  console.log(`Matching ${newDeals.length} new deals against ${usersSnapshot.size} users.`);

  for (const userDoc of usersSnapshot.docs) {
    const user = userDoc.data();
    const userId = userDoc.id;
    const userPlatforms = new Set((user.preferredGamePlatforms || []).map(p => p.toLowerCase()));

    if (userPlatforms.size === 0) {
      continue;
    }

    const matchingDealsForUser = [];

    for (const deal of newDeals) {
      const dealPlatforms = deal.platforms.split(',').map(p => p.trim().toLowerCase());
      const hasMatchingPlatform = dealPlatforms.some(p => userPlatforms.has(p));

      if (hasMatchingPlatform) {
        matchingDealsForUser.push(deal);
      }
    }

    if (matchingDealsForUser.length > 0) {
      console.log(`User ${userId} has ${matchingDealsForUser.length} matching deals.`);
      userDealsMap.set(userId, matchingDealsForUser);

      // Save to Firestore for backup
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

async function sendPersonalizedNotifications(userDealsMap) {
  if (userDealsMap.size === 0) {
    console.log('No users with matching deals to notify.');
    return;
  }

  console.log(`Preparing to send notifications to ${userDealsMap.size} users.`);

  for (const [userId, deals] of userDealsMap.entries()) {
    const userDoc = await db.collection('users').doc(userId).get();
    const user = userDoc.data();

    if (!user || !user.notificationTokens || user.notificationTokens.length === 0) {
      console.log(`User ${userId} has no notification tokens.`);
      continue;
    }

    // Send ONE message with ALL deals
    const message = {
      data: {
        deals: JSON.stringify(deals)
      },
      tokens: user.notificationTokens
    };

    console.log(`Sending ONE notification with ${deals.length} deals to user ${userId}.`);

    try {
      const response = await messaging.sendEachForMulticast(message);
      console.log(`Successfully sent to user ${userId}. Success: ${response.successCount}, Failure: ${response.failureCount}`);
    } catch (error) {
      console.error(`Error sending to user ${userId}:`, error);
    }
  }

  console.log('Finished sending notifications.');
}

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