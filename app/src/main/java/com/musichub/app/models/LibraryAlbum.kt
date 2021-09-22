package com.musichub.app.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LibraryAlbum(
    @PrimaryKey val id:Int?,
    @ColumnInfo(name = "albumId") val albumId:String,
    @ColumnInfo(name = "name") val name:String,
    @ColumnInfo(name = "image") val image:String
)
