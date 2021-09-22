package com.musichub.app.models

import com.google.gson.annotations.SerializedName

data class SpotifyError(
    @SerializedName("error") val error:Error
)
data class Error(
    @SerializedName("status") val status:Int,
    @SerializedName("message") val message:String
)
