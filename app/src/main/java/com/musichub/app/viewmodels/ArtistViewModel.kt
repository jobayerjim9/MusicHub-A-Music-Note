package com.musichub.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.musichub.app.helpers.room.AppDatabase
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.SpotifyError
import com.musichub.app.models.auth.OAuthResponse
import com.musichub.app.models.constants.SpotifyType
import com.musichub.app.models.genius.ArtistSocialMedia
import com.musichub.app.models.spotify.*
import com.musichub.app.repositories.MusicHubRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltViewModel
class ArtistViewModel @Inject constructor(private val repo: MusicHubRepositories,private val appDatabase: AppDatabase) : ViewModel() {
    val spotifyArtists:MutableLiveData<SpotifyArtists> = MutableLiveData()
    val spotifyAlbumsAll:MutableLiveData<SpotifyAlbum> = MutableLiveData()
    val spotifyAlbumsSingle:MutableLiveData<SpotifyAlbum> = MutableLiveData()
    val spotifyAlbumsFeatured:MutableLiveData<SpotifyAlbum> = MutableLiveData()
    val spotifyAlbumsOnly:MutableLiveData<SpotifyAlbum> = MutableLiveData()
    val isLoading:MutableLiveData<Boolean> = MutableLiveData()
    val artistBio: MutableLiveData<String> = MutableLiveData()
    val artistSocialMedia: MutableLiveData<ArtistSocialMedia> = MutableLiveData()
    val followedArtist : MutableLiveData<List<FollowedArtist>> = MutableLiveData()
    val libraryItems : MutableLiveData<List<AlbumItems>> = MutableLiveData()
    val isFollowed : MutableLiveData<Boolean> = MutableLiveData()

    fun searchArtist(term:String,offset:Int) {
        Log.d("searchArtistViewModel",offset.toString())
        isLoading.postValue(true)
        repo.searchArtist(term,offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyArtistResponse> {
                override fun onSubscribe(d: Disposable) {
                    isLoading.postValue(true)
                }

                override fun onNext(t: SpotifyArtistResponse) {
                    isLoading.postValue(false)
                    spotifyArtists.postValue(t.artists)
                }

                override fun onError(e: Throwable) {

                    isLoading.postValue(false)
                    if (e is HttpException) {
                        try {
                            val body = (e).response()!!.errorBody()
                            val gson = Gson()
                            val adapter = gson.getAdapter(SpotifyError::class.java)
                            val responseBody = adapter.fromJson(body!!.string())
                            if (responseBody?.error?.status==401) {
                                reAuthSpotify(term,SpotifyType.ARTIST_TYPE,offset)
                            }

                        } catch (ioException: IOException) {
                            ioException.printStackTrace()
                        }
                    }
                }

                override fun onComplete() {
                    isLoading.postValue(false)
                }

            })
    }
    fun reAuthSpotify(term: String,type:String,offset: Int) {
        Log.d("reAuthSpotify",offset.toString())

        repo.spotifyAuth()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<OAuthResponse> {
                override fun onSubscribe(d: Disposable) {
                    isLoading.postValue(true)
                }



                override fun onError(e: Throwable) {
                    isLoading.postValue(false)
                }

                override fun onSuccess(t: OAuthResponse) {
                    repo.saveSpotifyToken(t.access_token,t.token_type)
                    if (type == SpotifyType.ARTIST_TYPE) {
                        searchArtist(term,offset)
                    }
                }


            })
    }


    fun searchOnGenius(term: String) {
        repo.searchOnGenius(term)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<JsonElement> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(element: JsonElement) {
                    Log.d("geniusRep",element.asJsonObject.toString())
                    val t=element.asJsonObject
                    val response=t.getAsJsonObject("response")
                    val hits=response.getAsJsonArray("hits")
                    val results=hits.get(0).asJsonObject
                    val result=results.getAsJsonObject("result")
                    val artist=result.getAsJsonObject("primary_artist")
                    val artistId=artist.get("id").toString()
                    getGeniusArtist(artistId)
                }

                override fun onError(e: Throwable) {
                    Log.d("geniusRep",e.message!!)
                }


            })
    }
    fun getAllAlbums(id:String,offset:Int) {
        repo.getAlbums(id,"album,single,appears_on",offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyAlbum> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SpotifyAlbum) {
                    for (album in t.items) {
                        if (album.album_group == "appears_on") {
                            album.artists.add(SpotifyArtistItem(null,null,null,null,id,null,"",null,null,null))
                        }
                    }
                    spotifyAlbumsAll.postValue(t)
                }

                override fun onError(e: Throwable) {
                    val code=(e as HttpException).code()
                    if (code==401) {
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
                                    getAllAlbums(id, offset)
                                }


                            })
                    }
                }

                override fun onComplete() {

                }

            })
    }
    fun getSingleAlbums(id:String,offset:Int) {
        repo.getAlbums(id,"single",offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyAlbum> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SpotifyAlbum) {
                    spotifyAlbumsSingle.postValue(t)
                }

                override fun onError(e: Throwable) {
                    val code=(e as HttpException).code()
                    if (code==401) {
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
                                    getSingleAlbums(id, offset)
                                }


                            })
                    }
                }

                override fun onComplete() {

                }

            })
    }
    fun getFeaturedAlbums(id:String,offset:Int,type:Int = 0) {
        repo.getAlbums(id,"appears_on",offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyAlbum> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SpotifyAlbum) {
                    if (type==100) {
                        for (item in t.items) {
                            item.artists.add(
                                SpotifyArtistItem(
                                    null,
                                    null,
                                    null,
                                    null,
                                    id,
                                    null,
                                    "",
                                    null,
                                    null,
                                    null
                                )
                            )
                        }
                    }
                    spotifyAlbumsFeatured.postValue(t)
                }

                override fun onError(e: Throwable) {
                    val code=(e as HttpException).code()
                    if (code==401) {
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
                                    getFeaturedAlbums(id, offset)
                                }


                            })
                    }
                }

                override fun onComplete() {

                }

            })
    }
    fun getOnlyAlbums(id:String,offset:Int) {
        repo.getAlbums(id,"album",offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyAlbum> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: SpotifyAlbum) {
                    spotifyAlbumsOnly.postValue(t)
                }

                override fun onError(e: Throwable) {
                    val code=(e as HttpException).code()
                    if (code==401) {
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
                                    getOnlyAlbums(id, offset)
                                }


                            })
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun getGeniusArtist(id:String) {
        repo.geniusArtist(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<JsonElement> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: JsonElement) {
                    val json=t.asJsonObject.getAsJsonObject("response").getAsJsonObject("artist").getAsJsonObject("description").getAsJsonObject("dom").getAsJsonArray("children").get(0).asJsonObject.getAsJsonArray("children")
                    val artist=t.asJsonObject.getAsJsonObject("response").getAsJsonObject("artist")
                    val social=ArtistSocialMedia(artist.get("facebook_name").toString().replace("\"",""),artist.get("twitter_name").toString().replace("\"",""),artist.get("instagram_name").toString().replace("\"",""))
                    artistSocialMedia.postValue(social)
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
                    bio=bio.trim()
                    artistBio.postValue(bio)
                }

                override fun onError(e: Throwable) {
                    Log.e("onErrorGeniusArtist",e.message!!)
                }

            })
    }
    fun followArtist(artistId:String,name:String,image:String) {
        repo.followArtist(artistId,name,image)
    }
    fun unfollowArtist(artistId:String) {
        repo.unfollowArtist(artistId)
    }
    fun getFollowedArtist() {
        thread {
            val artists=appDatabase.roomDao().getAllFollowedArtist()
            followedArtist.postValue(artists)
        }
    }
    fun isFollowed(id:String) {
        thread {
            val artists=appDatabase.roomDao().getFollowedArtistById(id)
            if (artists.isEmpty()) {
                isFollowed.postValue(false)
            }
            else {
                isFollowed.postValue(true)
            }

        }
    }
    fun addToLibrary(albumItems: AlbumItems) {
        repo.addToLibrary(albumItems)
    }
    fun removeFromLibrary(albumItems: AlbumItems) {
        repo.removeFromLibrary(albumItems.id!!)
    }
    fun getLibraryItems() {
        thread {
            libraryItems.postValue(appDatabase.roomDao().getAllAlbums())
        }
    }

}
