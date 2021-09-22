package com.musichub.app.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FollowedArtist(
    @PrimaryKey val id:Int?,
    @ColumnInfo(name = "artistId") val artistId:String,
    @ColumnInfo(name = "name") val name:String,
    @ColumnInfo(name = "image") val image:String
)
