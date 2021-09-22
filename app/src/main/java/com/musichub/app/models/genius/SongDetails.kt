package com.musichub.app.models.genius

import com.google.gson.annotations.SerializedName

data class SongDetails(
    @SerializedName("response") val response:Response
)
data class Response(
    @SerializedName("song") val song: Song
)
data class Song(
    @SerializedName("id") val id:Long,
    @SerializedName("custom_performances") val custom_performances:ArrayList<CustomPerformance>,
    @SerializedName("primary_artist") val primary_artist:Artist,
    @SerializedName("producer_artists") val producer_artists:ArrayList<Artist>,
    @SerializedName("writer_artists") val writer_artists:ArrayList<Artist>,
    @SerializedName("title") val title:String,
    @SerializedName("media") val media:ArrayList<Media>,
)
data class CustomPerformance(
    @SerializedName("label") val label:String,
    @SerializedName("artists") val artists:ArrayList<Artist>
)
data class Artist(
    @SerializedName("id") val id:Long,
    @SerializedName("name") val name:String,
    @SerializedName("url") val url:String,
)
data class Media(
    @SerializedName("provider") val provider:String,
    @SerializedName("type") val type:String,
    @SerializedName("url") val url:String,
)

