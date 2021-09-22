package com.musichub.app.helpers.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.TrackItems

@Dao
interface RoomDao {
    @Insert
    fun insertArtist(followedArtist: FollowedArtist)

    @Query("SELECT * FROM FollowedArtist")
    fun getAllFollowedArtist() : List<FollowedArtist>

    @Query("SELECT * FROM FollowedArtist WHERE artistId=:followId")
    fun getFollowedArtistById(followId:String) : List<FollowedArtist>

    @Query("DELETE FROM FollowedArtist WHERE artistId=:followId")
    fun unfollowArtist(followId: String)

    @Query("DELETE FROM FollowedArtist")
    fun unfollowAllArtist()

    @Insert
    fun insertAlbum(albumItems: AlbumItems)

    @Query("DELETE FROM AlbumItems WHERE id=:id")
    fun removeAlbum(id:String)

    @Query("SELECT * FROM AlbumItems")
    fun getAllAlbums() : List<AlbumItems>

    @Query("DELETE FROM AlbumItems")
    fun clearLibrary()
    @Insert
    fun insertTracks(albumItems: TrackItems)

    @Query("SELECT * FROM TrackItems")
    fun getAllTracks() : List<TrackItems>




}