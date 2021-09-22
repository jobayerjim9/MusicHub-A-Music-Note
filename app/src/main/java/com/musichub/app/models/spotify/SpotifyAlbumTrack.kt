package com.musichub.app.models.spotify

import com.google.gson.annotations.SerializedName

data class SpotifyAlbumTrack(
    @SerializedName("albums") val albums:SpotifyAlbum,
    @SerializedName("tracks") val tracks:TracksResponse
)
