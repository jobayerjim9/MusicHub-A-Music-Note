package com.musichub.app.helpers.listeners

import com.musichub.app.models.spotify.AlbumItems

interface RecyclerViewItemClick {
    fun onItemClick(position:Int)
    fun onAddToLibrary(albumItems: AlbumItems)
    fun onRemoveFromLibrary(albumItems: AlbumItems)

}