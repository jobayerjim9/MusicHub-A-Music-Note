package com.musichub.app.models.spotify

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SpotifyAlbum(
    @SerializedName("items") var items:ArrayList<AlbumItems>,
    @SerializedName("offset") val offset:Int,
)

@Entity
@Parcelize
data class AlbumItems (
    @PrimaryKey val uid:Int?,
    @ColumnInfo(name = "album_group") @SerializedName("album_group") val album_group:String?,
    @ColumnInfo(name = "album_type") @SerializedName("album_type") val album_type:String?,
    @ColumnInfo(name = "artists") @SerializedName("artists") val artists:ArrayList<SpotifyArtistItem>,
    @ColumnInfo(name = "external_urls") @SerializedName("external_urls") val external_urls:ExternalUrl?,
    @ColumnInfo(name = "id") @SerializedName("id") val id:String?,
    @ColumnInfo(name = "images") @SerializedName("images") val images:ArrayList<Image>?,
    @ColumnInfo(name = "name") @SerializedName("name") val name:String?,
    @ColumnInfo(name = "release_date") @SerializedName("release_date") val release_date:String?,
    @ColumnInfo(name = "release_date_precision") @SerializedName("release_date_precision") val release_date_precision:String?,
    @ColumnInfo(name = "total_tracks")  @SerializedName("total_tracks") val total_tracks:Int?,
    @ColumnInfo(name = "type") @SerializedName("type") val type:String?,
    @ColumnInfo(name = "uri") @SerializedName("uri") val uri:String?,
    @ColumnInfo(name = "inLibrary")  var inLibrary:Boolean,
) : Parcelable

