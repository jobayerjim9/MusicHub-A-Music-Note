package com.musichub.app.viewmodels

import androidx.lifecycle.ViewModel
import com.musichub.app.repositories.MusicHubRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: MusicHubRepositories) : ViewModel() {
    fun unFollowAllArtists() {
        repo.unfollowAllArtists()
    }
    fun clearLibrary() {
        repo.clearLibrary()
    }
}