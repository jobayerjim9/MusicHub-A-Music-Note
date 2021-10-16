package com.musichub.app.helpers.apis

import com.musichub.app.models.auth.OAuthResponse
import com.musichub.app.models.spotify.*
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface SpotifyApiInterface {
    @FormUrlEncoded
    @POST("token")
    fun getSpotifyAccessToken(@Field("grant_type") grant_type:String, @Header("Authorization") token:String) : Single<OAuthResponse>

    @GET("search")
    fun spotifySearch(@Query("q") q:String,@Query("type") type:String,@Query("offset") offset:Int,@Query("limit") limit:Int) : Observable<SpotifyArtistResponse>

    @GET("artists/{id}/albums")
    fun getAlbums(@Path("id") id:String,@Query("include_groups") include_groups:String,@Query("limit") limit:Int,@Query("offset") offset:Int) : Observable<SpotifyAlbum>

    @GET("albums/{id}/tracks")
    fun getAlbumTracks(@Path("id") id:String) : Observable<TracksResponse>

    @GET("search")
    fun searchTrackAlbum(@Query("q") q:String,@Query("type") type:String,@Query("offset") offset:Int,@Query("limit") limit:Int) : Observable<SpotifyAlbumTrack>

    @GET("artists/{id}")
    fun getArtist(@Path("id") id:String) : Single<SpotifyArtistItem>

    @GET("albums/{id}")
    fun getAlbumById(@Path("id") id:String) : Single<AlbumItems>


}