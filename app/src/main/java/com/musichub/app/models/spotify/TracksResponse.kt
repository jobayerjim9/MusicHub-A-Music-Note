package com.musichub.app.models.spotify

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class TracksResponse(
    @SerializedName("items") val items:ArrayList<TrackItems>,
    @SerializedName("limit") val limit:Int,
    @SerializedName("offset") val offset:Int,
    @SerializedName("total") val total:Int,
)
@Entity
data class TrackItems (
    @PrimaryKey val uid:Int,
    @ColumnInfo(name = "album") @SerializedName("album") val album:AlbumItems,
    @ColumnInfo(name = "artists") @SerializedName("artists") val artists:ArrayList<SpotifyArtistItem>,
    @ColumnInfo(name = "disc_number") @SerializedName("disc_number") val disc_number:Int,
    @ColumnInfo(name = "duration_ms") @SerializedName("duration_ms") val duration_ms:Long,
    @ColumnInfo(name = "explicit") @SerializedName("explicit") val explicit:Boolean,
    @ColumnInfo(name = "external_urls") @SerializedName("external_urls") val external_urls:ExternalUrl,
    @ColumnInfo(name = "href") @SerializedName("href") val href:String,
    @ColumnInfo(name = "id") @SerializedName("id") val id:String,
    @ColumnInfo(name = "is_local") @SerializedName("is_local") val is_local:Boolean,
    @ColumnInfo(name = "name") @SerializedName("name") val name:String,
    @ColumnInfo(name = "preview_url") @SerializedName("preview_url") val preview_url:String?,
    @ColumnInfo(name = "track_number") @SerializedName("track_number") val track_number:Int,
    @ColumnInfo(name = "type") @SerializedName("type") val type:String,
    @ColumnInfo(name = "uri") @SerializedName("uri") val uri:String

)
