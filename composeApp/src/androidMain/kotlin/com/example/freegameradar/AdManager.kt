package com.example.freegameradar

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(
    private val context: Context,
    private val shouldShowAds: () -> Boolean  // ✅ NEW: Callback to check ad-free status
) {

    private val gameDetailAdUnitId = "ca-app-pub-5652022735470762/3505969771"
    private val refreshAdUnitId = "ca-app-pub-5652022735470762/2231921718"
    private val settingsAdUnitId = "ca-app-pub-5652022735470762/2192888100"

    private var gameDetailAd: InterstitialAd? = null
    private var refreshAd: InterstitialAd? = null
    private var settingsAd: InterstitialAd? = null

    init {
        MobileAds.initialize(context)
    }

    // ✅ Helper to check if ads should be shown
    private fun canShowAds(): Boolean {
        val shouldShow = shouldShowAds()
        if (!shouldShow) {
            Log.d("AdManager", "🚫 User is ad-free, skipping ad")
        }
        return shouldShow
    }

    // Game Detail Ad
    fun loadGameDetailAd() {
        if (!canShowAds()) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            gameDetailAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    gameDetailAd = ad
                    Log.d("AdManager", "Game detail ad loaded successfully")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdManager", "Game detail ad dismissed")
                            gameDetailAd = null
                            loadGameDetailAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e("AdManager", "Game detail ad failed to show: ${error.message}")
                            gameDetailAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdManager", "Game detail ad shown")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Game detail ad failed to load: ${error.message}")
                    gameDetailAd = null
                }
            }
        )
    }

    fun showGameDetailAd(activity: Activity) {
        if (!canShowAds()) return

        if (gameDetailAd != null) {
            Log.d("AdManager", "Showing game detail ad")
            gameDetailAd?.show(activity)
            gameDetailAd = null
        } else {
            Log.w("AdManager", "Game detail ad not ready yet, loading...")
            loadGameDetailAd()
        }
    }

    // Refresh Ad
    fun loadRefreshAd() {
        if (!canShowAds()) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            refreshAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    refreshAd = ad
                    Log.d("AdManager", "Refresh ad loaded successfully")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdManager", "Refresh ad dismissed")
                            refreshAd = null
                            loadRefreshAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e("AdManager", "Refresh ad failed to show: ${error.message}")
                            refreshAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdManager", "Refresh ad shown")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Refresh ad failed to load: ${error.message}")
                    refreshAd = null
                }
            }
        )
    }

    fun showRefreshAd(activity: Activity) {
        if (!canShowAds()) return

        if (refreshAd != null) {
            Log.d("AdManager", "Showing refresh ad")
            refreshAd?.show(activity)
            refreshAd = null
        } else {
            Log.w("AdManager", "Refresh ad not ready yet, loading...")
            loadRefreshAd()
        }
    }

    // Settings Ad
    fun loadSettingsAd() {
        if (!canShowAds()) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            settingsAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    settingsAd = ad
                    Log.d("AdManager", "Settings ad loaded successfully")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdManager", "Settings ad dismissed")
                            settingsAd = null
                            loadSettingsAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e("AdManager", "Settings ad failed to show: ${error.message}")
                            settingsAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdManager", "Settings ad shown")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Settings ad failed to load: ${error.message}")
                    settingsAd = null
                }
            }
        )
    }

    fun showSettingsAd(activity: Activity) {
        if (!canShowAds()) return

        if (settingsAd != null) {
            Log.d("AdManager", "Showing settings ad")
            settingsAd?.show(activity)
            settingsAd = null
        } else {
            Log.w("AdManager", "Settings ad not ready yet, loading...")
            loadSettingsAd()
        }
    }
}
