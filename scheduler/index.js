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

/**
 * Normalization Map for Game Types
 * Maps various API type strings to a consistent set used by the app.
 */
const TYPE_MAP = {
  'game': 'game',
  'full game': 'game',
  'dlc': 'dlc',
  'add-on': 'dlc',
  'early access': 'early-access',
  'loot': 'loot',
  'in-game loot': 'loot',
  'beta': 'beta',
  'other': 'other'
};

/**
 * Normalizes platform strings by removing parenthetical info and matching against PLATFORM_MAP.
 * e.g., "PC (Windows)" -> "pc", "Epic Games Store" -> "epic-games-store"
 */
function normalizeString(str) {
  if (!str) return '';
  // Strip parentheses and content inside them, then trim extra whitespace
  const raw = str.replace(/\([^)]*\)/g, '').trim().toLowerCase();
  
  if (PLATFORM_MAP[raw]) {
    return PLATFORM_MAP[raw];
  }

  const fallback = raw.replace(/\s+/g, '-');
  if (raw !== '') {
    console.log(`⚠️ Platform Normalization: "${str}" not found in PLATFORM_MAP. Using fallback: "${fallback}"`);
  }
  return fallback;
}

function normalizePlatforms(platformString) {
  if (!platformString) return [];
  return platformString.split(',').map(p => normalizeString(p));
}

/**
 * Normalizes game type strings using TYPE_MAP.
 */
function normalizeType(str) {
  if (!str) return '';
  const raw = str.replace(/\([^)]*\)/g, '').trim().toLowerCase();

  if (TYPE_MAP[raw]) {
    return TYPE_MAP[raw];
  }

  const fallback = raw.replace(/\s+/g, '-');
  if (raw !== '') {
    console.log(`⚠️ Type Normalization: "${str}" not found in TYPE_MAP. Using fallback: "${fallback}"`);
  }
  return fallback;
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
          notifiedUserIds: [], // Track notifications per-user instead of global boolean
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

/**
 * Fetches deals imported within a retention window (last 48 hours).
 */
async function getRecentDeals() {
  const fortyEightHoursAgo = new Date(Date.now() - 48 * 60 * 60 * 1000);
  const snapshot = await db.collection('deals')
    .where('importedAt', '>=', admin.firestore.Timestamp.fromDate(fortyEightHoursAgo))
    .get();
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
  console.log(`--- Phase 2: Notifying Users for ${dealsToNotify.length} recent deals ---`);
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
  const userIdsToMark = {}; // Map of dealId -> Array of userIds to update in Firestore

  dealsToNotify.forEach(d => {
    dealStats[d.id] = { 
      matchedCount: 0, 
      tokensSent: 0, 
      failures: 0,
      tier1Matches: 0,
      tier2Matches: 0,
      tier3Matches: 0
    };
    userIdsToMark[d.id] = [];
  });

  const notificationPromises = [];

  usersSnapshot.forEach(doc => {
    const user = doc.data();
    const userId = doc.id;
    
    if (!user.notificationTokens || user.notificationTokens.length === 0) {
      return;
    }

    const userPlatforms = (user.preferredGamePlatforms || []).map(p => normalizeString(p));
    const userTypes = (user.preferredGameTypes || []).map(t => normalizeType(t));

    let matchingDeals = [];
    let matchTier = 'None';

    // Tier 1: Strict Match (Platform AND Type)
    const tier1Matches = dealsToNotify.filter(deal => {
      if (deal.notifiedUserIds && deal.notifiedUserIds.includes(userId)) return false;

      const dealPlatforms = normalizePlatforms(deal.platforms);
      const dealType = normalizeType(deal.type);

      const platformMatch = userPlatforms.length === 0 || userPlatforms.some(p => dealPlatforms.includes(p));
      const typeMatch = userTypes.length === 0 || userTypes.includes(dealType);

      return platformMatch && typeMatch;
    });

    if (tier1Matches.length > 0) {
      matchingDeals = tier1Matches;
      matchTier = 'Tier 1';
    } else {
      // Tier 2: Type-only Match (Ignore Platform)
      const tier2Matches = dealsToNotify.filter(deal => {
        if (deal.notifiedUserIds && deal.notifiedUserIds.includes(userId)) return false;
        const dealType = normalizeType(deal.type);
        return userTypes.length === 0 || userTypes.includes(dealType);
      });

      if (tier2Matches.length > 0) {
        matchingDeals = tier2Matches;
        matchTier = 'Tier 2';
      } else {
        // Tier 3: Fallback to most recent unnotified deal overall
        const tier3Candidates = dealsToNotify
          .filter(deal => !(deal.notifiedUserIds && deal.notifiedUserIds.includes(userId)))
          .sort((a, b) => {
            const timeA = (a.importedAt && a.importedAt.toMillis) ? a.importedAt.toMillis() : 0;
            const timeB = (b.importedAt && b.importedAt.toMillis) ? b.importedAt.toMillis() : 0;
            return timeB - timeA;
          });

        if (tier3Candidates.length > 0) {
          matchingDeals = [tier3Candidates[0]];
          matchTier = 'Tier 3';
        }
      }
    }

    if (matchingDeals.length === 0) {
      return;
    }

    console.log(`User ${userId}: Matched ${matchingDeals.length} deals via ${matchTier}. Sending to ${user.notificationTokens.length} tokens.`);

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
        deal_ids: matchingDeals.map(d => d.id).join(','),
        matchTier: matchTier
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
        
        if (response.successCount > 0) {
          matchingDeals.forEach(d => {
            dealStats[d.id].tokensSent += response.successCount;
            dealStats[d.id].matchedCount++;
            if (matchTier === 'Tier 1') dealStats[d.id].tier1Matches++;
            else if (matchTier === 'Tier 2') dealStats[d.id].tier2Matches++;
            else if (matchTier === 'Tier 3') dealStats[d.id].tier3Matches++;
            userIdsToMark[d.id].push(userId);
          });
        }

        if (response.failureCount > 0) {
          matchingDeals.forEach(d => {
            dealStats[d.id].failures += response.failureCount;
          });
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

  // Store delivery analytics in Firestore and mark users as notified
  const batch = db.batch();
  dealsToNotify.forEach(deal => {
    const stats = dealStats[deal.id];
    const usersToMark = userIdsToMark[deal.id];
    
    const updateData = {
      notificationAnalytics: {
        usersMatched: stats.matchedCount,
        tokensSentSuccessfully: stats.tokensSent,
        tokensFailed: stats.failures,
        tier1Matches: stats.tier1Matches,
        tier2Matches: stats.tier2Matches,
        tier3Matches: stats.tier3Matches,
        lastAttemptedAt: admin.firestore.FieldValue.serverTimestamp()
      }
    };

    if (usersToMark.length > 0) {
      updateData.notifiedUserIds = admin.firestore.FieldValue.arrayUnion(...usersToMark);
    }

    batch.update(db.collection('deals').doc(String(deal.id)), updateData);
    if (stats.matchedCount > 0) {
      console.log(`Deal ${deal.id} (${deal.title}): Notified ${usersToMark.length} users (T1: ${stats.tier1Matches}, T2: ${stats.tier2Matches}, T3: ${stats.tier3Matches}).`);
    }
  });
  await batch.commit();
}

async function main() {
  try {
    await importDeals();
    
    const recentDeals = await getRecentDeals();
    if (recentDeals.length > 0) {
      console.log(`Found ${recentDeals.length} recent deals to process.`);
      await notifyUsers(recentDeals);
    } else {
      console.log('No deals within the retention window found.');
    }
    
    console.log('--- Workflow Completed Successfully ---');
  } catch (err) {
    console.error('--- ❌ Workflow Failed ❌ ---');
    console.error(err);
    process.exit(1);
  }
}

main();
