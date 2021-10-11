package com.musichub.app.helpers.apis

import com.google.gson.JsonElement
import com.musichub.app.models.auth.OAuthResponse
import com.musichub.app.models.genius.SongDetails
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface GeniusApiInterface {
    @FormUrlEncoded
    @POST("token")
    fun getGeniusAccessToken(@Field("code") code:String,@Field("client_id") client_id:String,@Field("client_secret") client_secret:String,@Field("redirect_uri") redirect_uri:String,@Field("response_type") response_type:String,@Field("grant_type") grant_type:String) : Observable<OAuthResponse>

    @GET("search")
    fun search(@Query("q") q:String) : Single<JsonElement>

    @GET("search")
    fun searchSong(@Query("q") q:String) : Single<JsonElement>

    @GET("artists/{id}")
    fun geniusArtist(@Path("id") id: String): Single<JsonElement>

    @GET("songs/{id}")
    fun getBio(@Path("id") id: String): Single<JsonElement>

    @GET("songs/{id}")
    fun getSongDetails(@Path("id") id: String): Observable<SongDetails>

    @GET("search")
    fun searchTrack(@Query("q") q: String): Single<JsonElement>
}