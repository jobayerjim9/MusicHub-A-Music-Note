package com.musichub.app.models.spotify

import com.google.gson.annotations.SerializedName

data class SpotifyArtistResponse(
    @SerializedName("artists") val artists: SpotifyArtists
)
