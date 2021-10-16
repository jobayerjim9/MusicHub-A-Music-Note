package com.musichub.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.musichub.app.models.genius.Song
import com.musichub.app.models.genius.SongDetails
import com.musichub.app.models.spotify.SpotifyAlbumTrack
import com.musichub.app.models.spotify.SpotifyArtistItem
import com.musichub.app.models.spotify.SpotifyArtistResponse
import com.musichub.app.models.spotify.TrackItems
import com.musichub.app.repositories.MusicHubRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(private val repo: MusicHubRepositories) : ViewModel() {
    val songBio: MutableLiveData<String> = MutableLiveData()
    val song: MutableLiveData<Song> = MutableLiveData()
    val foundSample: MutableLiveData<TrackItems> = MutableLiveData()
    fun getSongDetails(id:String) {
        repo.getSongDetails(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SongDetails> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SongDetails) {
                    song.postValue(t.response.song)
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }

            })

    }


    fun getSongBio(id: String) {
        repo.getSongBio(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<JsonElement> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: JsonElement) {
                    val json=t.asJsonObject.getAsJsonObject("response").getAsJsonObject("song").getAsJsonObject("description").getAsJsonObject("dom").getAsJsonArray("children").get(0).asJsonObject.getAsJsonArray("children")
                    var bio=""
                    for (i in 0 until json.size()) {
                        val item=json.get(i)
                        if (json.get(i).isJsonObject) {
                            val obj=json.get(i).asJsonObject.getAsJsonArray("children").get(0)
                            if (obj.isJsonObject) {
                                val obj1=obj.asJsonObject.getAsJsonArray("children").get(0)
                                bio= "$bio $obj1 "
                            }
                            else {
                                bio= "$bio $obj "
                            }

                        }
                        else {
                            bio= "$bio $item "
                        }
                        bio=bio.replace(Regex("""["}{/]"""), "")
                    }
                    bio=bio.replace("  "," ").replace("  "," ")
                    bio = bio.trim()
                    songBio.postValue(bio)
                }

                override fun onError(e: Throwable) {

                }

            })
    }

    fun getAlbumByTrack(name: String, artistName: String) {
        val search = name.replace("by", "").replace("  ", " ").substringBefore("(").trim()
        Log.d("searchingTrack", search)

        repo.getSpotifyAlbumByTrack(search, 0)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyAlbumTrack> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SpotifyAlbumTrack) {
                    for (tracks in t.tracks.items) {
                        for (artist in tracks.artists) {
                            if (artist.name.lowercase().trim() == artistName.lowercase().trim()) {
                                Log.d("foundAlbum", "found")
                                foundSample.postValue(tracks)
                                break
                            }
                        }
                    }
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }

            })
    }
}