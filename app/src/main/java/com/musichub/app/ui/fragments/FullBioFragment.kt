package com.musichub.app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.musichub.app.R
import com.musichub.app.databinding.FragmentFullBioBinding

class FullBioFragment : Fragment() {
    private val args: FullBioFragmentArgs by navArgs()
    lateinit var binding: FragmentFullBioBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_full_bio, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment

        binding.name = args.artistName
        binding.bio = args.bio
        binding.back.setOnClickListener {
            navHostFragment.navController.popBackStack()
        }
    }

}