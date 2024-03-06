package com.example.screenshotService.ads

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

fun AppCompatActivity.loadInterstitialAd() {
    lifecycleScope.launch {
        AdsInterstitial.loadInterstitialAd(this@loadInterstitialAd, {}, {})
    }
}

fun AppCompatActivity.showInterstitial(callback : () -> Unit) {

    if (AdsInterstitial.mInterstitialAd != null) {
        Log.e("AdsInterstitial", "showInterstitial: AD is Loaded")
        this.lifecycleScope.launch {
            AdsInterstitial.showAdsInterstitial(this@showInterstitial, {
                callback.invoke()
            }, {
                callback.invoke()
            })
        }
    } else {
        Log.e("AdsInterstitial", "showInterstitial: Failed to load Ad")
        callback.invoke()
    }
}