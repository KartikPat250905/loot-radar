package com.example.freegameradar

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(private val context: Context) {
    private val gameDetailAdUnitId = "ca-app-pub-5652022735470762/3505969771"
    private val refreshAdUnitId = "ca-app-pub-5652022735470762/2231921718"
    private val settingsAdUnitId = "ca-app-pub-5652022735470762/2192888100"

    private var gameDetailAd: InterstitialAd? = null
    private var refreshAd: InterstitialAd? = null
    private var settingsAd: InterstitialAd? = null

    init {
        MobileAds.initialize(context)
    }

    // Load game detail ad
    fun loadGameDetailAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            gameDetailAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    gameDetailAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    gameDetailAd = null
                }
            }
        )
    }

    fun showGameDetailAd(activity: Activity) {
        gameDetailAd?.show(activity) ?: run {
            loadGameDetailAd()
        }
    }

    // Load refresh ad
    fun loadRefreshAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            refreshAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    refreshAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    refreshAd = null
                }
            }
        )
    }

    fun showRefreshAd(activity: Activity) {
        refreshAd?.show(activity) ?: run {
            loadRefreshAd()
        }
    }

    // Load settings ad - NEW
    fun loadSettingsAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            settingsAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    settingsAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    settingsAd = null
                }
            }
        )
    }

    fun showSettingsAd(activity: Activity) {
        settingsAd?.show(activity) ?: run {
            loadSettingsAd()
        }
    }
}