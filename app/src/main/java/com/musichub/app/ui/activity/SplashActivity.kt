package com.musichub.app.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.musichub.app.R
import com.musichub.app.viewmodels.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var baseViewModel: BaseViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        baseViewModel.authSpotify()
        auth = Firebase.auth
        Log.d("onCreate", "called")
        baseViewModel.spotifyToken.observe(this, {
            if (auth.currentUser != null) {
                val albumId = intent.getStringExtra("albumId")
                if (albumId.isNullOrEmpty()) {
                    val segments = intent?.data?.pathSegments
                    if (segments != null) {
                        Log.d("segmentsFound", "found")
                        if (segments[segments.size - 2] == "artist") {
                            Log.d("segmentsFound", "artist")
                            val main = Intent(this, MainActivity::class.java)
                            main.putExtra("artistId", segments[segments.size - 1].toString())
                            startActivity(main)
                            finish()
                        } else if (segments[segments.size - 2] == "album") {
                            Log.d("segmentsFound", segments[segments.size - 1])
                            val main = Intent(this, MainActivity::class.java)
                            main.putExtra("albumId", segments[segments.size - 1].toString())
                            startActivity(main)
                            finish()
                        }
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Log.d("albumIdFound", albumId)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("albumId", albumId)
                    startActivity(intent)
                    finish()
                }
            } else {
                auth.signInAnonymously()
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            val albumId = intent.getStringExtra("albumId")
                            if (albumId.isNullOrEmpty()) {
                                val segments = intent?.data?.pathSegments
                                if (segments != null) {
                                    if (segments!![segments.size - 2] == "artist") {
                                        val main = Intent(this, MainActivity::class.java)
                                        main.putExtra("artistId", segments[segments.size - 1])
                                        startActivity(main)
                                        finish()
                                    } else if (segments[segments.size - 2] == "album") {
                                        val main = Intent(this, MainActivity::class.java)
                                        main.putExtra("albumId", segments[segments.size - 1])
                                        startActivity(main)
                                        finish()
                                    }
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            } else {


                                Log.d("albumIdFound", albumId)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("albumId", albumId)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        })


    }

}