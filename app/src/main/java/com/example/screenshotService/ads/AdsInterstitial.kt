package com.example.screenshotService.ads

import android.net.ConnectivityManager
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AdsInterstitial {
    private val TAG = "AdsInterstitial"
    var isAdLoaded: Boolean = false
    var interstitialTimeElapsed = 0L
    var mInterstitialAd: InterstitialAd? = null
    var isAdLoading = false
    private var timer: CountDownTimer? = null
    private val mutex = Mutex()

    private fun timeDifference(millis: Long): Int {
        val current = Calendar.getInstance().timeInMillis
        val elapsedTime = current - millis
        return TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toInt()
    }

    fun isNetworkAvailable(context: AppCompatActivity): Boolean {
        val connectivityManager =
            context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    suspend fun showAdsInterstitial(
        activity: AppCompatActivity, adClosedCallback: () -> Unit, adFailedCallback: () -> Unit,
    ) = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (mInterstitialAd != null) {
                if (isNetworkAvailable(activity)) {

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(100)
                        if (mInterstitialAd != null) {
                            Log.e(
                                TAG, " Ad ready to show: $mInterstitialAd"
                            )
                            mInterstitialAd?.show(activity)
                            mInterstitialAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {

                                    override fun onAdImpression() {
                                        super.onAdImpression()
                                        interstitialTimeElapsed =
                                            Calendar.getInstance().timeInMillis
                                        Log.e(
                                            TAG, " onAdImpression: $mInterstitialAd"
                                        )
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()


                                        mInterstitialAd = null
                                        isAdLoaded = false
                                        adClosedCallback.invoke()
                                        Log.e(
                                            TAG,
                                            " onAdDismissedFullScreenContent: $mInterstitialAd"
                                        )

//                                            CoroutineScope(Dispatchers.Main).launch {
//
//                                                loadInterstitialAd(activity, {}, {})
//                                            }
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        mInterstitialAd = null
                                        isAdLoaded = false
                                        adFailedCallback.invoke()

                                        Log.e(
                                            TAG,
                                            " onAdFailedToShowFullScreenContent: $mInterstitialAd"
                                        )
                                        //mutex.unlock()
                                    }
                                }
                        }
                    }
                }


            }

        }
    }

    suspend fun loadInterstitialAd(
        activity: AppCompatActivity,
        adLoaded: () -> Unit,
        adFailed: () -> Unit,
    ) {
        if (mInterstitialAd == null && timeDifference(interstitialTimeElapsed) > 3) {
            mutex.withLock {

                isAdLoading = true

                try {
                    val interstitialAd: String = "ca-app-pub-3940256099942544/1033173712"
                    InterstitialAd.load(activity,
                        interstitialAd!!,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e(TAG, adError.message)
                                isAdLoaded = false
                                Log.e(TAG, " onAdFailedToLoad: $mInterstitialAd")
                                mInterstitialAd = null
                                isAdLoading = false
                                timer?.onFinish()
                                adFailed.invoke()
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                isAdLoaded = true
                                mInterstitialAd = interstitialAd

                                isAdLoading = false
                                adLoaded.invoke()

                            }
                        })
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading interstitial ad: ${e.message}")
                    isAdLoading = false
                    timer?.onFinish()
                    adFailed.invoke()

                }

            }
        }
    }
}