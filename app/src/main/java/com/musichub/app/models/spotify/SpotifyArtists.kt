package com.musichub.app.models.spotify

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class SpotifyArtists(
    @SerializedName("href") val href:String,
    @SerializedName("items") val items:ArrayList<SpotifyArtistItem>,
    @SerializedName("limit") val limit:Int,
    @SerializedName("offset") val offset:Int,
    @SerializedName("total") val total:Int
)
@Entity
@Parcelize
data class SpotifyArtistItem(
    @PrimaryKey val uid:Int?,
    @ColumnInfo(name = "external_urls") @SerializedName("external_urls") val external_urls:ExternalUrl?,
    @ColumnInfo(name = "genres") @SerializedName("genres") val genres:ArrayList<String>?,
    @ColumnInfo(name = "href") @SerializedName("href") val href:String?,
    @ColumnInfo(name = "id") @SerializedName("id") val id:String,
    @ColumnInfo(name = "images") @SerializedName("images") val images:ArrayList<Image>?,
    @ColumnInfo(name = "name") @SerializedName("name") val name:String,
    @ColumnInfo(name = "popularity") @SerializedName("popularity") val popularity:Int?,
    @ColumnInfo(name = "type") @SerializedName("type") val type:String?,
    @ColumnInfo(name = "uri") @SerializedName("uri") val uri:String?

) : Parcelable

@Parcelize
data class ExternalUrl (
    @SerializedName("spotify") val spotify:String
) : Parcelable
data class Follower (
    @SerializedName("href") val href:String,
    @SerializedName("total") val total:Int
)
@Parcelize
data class Image (
    @SerializedName("height") val href:Int,
    @SerializedName("url") val url:String,
    @SerializedName("width") val width:Int
) : Parcelable
