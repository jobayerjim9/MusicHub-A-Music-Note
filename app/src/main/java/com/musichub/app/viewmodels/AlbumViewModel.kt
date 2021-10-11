package com.musichub.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.CharMatcher
import com.google.gson.JsonElement
import com.musichub.app.helpers.room.AppDatabase
import com.musichub.app.models.auth.OAuthResponse
import com.musichub.app.models.genius.SongShort
import com.musichub.app.models.spotify.*
import com.musichub.app.repositories.MusicHubRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltViewModel
class AlbumViewModel @Inject constructor(private val repo: MusicHubRepositories) : ViewModel() {
    val tracks:MutableLiveData<TracksResponse> = MutableLiveData()
    val albums:MutableLiveData<SpotifyAlbum> = MutableLiveData()
    val loading : MutableLiveData<Boolean> = MutableLiveData()
    val songShort : MutableLiveData<SongShort> = MutableLiveData()

    fun getAlbumTracks(albumId:String) {
        repo.getTracks(albumId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<TracksResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: TracksResponse) {
                    tracks.postValue(t)

                }

                override fun onError(e: Throwable) {
                    if (e is HttpException) {
                        val code = (e as HttpException).code()
                        if (code == 401) {
                            repo.spotifyAuth()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<OAuthResponse> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {

                                    }

                                    override fun onSuccess(t: OAuthResponse) {
                                        repo.saveSpotifyToken(t.access_token, t.token_type)
                                        getAlbumTracks(albumId)
                                    }


                                })
                        }
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun searchTrackAlbum(q:String,offset:Int) {
        loading.postValue(true)
        repo.getSpotifyTrackAlbum(q,offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyAlbumTrack> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SpotifyAlbumTrack) {
                    tracks.postValue(t.tracks)
                    albums.postValue(t.albums)
                    loading.postValue(false)
                }

                override fun onError(e: Throwable) {
                    loading.postValue(false)
                    if (e is HttpException) {
                        val code = (e as HttpException).code()
                        if (code == 401) {
                            repo.spotifyAuth()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<OAuthResponse> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {

                                    }

                                    override fun onSuccess(t: OAuthResponse) {
                                        repo.saveSpotifyToken(t.access_token, t.token_type)
                                        searchTrackAlbum(q, offset)
                                    }


                                })
                        }
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun searchTrackOnGenius(term: String,artistName:String) {
        var finalterm = term.substringBefore("(").trim()
        finalterm = finalterm.replace(".", "").replace("-", "").replace(";", "").replace("  ", " ")
        repo.searchOnGenius("$finalterm $artistName")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<JsonElement> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: JsonElement) {
                    var found = false
                    var name = ""
                    var artist=""
                    var id=""
                    val hits=t.asJsonObject.getAsJsonObject("response").getAsJsonArray("hits")
                    Log.d("searched", finalterm + "   " + artistName)
                    for (i in 0 until hits.size()) {
                        val song = hits[i].asJsonObject.getAsJsonObject("result")
                        name = song?.get("title").toString().replace("\"", "")
                        artist = song.getAsJsonObject("primary_artist").get("name").toString()
                            .replace("\"", "")
                        id = song.get("id").toString().replace("\"", "")
                        Log.d("found", name.trim() + " " + artist.trim())
                        if (artist.lowercase().trim().contains(artistName.lowercase().trim())) {
                            found = true
                            break
                        }

                    }
                    if (found) {
                        Log.d("trackOnGenius", "$name $artist")
                        Log.d("trackId", id)
                    }
                    else {
                        Log.d("trackOnGenius","Not Found")
                    }
                    val song=SongShort(found,name,id)
                    songShort.postValue(song)

                }

                override fun onError(e: Throwable) {

                }

            })
    }

}