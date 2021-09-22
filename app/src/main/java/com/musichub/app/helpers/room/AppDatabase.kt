package com.musichub.app.helpers.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.TrackItems

@Database(entities = [FollowedArtist::class, AlbumItems::class, TrackItems::class], version=1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao() : RoomDao
}