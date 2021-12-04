package com.musichub.app.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.musichub.app.R
import com.musichub.app.databinding.ActivityMainBinding
import com.musichub.app.models.constants.ApiConstants
import com.musichub.app.viewmodels.ArtistViewModel
import com.musichub.app.viewmodels.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var baseViewModel: BaseViewModel
    lateinit var binding: ActivityMainBinding
    lateinit var navHostFragment: NavHostFragment
    lateinit var navOptions:NavOptions
    lateinit var artistViewModel: ArtistViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        initView()
    }

    private fun initView() {
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        artistViewModel = ViewModelProvider(this).get(ArtistViewModel::class.java)
        val auth = Firebase.auth
        Log.d("firebaseUserUid", auth.uid!!)
        navOptions = NavOptions.Builder()
            .setEnterAnim(android.R.anim.slide_in_left)
            .setExitAnim(android.R.anim.slide_out_right)
            .build()
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
//        binding.button.setOnClickListener {
//            val intent= Intent(this,WebViewActivity::class.java)
//            intent.putExtra("url",ApiConstants.GENIUS_OAUTH_URL)
//            startActivity(intent)
//        }
        baseViewModel.updateNotificationToken()
        baseViewModel.spotifyArtistItem.observe(this, {
            if (it != null) {
                val bundle = Bundle()
                bundle.putString("name", it.name)
                bundle.putString("artistId", it.id)
                if (it.images!!.isNotEmpty()) {
                    bundle.putString("image", it.images!![0].url)
                } else {
                    bundle.putString("image", "")
                }
                binding.loading = false
                navHostFragment.navController.navigate(
                    R.id.artistDetailsFragment,
                    bundle,
                    navOptions
                )
                baseViewModel.spotifyArtistItem.postValue(null)
            }
        })
        baseViewModel.albumItem.observe(this, {
            if (it != null) {
                val bundle = Bundle()
                bundle.putParcelable("albumIten", it)
                bundle.putString("artistName", it.artists[0].name)
                binding.loading = false
                navHostFragment.navController.navigate(
                    R.id.albumDetailsFragment,
                    bundle,
                    navOptions
                )
                baseViewModel.albumItem.postValue(null)
            }
        })
        val albumId = intent.getStringExtra("albumId")
        if (!albumId.isNullOrEmpty()) {
            Log.d("albumIdFound", albumId)
            baseViewModel.getAlbumById(albumId)
        }
        val artistId = intent.getStringExtra("artistId")
        if (!artistId.isNullOrEmpty()) {
            baseViewModel.getSpotifyArtistById(artistId)
        }


    }
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        val segments = intent?.data?.pathSegments
//        if (segments!![segments.size-2] == "artist") {
//            binding.loading=true
//            baseViewModel.getSpotifyArtistById(segments!![segments.size-1])
//        }
//        else if (segments!![segments.size-2] == "album") {
//            binding.loading=true
//            baseViewModel.getAlbumById(segments!![segments.size-1])
//        }
//        //navHostFragment.navController.navigate(Uri.parse(intent?.data.toString()))
//
//    }

}