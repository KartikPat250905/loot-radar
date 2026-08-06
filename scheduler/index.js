const admin = require('firebase-admin');
const axios = require('axios');

if (!process.env.SERVICE_ACCOUNT_KEY_JSON) {
  throw new Error('SERVICE_ACCOUNT_KEY_JSON environment variable is not set.');
}

admin.initializeApp({
  credential: admin.credential.cert(JSON.parse(process.env.SERVICE_ACCOUNT_KEY_JSON))
});

const db = admin.firestore();
const messaging = admin.messaging();
const GAME_API_URL = 'https://www.gamerpower.com/api/giveaways';
const CHUNK_SIZE = 30;

/**
 * Normalization Map for Platforms
 * Ensures variations match the keys used in the API and app.
 */
const PLATFORM_MAP = {
  'epic games store': 'epic-games-store',
  'epic games': 'epic-games-store',
  'nintendo switch': 'switch',
  'playstation 4': 'ps4',
  'playstation 5': 'ps5',
  'ps4': 'ps4',
  'ps5': 'ps5',
  'xbox one': 'xbox-one',
  'xbox series x|s': 'xbox-series-xs',
  'xbox series xs': 'xbox-series-xs',
  'xbox 360': 'xbox-360',
  'battle.net': 'battlenet',
  'pc': 'pc',
  'steam': 'steam',
  'gog': 'gog',
  'itch.io': 'itchio',
  'origin': 'origin',
  'ubisoft': 'ubisoft',
  'android': 'android',
  'ios': 'ios'
};

function normalizeString(str) {
  if (!str) return '';
  const key = str.trim().toLowerCase();
  return PLATFORM_MAP[key] || key.replace(/\s+/g, '-');
}

function normalizePlatforms(platformString) {
  if (!platformString) return [];
  return platformString.split(',').map(p => normalizeString(p));
}

async function importDeals() {
  console.log('--- Phase 1: Importing Deals from API ---');
  try {
    const { data: dealsFromApi } = await axios.get(GAME_API_URL);
    if (!Array.isArray(dealsFromApi) || dealsFromApi.length === 0) {
      console.log('No deals found in API response.');
      return;
    }

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
        batch.set(dealsCollection.doc(String(deal.id)), { 
          ...deal, 
          sent: false,
          importedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      });
      await batch.commit();
      console.log(`✅ Success: Imported ${newDeals.length} new deals.`);
    } else {
      console.log('No new deals to import (all already exist).');
    }
  } catch (error) {
    console.error('❌ Phase 1 Failed:', error.message);
    throw error;
  }
}

async function getUnsentDeals() {
  const snapshot = await db.collection('deals').where('sent', '==', false).get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

async function removeStaleTokens(userId, tokens) {
  if (!tokens.length) return;
  console.log(`🧹 Cleaning up ${tokens.length} stale tokens for user ${userId}`);
  await db.collection('users').doc(userId).update({
    notificationTokens: admin.firestore.FieldValue.arrayRemove(...tokens)
  });
}

async function notifyUsers(dealsToNotify) {
  console.log(`--- Phase 2: Notifying Users for ${dealsToNotify.length} deals ---`);
  const usersSnapshot = await db.collection('users')
    .where('notificationsEnabled', '==', true)
    .get();

  if (usersSnapshot.empty) {
    console.log('⚠️ No users have notifications enabled.');
    return;
  }

  console.log(`Checking matches for ${usersSnapshot.size} active users.`);
  
  let totalNotificationsSent = 0;
  const dealStats = {}; 
  dealsToNotify.forEach(d => {
    dealStats[d.id] = { matchedCount: 0, tokensSent: 0, failures: 0 };
  });

  const notificationPromises = [];

  usersSnapshot.forEach(doc => {
    const user = doc.data();
    const userId = doc.id;
    
    // Log user matching status
    if (!user.notificationTokens || user.notificationTokens.length === 0) {
      console.log(`User ${userId}: Skipped (0 tokens)`);
      return;
    }

    const userPlatforms = (user.preferredGamePlatforms || []).map(p => normalizeString(p));
    const userTypes = (user.preferredGameTypes || []).map(t => t.toLowerCase());

    const matchingDeals = dealsToNotify.filter(deal => {
      const dealPlatforms = normalizePlatforms(deal.platforms);
      const dealType = (deal.type || '').toLowerCase();

      const platformMatch = userPlatforms.length === 0 || userPlatforms.some(p => dealPlatforms.includes(p));
      const typeMatch = userTypes.length === 0 || userTypes.includes(dealType);

      if (platformMatch && typeMatch) {
        dealStats[deal.id].matchedCount++;
        return true;
      }
      return false;
    });

    if (matchingDeals.length === 0) {
      console.log(`User ${userId}: No matches (Preferences: Platforms[${userPlatforms.join(', ')}], Types[${userTypes.join(', ')}])`);
      return;
    }

    console.log(`User ${userId}: Matched ${matchingDeals.length} deals. Sending to ${user.notificationTokens.length} tokens.`);

    // Send BOTH Notification and Data payload for background reliability
    const message = {
      notification: {
        title: matchingDeals.length === 1 
          ? '🎁 New Free Game Detected!' 
          : `📡 ${matchingDeals.length} New Free Games Found!`,
        body: matchingDeals.length === 1
          ? `${matchingDeals[0].title} is now free on ${matchingDeals[0].platforms}.`
          : `Check out the latest free games matching your radar preferences.`
      },
      data: { 
        deal_ids: matchingDeals.map(d => d.id).join(',')
      },
      tokens: user.notificationTokens,
      android: {
        priority: 'high',
        notification: {
          channelId: 'new_deals_channel'
        }
      }
    };

    notificationPromises.push(
      messaging.sendEachForMulticast(message).then(async response => {
        totalNotificationsSent += response.successCount;
        
        matchingDeals.forEach(d => {
          dealStats[d.id].tokensSent += response.successCount;
          dealStats[d.id].failures += response.failureCount;
        });

        if (response.failureCount > 0) {
          const stale = response.responses
            .map((r, i) => (!r.success &&
              ['messaging/invalid-registration-token', 'messaging/registration-token-not-registered']
              .includes(r.error?.code)) ? user.notificationTokens[i] : null)
            .filter(Boolean);
          if (stale.length > 0) {
            await removeStaleTokens(userId, stale);
          }
        }
      }).catch(err => {
        console.error(`❌ FCM Multicast Error for user ${userId}:`, err.message);
      })
    );
  });

  await Promise.all(notificationPromises);
  console.log(`✅ Success: Total tokens notified: ${totalNotificationsSent}`);

  // Store delivery analytics in Firestore and mark as sent
  const batch = db.batch();
  dealsToNotify.forEach(deal => {
    const stats = dealStats[deal.id];
    batch.update(db.collection('deals').doc(String(deal.id)), { 
      sent: true,
      notificationAnalytics: {
        usersMatched: stats.matchedCount,
        tokensSentSuccessfully: stats.tokensSent,
        tokensFailed: stats.failures,
        lastAttemptedAt: admin.firestore.FieldValue.serverTimestamp()
      }
    });
    console.log(`Deal ${deal.id} (${deal.title}): Matched ${stats.matchedCount} users.`);
  });
  await batch.commit();
}

async function main() {
  try {
    await importDeals();
    
    const unsentDeals = await getUnsentDeals();
    if (unsentDeals.length > 0) {
      console.log(`Found ${unsentDeals.length} pending deals to process.`);
      await notifyUsers(unsentDeals);
    } else {
      console.log('No unsent deals in the database.');
    }
    
    console.log('--- Workflow Completed Successfully ---');
  } catch (err) {
    console.error('--- ❌ Workflow Failed ❌ ---');
    console.error(err);
    process.exit(1);
  }
}

main();
