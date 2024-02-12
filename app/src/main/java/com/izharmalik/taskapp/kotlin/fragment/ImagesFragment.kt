package com.izharmalik.taskapp.kotlin.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.izharmalik.taskapp.kotlin.R
import com.izharmalik.taskapp.kotlin.adapter.ImagesAdapter
import com.izharmalik.taskapp.kotlin.api.ImagesAPI
import com.izharmalik.taskapp.kotlin.api.Photos
import com.izharmalik.taskapp.kotlin.api.RetrofitInstance.retrofit
import com.izharmalik.taskapp.kotlin.databinding.FragmentImagesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ImagesFragment : Fragment(), ImagesAdapter.OnItemClickListener {
    lateinit var binding: FragmentImagesBinding
    private var mInter: InterstitialAd? = null
    var photosList: List<Photos?>? = null
    var sPhotos: Photos? = null
    private lateinit var adapter: ImagesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImagesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded && context != null) {
            getAllImages()
            loadInterstitialAd()
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

        if(sPhotos!=null && isAdded && context!=null){
            val action = ImagesFragmentDirections.toFullImageFragment(sPhotos!!)
            findNavController().navigate(action)
            sPhotos = null
        }

    }

    private fun getAllImages() {
        val apiInterface = retrofit!!.create(ImagesAPI::class.java)
        apiInterface.getImages().enqueue(object : Callback<List<Photos>> {
            override fun onResponse(call: Call<List<Photos>>, response: Response<List<Photos>>) {
                if (isAdded && context != null) {
                    if (response.body()!!.isNotEmpty()) {
                        photosList = response.body()!!
                        setupRecyclerView()
                    } else {
                        Toast.makeText(requireContext(), "List is empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }

            override fun onFailure(call: Call<List<Photos>>, t: Throwable) {
                Log.d("Error", t.message!!)
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), t.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    private fun setupRecyclerView() {
        if (isAdded && context != null) {
            adapter = ImagesAdapter(photosList.orEmpty(), this@ImagesFragment)
            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.imagesRv.layoutManager = layoutManager
            binding.imagesRv.adapter = adapter
        }

    }

    override fun onItemClick(photos: Photos) {
        sPhotos = photos
        showInterstitialAd()
    }

}