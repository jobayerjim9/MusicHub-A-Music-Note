package com.musichub.app.helpers.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.ExternalUrl
import com.musichub.app.models.spotify.Image
import com.musichub.app.models.spotify.SpotifyArtistItem
import java.util.ArrayList

class Converters {
    companion object {
        @JvmStatic @TypeConverter
        fun fromString(value: String?): ArrayList<String>? {
            val listType = object : TypeToken<ArrayList<String>>() {}.type
            return Gson().fromJson(value, listType)
        }

        @JvmStatic @TypeConverter
        fun fromArrayList(list: ArrayList<String>): String {
            val gson = Gson()
            return gson.toJson(list)
        }

        @JvmStatic @TypeConverter
        fun spotifyArtistItemToString(cartItems: ArrayList<SpotifyArtistItem>): String {
            val gson = Gson()
            return gson.toJson(cartItems)
        }

        @JvmStatic @TypeConverter
        fun stringToSpotifyArtistItem(value: String?): ArrayList<SpotifyArtistItem> {
            val listType = object : TypeToken<ArrayList<SpotifyArtistItem>>() {}.type
            return Gson().fromJson<ArrayList<SpotifyArtistItem>>(value, listType)
        }

        @JvmStatic @TypeConverter
        fun imagesToString(cartItems: ArrayList<Image>): String {
            val gson = Gson()
            return gson.toJson(cartItems)
        }

        @JvmStatic @TypeConverter
        fun stringToImages(value: String): ArrayList<Image> {
            val listType = object : TypeToken<ArrayList<Image>>() {}.type
            return Gson().fromJson<ArrayList<Image>>(value, listType)
        }

        @JvmStatic @TypeConverter
        fun externalUrlToSting(externalUrl: ExternalUrl): String {
            val gson = Gson()
            return gson.toJson(externalUrl)
        }

        @JvmStatic @TypeConverter
        fun stringToExternalUrl(value: String?): ExternalUrl {
            val listType = object : TypeToken<ExternalUrl>() {}.type
            return Gson().fromJson(value, listType)
        }

        @JvmStatic @TypeConverter
        fun albumItemsToSting(albumItems: AlbumItems): String {
            val gson = Gson()
            return gson.toJson(albumItems)
        }

        @JvmStatic @TypeConverter
        fun stringToAlbumItems(value: String): AlbumItems {
            val listType = object : TypeToken<AlbumItems>() {}.type
            return Gson().fromJson(value, listType)
        }

    }
}