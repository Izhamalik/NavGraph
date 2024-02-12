package com.izharmalik.taskapp.kotlin.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.izharmalik.taskapp.kotlin.R
import com.izharmalik.taskapp.kotlin.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {
    lateinit var binding: FragmentSplashBinding
    var nativeAd: NativeAd? = null
    private lateinit var countDownTimer: CountDownTimer
    private var mInter: InterstitialAd? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        countDownTimer = object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.splashTime.text = (millisUntilFinished / 1000).toString()
            }
            override fun onFinish() {
                showInterstitialAd()
            }
        }.start()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded && context != null) {
            loadNativeAd()
            loadInterstitialAd()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer.cancel()
    }
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            getString(R.string.inter_ad),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(myAd: InterstitialAd) {
                    super.onAdLoaded(myAd)
                    mInter = myAd
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    mInter = null
                }
            })
    }

    private fun showInterstitialAd() {
        if (mInter != null) {
            mInter?.show(requireActivity())
            mInter?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                       changeFragment()
                    }
                    override fun onAdDismissedFullScreenContent() {
                        mInter = null
                        changeFragment()
                    }
                    override fun onAdShowedFullScreenContent() {
                        mInter = null
                    }
                }
        } else {
            changeFragment()
        }
    }

    private fun changeFragment() {
        if(isAdded && context!=null){
            val action = SplashFragmentDirections.toImageFragment()
            findNavController().navigate(action)
        }

    }

    private fun loadNativeAd() {
        val adLoader = AdLoader.Builder(requireContext(), resources.getString(R.string.native_ad))
            .forNativeAd { ad ->
                nativeAd = ad
                val adView = layoutInflater.inflate(R.layout.native_ad_layout, null) as NativeAdView
                populateNativeAdView(nativeAd!!, adView)
                binding.splashNative.removeAllViews()
                binding.splashNative.addView(adView)
                binding.splashNative.visibility = View.VISIBLE
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    nativeAd = null
                    Toast.makeText(
                        requireContext(),
                        "Failed: ${adError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        try {
            adView.mediaView = adView.findViewById(R.id.ad_media)
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_app_icon)
            adView.priceView = adView.findViewById(R.id.ad_price)
            adView.starRatingView = adView.findViewById(R.id.ad_stars)
            adView.storeView = adView.findViewById(R.id.ad_store)
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

            (adView.headlineView as TextView?)?.text = nativeAd.headline
            adView.mediaView!!.mediaContent = nativeAd.mediaContent

            if (nativeAd.body == null) {
                adView.bodyView!!.visibility = View.INVISIBLE
            } else {
                adView.bodyView!!.visibility = View.VISIBLE
                (adView.bodyView as TextView?)?.text = nativeAd.body
            }
            if (nativeAd.callToAction == null) {
                adView.callToActionView!!.visibility = View.INVISIBLE
            } else {
                adView.callToActionView!!.visibility = View.VISIBLE
                (adView.callToActionView as Button?)?.text = nativeAd.callToAction
            }
            if (nativeAd.getIcon() == null) {
                adView.iconView!!.visibility = View.GONE
            } else {
                (adView.iconView as ImageView?)!!.setImageDrawable(
                    nativeAd.getIcon()!!.drawable
                )
                adView.iconView!!.visibility = View.VISIBLE
            }
            if (nativeAd.getPrice() == null) {
                adView.priceView!!.visibility = View.INVISIBLE
            } else {
                adView.priceView!!.visibility = View.VISIBLE
                (adView.priceView as TextView?)?.text = nativeAd.price
            }
            if (nativeAd.getStore() == null) {
                adView.storeView!!.visibility = View.INVISIBLE
            } else {
                adView.storeView!!.visibility = View.VISIBLE
                (adView.storeView as TextView?)?.text = nativeAd.store
            }
            if (nativeAd.getStarRating() == null) {
                adView.starRatingView!!.visibility = View.INVISIBLE
            } else {
                nativeAd.starRating?.toFloat()?.let {
                    (adView.starRatingView as RatingBar?)
                        ?.setRating(it)
                }
                adView.starRatingView!!.visibility = View.VISIBLE
            }
            if (nativeAd.getAdvertiser() == null) {
                adView.advertiserView!!.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView?)?.text = nativeAd.advertiser
                adView.advertiserView!!.visibility = View.VISIBLE
            }
            adView.setNativeAd(nativeAd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}