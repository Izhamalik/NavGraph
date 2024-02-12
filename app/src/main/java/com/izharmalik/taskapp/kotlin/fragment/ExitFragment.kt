package com.izharmalik.taskapp.kotlin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.izharmalik.taskapp.kotlin.MainActivity.Companion.navController
import com.izharmalik.taskapp.kotlin.R
import com.izharmalik.taskapp.kotlin.databinding.FragmentExitBinding

class ExitFragment : Fragment() {
    lateinit var binding : FragmentExitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExitBinding.inflate(layoutInflater)
        binding.apply {
            exitBack.setOnClickListener {
                val firstFragmentId = R.id.splashFragment
                navController?.popBackStack(firstFragmentId, false)
            }
            exitApp.setOnClickListener {
                requireActivity().finishAndRemoveTask()
            }
        }
        return  binding.root
    }
}