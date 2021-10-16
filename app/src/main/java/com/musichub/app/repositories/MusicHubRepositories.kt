package com.musichub.app.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.musichub.app.helpers.PrefManager
import com.musichub.app.helpers.apis.GeniusApiInterface
import com.musichub.app.helpers.apis.SpotifyApiInterface
import com.musichub.app.helpers.apis.OauthClient
import com.musichub.app.helpers.apis.SpotifyApiClient
import com.musichub.app.helpers.room.AppDatabase
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.auth.OAuthResponse
import com.musichub.app.models.constants.ApiConstants
import com.musichub.app.models.constants.SpotifyType
import com.musichub.app.models.genius.SongDetails
import com.musichub.app.models.spotify.*
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.concurrent.thread

class MusicHubRepositories @Inject constructor(
    private val prefManager: PrefManager,
    private val geniusApiInterface: GeniusApiInterface,
    private val appDatabase: AppDatabase
) {

    fun spotifyAuth() : Single<OAuthResponse> {
        val oauth=OauthClient.getClient(ApiConstants.SPOTIFY_OAUTH_BASE_URL).create(SpotifyApiInterface::class.java)
        return oauth.getSpotifyAccessToken(ApiConstants.SPOTIFY_GRANT_TYPE,ApiConstants.SPOTIFY_BASIC_AUTH)
    }
    fun geniusAuth(code:String) : Observable<OAuthResponse> {
        val oauth=OauthClient.getClient(ApiConstants.GENIUS_OAUTH_BASE_URL).create(GeniusApiInterface::class.java)
        return oauth.getGeniusAccessToken(code,ApiConstants.GENIUS_CLIENT_ID,ApiConstants.GENIUS_CLIENT_SECRET,ApiConstants.GENIUS_REDIRECT_URL,ApiConstants.GENIUS_RESPONSE_TYPE,ApiConstants.GENIUS_GRANT_TYPE)
    }

    fun searchArtist(term:String,offset:Int) : Observable<SpotifyArtistResponse> {
        val spotifyApiInterface=SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.spotifySearch(term,SpotifyType.ARTIST_TYPE,offset,50)
    }

    fun searchOnGenius(term: String) : Single<JsonElement>  {
        return geniusApiInterface.search(term)
    }
    fun geniusArtist(id: String) : Single<JsonElement>  {
        return geniusApiInterface.geniusArtist(id)
    }
    fun getAlbums(id:String,groups:String,offset:Int) : Observable<SpotifyAlbum> {
        val spotifyApiInterface=SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.getAlbums(id,groups,50,offset)
    }
    fun getTracks(albumId:String) : Observable<TracksResponse> {
        val spotifyApiInterface=SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.getAlbumTracks(albumId)
    }
    fun saveSpotifyToken(token:String,type:String) {
        prefManager.saveSpotifyToken(token, type)
    }
    fun getSpotifyTrackAlbum(q:String,offset: Int) : Observable<SpotifyAlbumTrack> {
        val spotifyApiInterface=SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.searchTrackAlbum(q,"track,album",offset,50)
    }

    fun getSongDetails(id:String) : Observable<SongDetails> {
        return geniusApiInterface.getSongDetails(id)
    }

    fun getSongBio(id:String) : Single<JsonElement> {
        return geniusApiInterface.getBio(id)
    }

    fun getSpotifyArtist(id: String): Single<SpotifyArtistItem> {
        val spotifyApiInterface =
            SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.getArtist(id)
    }

    fun getSpotifyAlbumById(id: String): Single<AlbumItems> {
        val spotifyApiInterface =
            SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.getAlbumById(id)
    }

    fun getSpotifyAlbumByTrack(q: String, offset: Int): Observable<SpotifyAlbumTrack> {
        val spotifyApiInterface =
            SpotifyApiClient.getClient(prefManager).create(SpotifyApiInterface::class.java)
        return spotifyApiInterface.searchTrackAlbum(q, "track", offset, 50)
    }

    fun followArtist(artistId: String, name: String, image: String) {
        val artist = FollowedArtist(null, artistId, name, image)
        thread {
            Log.d("ArtistFollowed", "Done")
            appDatabase.roomDao().insertArtist(artist)
        }
    }

    fun unfollowArtist(artistId: String) {
        thread {
            Log.d("ArtistFollowed", "Done")
            appDatabase.roomDao().unfollowArtist(artistId)
        }
    }
    fun addToLibrary(albumItems: AlbumItems) {
        thread {
            appDatabase.roomDao().insertAlbum(albumItems)
        }
    }
    fun removeFromLibrary(id: String) {
        thread {
            appDatabase.roomDao().removeAlbum(id)
        }
    }

    fun unfollowAllArtists() {
        thread {
            appDatabase.roomDao().unfollowAllArtist()
        }
    }
    fun clearLibrary() {
        thread {
            appDatabase.roomDao().clearLibrary()
        }
    }



}