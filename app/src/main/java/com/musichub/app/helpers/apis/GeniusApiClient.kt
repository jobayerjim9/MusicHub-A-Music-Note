package com.musichub.app.helpers.apis

import com.google.gson.GsonBuilder
import com.musichub.app.helpers.PrefManager
import com.musichub.app.models.constants.ApiConstants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GeniusApiClient {
    private const val BASE_URL="https://api.genius.com/"
    lateinit var retrofit: Retrofit
    fun getClient() : Retrofit {
        val okHttpClient = OkHttpClient().newBuilder()
            .readTimeout(160, TimeUnit.SECONDS)
            .writeTimeout(160, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val builder: Request.Builder = originalRequest.newBuilder().addHeader("Authorization",ApiConstants.GENIUS_ACCESS_TOKEN)
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