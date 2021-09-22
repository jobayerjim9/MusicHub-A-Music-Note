package com.musichub.app.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.musichub.app.models.auth.OAuthResponse
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.SpotifyArtistItem
import com.musichub.app.repositories.MusicHubRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(private val repo: MusicHubRepositories) : ViewModel() {
    val spotifyToken : MutableLiveData<OAuthResponse> = MutableLiveData()
    var mDisposable: CompositeDisposable = CompositeDisposable()
    val albumItem : MutableLiveData<AlbumItems> = MutableLiveData()
    val spotifyArtistItem: MutableLiveData<SpotifyArtistItem> = MutableLiveData()
    fun authSpotify() {
        repo.spotifyAuth()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<OAuthResponse> {
                override fun onSubscribe(d: Disposable) {

                }


                override fun onError(e: Throwable) {

                }

                override fun onSuccess(t: OAuthResponse) {
                    spotifyToken.postValue(t)
                    repo.saveSpotifyToken(t.access_token,t.token_type)

                }


            })
    }

    fun geniusAuth(code:String) {
        repo.geniusAuth(code)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OAuthResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: OAuthResponse) {
                    Log.d("geniusOAuth",t.access_token)
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }

            })

    }
    fun getAlbumById(id:String) {
        repo.getSpotifyAlbumById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<AlbumItems> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: AlbumItems) {
                    albumItem.postValue(t)
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
                                    getAlbumById(id)
                                }


                            })
                    }
                }

            })
    }
    fun getSpotifyArtistById(id:String) {
        repo.getSpotifyArtist(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :SingleObserver<SpotifyArtistItem> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: SpotifyArtistItem) {
                    spotifyArtistItem.postValue(t)
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
                                    getSpotifyArtistById(id)
                                }


                            })
                    }
                }

            })
    }

}