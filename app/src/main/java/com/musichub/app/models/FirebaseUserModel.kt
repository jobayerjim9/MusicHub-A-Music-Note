package com.musichub.app.models

data class FirebaseUserModel(
    val followedArtists: ArrayList<String> = ArrayList(),
    var notificationToken: String = ""
)
