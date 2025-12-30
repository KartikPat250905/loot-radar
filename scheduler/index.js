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

async function buildUserDealsMap(newDeals) {
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
      console.log(`User ${userId} has no preferred platforms set.`);
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

  // Batch fetch all user documents to get tokens
  const userIds = Array.from(userDealsMap.keys());
  const userChunks = [];
  const CHUNK_SIZE = 30; // Firestore 'in' query limit is 30

  for (let i = 0; i < userIds.length; i += CHUNK_SIZE) {
    userChunks.push(userIds.slice(i, i + CHUNK_SIZE));
  }

  const userTokensMap = new Map();

  for (const chunk of userChunks) {
    const usersSnapshot = await db.collection('users')
      .where(admin.firestore.FieldPath.documentId(), 'in', chunk)
      .get();

    usersSnapshot.forEach(doc => {
      const data = doc.data();
      if (data.notificationTokens && data.notificationTokens.length > 0) {
        userTokensMap.set(doc.id, data.notificationTokens);
      }
    });
  }

  // Send ONE notification per user with ALL their deals
  for (const [userId, deals] of userDealsMap.entries()) {
    const tokens = userTokensMap.get(userId);

    if (!tokens || tokens.length === 0) {
      console.log(`User ${userId} has no notification tokens.`);
      continue;
    }

    const message = {
      data: {
        deals: JSON.stringify(deals)
      },
      tokens: tokens
    };

    console.log(`Sending ONE notification with ${deals.length} deals to user ${userId} (${tokens.length} devices).`);

    try {
      const response = await messaging.sendEachForMulticast(message);
      console.log(`User ${userId}: Success=${response.successCount}, Failure=${response.failureCount}`);

      if (response.failureCount > 0) {
        response.responses.forEach((resp, idx) => {
          if (!resp.success) {
            console.error(`  Failed to send to token ${idx}: ${resp.error}`);
          }
        });
      }
    } catch (error) {
      console.error(`Error sending to user ${userId}:`, error);
    }
  }

  console.log('Finished sending notifications.');
}

async function cleanupExpiredDeals(validDealIds) {
  console.log('Starting cleanup of expired deals from Firestore...');
  const validDealIdsSet = new Set(validDealIds.map(String));

  const dealsSnapshot = await db.collection('deals').get();

  if (dealsSnapshot.empty) {
    console.log('No deals in Firestore to clean up.');
    return;
  }

  const batch = db.batch();
  let deleteCount = 0;

  dealsSnapshot.forEach(doc => {
    if (!validDealIdsSet.has(doc.id)) {
      batch.delete(doc.ref);
      deleteCount++;
    }
  });

  if (deleteCount > 0) {
    await batch.commit();
    console.log(`Deleted ${deleteCount} expired deals from Firestore.`);
  } else {
    console.log('No expired deals to delete.');
  }
}

async function main() {
  try {
    const { newDeals, apiDealIds } = await importDeals();
    await cleanupExpiredDeals(apiDealIds);

    if (newDeals.length > 0) {
      const userDealsMap = await buildUserDealsMap(newDeals);
      await sendPersonalizedNotifications(userDealsMap);
    } else {
      console.log('No new deals to notify users about.');
    }

    console.log('Scheduled job finished successfully.');
  } catch (error) {
    console.error('Error running scheduled job:', error);
    console.error(error.stack);
  }
}

main();