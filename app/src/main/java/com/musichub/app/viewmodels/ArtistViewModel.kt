package com.musichub.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.musichub.app.helpers.room.AppDatabase
import com.musichub.app.models.FirebaseUserModel
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
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
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
    val isFollowed: MutableLiveData<Boolean> = MutableLiveData()
    val foundArtist: MutableLiveData<SpotifyArtistItem> = MutableLiveData()
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
                    try {
                        Log.d("geniusRep", element.asJsonObject.toString())
                        val t = element.asJsonObject
                        val response = t.getAsJsonObject("response")
                        val hits = response.getAsJsonArray("hits")
                        val results = hits.get(0).asJsonObject
                        val result = results.getAsJsonObject("result")
                        val artist = result.getAsJsonObject("primary_artist")
                        val artistId = artist.get("id").toString()
                        getGeniusArtist(artistId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

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
                                        getAllAlbums(id, offset)
                                    }


                                })
                        }
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
                                        getSingleAlbums(id, offset)
                                    }


                                })
                        }
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
                                        getFeaturedAlbums(id, offset)
                                    }


                                })
                        }
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
                                        getOnlyAlbums(id, offset)
                                    }


                                })
                        }
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
                    val size = t.asJsonObject.getAsJsonObject("response").getAsJsonObject("artist")
                        .getAsJsonObject("description").getAsJsonObject("dom")
                        .getAsJsonArray("children").size()
                    Log.d("sizeBio", size.toString())
                    var bio = ""

                    for (js in 0 until size) {
                        try {
                            if (js > 0) {
                                bio += "\n"
                            }
                            val raw =
                                t.asJsonObject.getAsJsonObject("response").getAsJsonObject("artist")
                                    .getAsJsonObject("description").getAsJsonObject("dom")
                                    .getAsJsonArray("children")
                                    .get(js)
                            if (raw.isJsonObject) {
                                val json = raw.asJsonObject.getAsJsonArray("children")
                                for (i in 0 until json.size()) {
                                    val item = json.get(i)
                                    if (json.get(i).isJsonObject) {
                                        val obj =
                                            json.get(i).asJsonObject.getAsJsonArray("children")
                                                .get(0)
                                        if (obj.isJsonObject) {
                                            val obj1 =
                                                obj.asJsonObject.getAsJsonArray("children").get(0)
                                            bio = "$bio $obj1 "
                                        } else {
                                            bio = "$bio $obj "
                                        }

                                    } else {
                                        bio = "$bio $item "
                                    }
                                    bio = bio.replace(Regex("""["}{/]"""), "")
                                }
                            } else {
                                val json = raw.toString()
                                Log.d("Bio " + js, json)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    bio = bio.replace("  ", " ").replace("  ", " ")
                    bio = bio.trim()
                    Log.d("finalBio", bio)
                    artistBio.postValue(bio)
                    val artist =
                        t.asJsonObject.getAsJsonObject("response").getAsJsonObject("artist")
                    val social = ArtistSocialMedia(
                        artist.get("facebook_name").toString().replace("\"", ""),
                        artist.get("twitter_name").toString().replace("\"", ""),
                        artist.get("instagram_name").toString().replace("\"", "")
                    )
                    artistSocialMedia.postValue(social)
                }

                override fun onError(e: Throwable) {
                    Log.e("onErrorGeniusArtist", e.message!!)
                }

            })
    }

    fun searchArtistSpotify(term: String) {
        repo.searchArtist(term, 0)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SpotifyArtistResponse> {
                override fun onSubscribe(d: Disposable) {

                }


                override fun onError(e: Throwable) {
                    if (e is HttpException) {
                        val code = (e).code()
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
                                        searchArtistSpotify(term)
                                    }


                                })
                        }
                    }
                }

                override fun onNext(t: SpotifyArtistResponse) {
                    for (artist in t.artists.items) {
                        if (artist.name.lowercase().trim() == term.lowercase().trim()) {
                            foundArtist.postValue(artist)
                            break
                        }
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun sortWith(album: ArrayList<AlbumItems>) {
        album.sortWith { p0, p1 ->
            val calender0 = Calendar.getInstance()
            val calender1 = Calendar.getInstance()
            val date0 = p0.formattedDate!!.split("/")
            val date1 = p1.formattedDate!!.split("/")

            calender0.set(Calendar.DAY_OF_MONTH, date0[0].toInt())
            calender1.set(Calendar.DAY_OF_MONTH, date1[0].toInt())
            calender0.set(Calendar.MONTH, date0[1].toInt())
            calender1.set(Calendar.MONTH, date1[1].toInt())
            calender0.set(Calendar.YEAR, date0[2].toInt())
            calender1.set(Calendar.YEAR, date1[2].toInt())
            when {
                calender0.compareTo(calender1) == 1 -> {
                    -1
                }
                calender0.compareTo(calender1) == -1 -> {
                    1
                }
                else -> {
                    0
                }
            }
        }


    }

    fun followArtist(artistId: String, name: String, image: String) {
        repo.followArtist(artistId, name, image)
        val database = FirebaseDatabase.getInstance().reference.child("userInfo")
            .child(Firebase.auth.uid.toString())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(FirebaseUserModel::class.java)
                val artists: ArrayList<String> = ArrayList()
                if (!data?.followedArtists!!.isNullOrEmpty()) {
                    artists.addAll(data.followedArtists)
                }
                artists.add(artistId)
                database.child("followedArtists").setValue(artists)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun unfollowArtist(artistId: String) {
        repo.unfollowArtist(artistId)
        val database = FirebaseDatabase.getInstance().reference.child("userInfo")
            .child(Firebase.auth.uid.toString())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(FirebaseUserModel::class.java)
                val artists: ArrayList<String> = ArrayList()
                if (!data?.followedArtists!!.isNullOrEmpty()) {
                    artists.addAll(data.followedArtists)
                }
                artists.remove(artistId)
                database.child("followedArtists").setValue(artists)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
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
