package com.musichub.app.helpers.listeners

import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.SpotifyArtistItem

interface RecyclerViewItemClick {
    fun onItemClick(position:Int)
    fun onAddToLibrary(albumItems: AlbumItems)
    fun onRemoveFromLibrary(albumItems: AlbumItems)
    fun onArtistClick(name: String, id: String, image: String)
}