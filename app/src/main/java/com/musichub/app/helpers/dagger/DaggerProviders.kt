package com.musichub.app.helpers.dagger

import android.content.Context
import androidx.room.Room
import com.musichub.app.helpers.PrefManager
import com.musichub.app.helpers.apis.GeniusApiClient
import com.musichub.app.helpers.apis.GeniusApiInterface
import com.musichub.app.helpers.apis.SpotifyApiInterface
import com.musichub.app.helpers.apis.SpotifyApiClient
import com.musichub.app.helpers.room.AppDatabase
import com.musichub.app.helpers.room.RoomDao
import com.musichub.app.repositories.MusicHubRepositories
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaggerProviders {
    @Provides
    @Singleton
    fun providePrefManager(context: Context) : PrefManager {
        return PrefManager(context)
    }
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context:Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideRepository(prefManager: PrefManager,geniusApiInterface: GeniusApiInterface,appDatabase: AppDatabase): MusicHubRepositories {
            return MusicHubRepositories(prefManager,geniusApiInterface,appDatabase)
    }
    @Provides
    @Singleton
    fun provideGeniusApiInterface() : GeniusApiInterface {
        return GeniusApiClient.getClient().create(GeniusApiInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context) : AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "musichub_database"
        ).build()
    }



}