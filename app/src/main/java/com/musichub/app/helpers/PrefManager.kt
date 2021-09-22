package com.musichub.app.helpers

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    private val spotifyTokenKey="spotifyToken"
    private val sharedPreferences=context.getSharedPreferences("musichub",Context.MODE_PRIVATE)

    fun saveSpotifyToken(token:String,type:String) {
        sharedPreferences.edit().putString(spotifyTokenKey, "$type $token").apply()
    }
    fun getSpotifyToken() : String? {
        return sharedPreferences.getString(spotifyTokenKey,"")
    }




}