package com.musichub.app.helpers.apis

import com.google.gson.GsonBuilder
import com.musichub.app.helpers.PrefManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SpotifyApiClient {
    private const val BASE_URL="https://api.spotify.com/v1/"
    lateinit var retrofit: Retrofit
    fun getClient(prefManager: PrefManager) : Retrofit {
        val token=prefManager.getSpotifyToken().toString()
        //val interceptor = HttpLoggingInterceptor()
       // interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient = OkHttpClient().newBuilder()
            .readTimeout(160, TimeUnit.SECONDS)
            .writeTimeout(160, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val builder: Request.Builder = originalRequest.newBuilder().addHeader("Authorization",token)
                val newRequest: Request = builder.build()
                    chain.proceed(newRequest)
            }).build()

        val gson = GsonBuilder()
            .setLenient()
            .create()


            retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit
    }

}