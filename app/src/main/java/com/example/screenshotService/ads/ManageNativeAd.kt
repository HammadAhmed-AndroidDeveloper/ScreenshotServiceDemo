package com.example.screenshotService.ads

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.screenshotService.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class ManageNativeAd(val activity: AppCompatActivity) {

    private val TAG = "ManageNativeAd"


//    Admob *******************************************************************************************************************************

    companion object {
        var mNativeAd: NativeAd? = null
        var mExitNativeAd: NativeAd? = null
        var isLoadedCalled = false
        var isLoadedCalled1 = false
    }

    fun loadAdmobNativeAd(frameLayout: FrameLayout?) {

        val adUnit = "ca-app-pub-3940256099942544/1044960115"

        if (adUnit!!.isEmpty()) {
            return
        }

        val adView: NativeAdView =
            activity.layoutInflater.inflate(
                R.layout.admob_native_template,
                null
            ) as NativeAdView

        if (mNativeAd == null && !isLoadedCalled) {
            isLoadedCalled = true
            val builder = AdLoader.Builder(activity, adUnit)
            Log.d(TAG, "loadAdmobNativeAd: loading.....")
            // OnUnifiedNativeAdLoadedListener implementation.
            builder.forNativeAd { nativeAd: NativeAd ->
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (mNativeAd != null) {
                    mNativeAd!!.destroy()
                }
//                try {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        adStatus.visibility = GONE
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
                mNativeAd = nativeAd
                populateNativeAdView(nativeAd, adView)
                frameLayout?.removeAllViews()
                frameLayout?.addView(adView)

            }.withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()
            builder.withNativeAdOptions(adOptions)

            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Failed to load native ad: $loadAdError")
//                    android.os.Handler(Looper.getMainLooper()).post {
//                        frameLayout.visibility = GONE
//                        adStatus.visibility = GONE
//                    }
                    isLoadedCalled = false
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "onAdLoaded: Native Ad is Loaded now")
//                    try {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            adStatus.visibility = GONE
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
                }
            }

            ).build()
            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            mNativeAd?.let {
                frameLayout?.removeAllViews()
                frameLayout?.visibility = View.VISIBLE
                frameLayout!!.addView(adView)
                populateNativeAdView(it, adView)
            } ?: run {
                frameLayout?.visibility = View.VISIBLE
//                adStatus.visibility = VISIBLE
//                isLoadedCalled = false
            }
        }
    }


    private fun populateNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        // Set the media view.
//        val ad_loading_View: ConstraintLayout = adView.findViewById(R.id.ad_loading_View)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        /*adView.setPriceView(adView.findViewById(R.id.ad_price));*/
//        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        /*adView.setStoreView(adView.findViewById(R.id.ad_store));*/
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
//        nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
//        if (nativeAd.starRating == null) {
//            adView.starRatingView?.visibility = View.INVISIBLE
//        } else {
//            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
//            adView.starRatingView?.visibility = View.VISIBLE
//        }
//        if (nativeAd.advertiser == null) {
//            adView.advertiserView?.visibility = View.INVISIBLE
//        } else {
//            (adView.advertiserView as TextView).text = nativeAd.advertiser
//            adView.advertiserView?.visibility = View.VISIBLE
//        }
//        ad_loading_View.visibility = View.GONE
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }


}