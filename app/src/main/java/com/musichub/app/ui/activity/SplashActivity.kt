package com.musichub.app.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.musichub.app.R
import com.musichub.app.viewmodels.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    lateinit var baseViewModel:BaseViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        baseViewModel.authSpotify()


        baseViewModel.spotifyToken.observe(this,{
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        })
    }

}