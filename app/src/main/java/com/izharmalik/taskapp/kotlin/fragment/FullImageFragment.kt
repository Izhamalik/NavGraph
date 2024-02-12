package com.izharmalik.taskapp.kotlin.fragment

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
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
import com.izharmalik.taskapp.kotlin.api.Photos
import com.izharmalik.taskapp.kotlin.databinding.FragmentFullImageBinding
import java.io.File
import java.io.FileOutputStream


class FullImageFragment : Fragment() {
    lateinit var binding: FragmentFullImageBinding
    var nativeAd: NativeAd? = null
    var PERMISSION_REQUEST_CODE = 123
    private val args: FullImageFragmentArgs by navArgs()
    private var mInter: InterstitialAd? = null
    var selectedPhoto: Photos? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFullImageBinding.inflate(layoutInflater)
        try {
            selectedPhoto = args.photos
        }catch (e:Exception){
            e.printStackTrace()
        }


        binding.apply {
            if(selectedPhoto!=null){
                Glide.with(requireContext())
                    .load(selectedPhoto?.thumbnailUrl)
                    .into(binding.fullImage)
                fullName.text = selectedPhoto?.title
            }

            fullShare.setOnClickListener {
                if (checkStoragePermissions()) {
                    shareImage()
                } else {
                    requestPermissions()
                }
            }
            fullNext.setOnClickListener {
                showInterstitialAd()
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded && context != null) {
            loadInterstitialAd()
            loadNativeAd()
        }
    }
    private fun shareImage() {
        try {
            val drawable = binding.fullImage.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val imageUri = saveImage(bitmap)

                if (imageUri != null) {
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "image/jpg"
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                    startActivity(
                        Intent.createChooser(
                            sharingIntent,
                            "Share image via..."
                        )
                    )
                } else {
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Invalid image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveImage(bitmap: Bitmap): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "api_image.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val contentResolver = requireContext().contentResolver
            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            val outputStream = imageUri?.let { contentResolver.openOutputStream(it) }
            outputStream?.use { it ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            imageUri
        } else {
            val picturesDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val savedImageFile = File(picturesDirectory, "api_image.jpg")
            picturesDirectory.mkdirs()

            val outputStream = FileOutputStream(savedImageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(savedImageFile.absolutePath),
                arrayOf("image/jpg"),
                null
            )

            Uri.fromFile(savedImageFile)
        }
        }

    private fun checkStoragePermissions(): Boolean {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        return storagePermission
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                ), PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), PERMISSION_REQUEST_CODE
            )
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                shareImage()
            } else {
                Toast.makeText(requireContext(), "Permission not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
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
            val action = FullImageFragmentDirections.toExitFragment()
            findNavController().navigate(action)
        }

    }

    private fun loadNativeAd() {
        try {
            val adLoader = AdLoader.Builder(requireContext(), resources.getString(R.string.native_ad))
                .forNativeAd { ad ->
                    nativeAd = ad
                    val adView = layoutInflater.inflate(R.layout.native_ad_layout, null) as NativeAdView
                    populateNativeAdView(nativeAd!!, adView)
                    binding.fullNative.removeAllViews()
                    binding.fullNative.addView(adView)
                    binding.fullNative.visibility = View.VISIBLE
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
        }catch (e:Exception){
            e.printStackTrace()
        }

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