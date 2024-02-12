package com.izharmalik.taskapp.kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.izharmalik.taskapp.kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var navHostFragment: NavHostFragment? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var navController: NavController? = null
        var bnv_menu: BottomNavigationView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        assignViews()

    }

    private fun assignViews() {
        MobileAds.initialize(this) {}
        bnv_menu = binding.mainBnv
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment?.navController
        navController?.let {
            binding.mainBnv.setupWithNavController(navController!!)
        }
    }


}