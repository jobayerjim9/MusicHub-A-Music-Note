package com.musichub.app.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.musichub.app.BuildConfig
import com.musichub.app.R
import com.musichub.app.databinding.FragmentSettingsBinding
import com.musichub.app.viewmodels.AlbumViewModel
import com.musichub.app.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_settings, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        viewModel= ViewModelProvider(this).get(SettingsViewModel::class.java)
        binding.unfollowAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are You Sure?")
                .setMessage("This Action Cannot be undone!")
                .setPositiveButton("Yes") {dialog,which ->
                    viewModel.unFollowAllArtists()
                    dialog.dismiss()
                    Toast.makeText(requireContext(),"All Artists Unfollowed!",Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") {dialog,which ->
                    dialog.dismiss()
                }
                .show()
        }
        binding.clearLibrary.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are You Sure?")
                .setMessage("This Action Cannot be undone!")
                .setPositiveButton("Yes") {dialog,which ->
                    viewModel.clearLibrary()
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Library Cleared!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") {dialog,which ->
                    dialog.dismiss()
                }
                .show()
        }
        binding.rate.setOnClickListener {
                val uri =
                    Uri.parse("https://play.google.com/store/apps/details?id="+BuildConfig.APPLICATION_ID)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
        }
        binding.share.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey Download MusicHub From Google Play:- https://play.google.com/store/apps/details?id="+BuildConfig.APPLICATION_ID);
            startActivity(Intent.createChooser(shareIntent,"Share via"))
        }
        binding.report.setOnClickListener {
            val uri =
                Uri.parse("https://musichub.userecho.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        binding.tip.setOnClickListener {
            val items = arrayOf("Snack Tip", "Lunch Tip", "Big Tip","Cancel")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tip")
                .setItems(items) { dialog, which ->
                    //dialog.dismiss()
                }
                .show()
        }


    }

}