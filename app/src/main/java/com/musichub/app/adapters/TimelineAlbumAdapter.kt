package com.musichub.app.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemAlbumsBinding
import com.musichub.app.databinding.ItemTimelineAlbumsBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.spotify.ArtistShort
import com.musichub.app.models.spotify.AlbumItems

class TimelineAlbumAdapter(private val context:Context, private val items:ArrayList<AlbumItems>, private val artists:ArrayList<FollowedArtist>,private val libraryItems: ArrayList<AlbumItems>,private val listener:RecyclerViewItemClick) : RecyclerView.Adapter<TimelineAlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_timeline_albums,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]
        if (items[position].inLibrary!!) {
            holder.binding?.cardColor = "#4E1E75"
            holder.binding?.libraryText?.setTextColor(Color.parseColor("#FFFFFF"))
            holder.binding?.libraryText?.text = "✔ Library"
            holder.binding?.addToLibrary?.setOnClickListener {
                listener.onRemoveFromLibrary(item)
                items[position].inLibrary=false
                notifyItemChanged(position)
            }
        }
        else {
            holder.binding?.cardColor = "#FFFFFF"
            holder.binding?.libraryText?.setTextColor(Color.parseColor("#001C6A"))
            holder.binding?.libraryText?.text = "+ Library"
            holder.binding?.addToLibrary?.setOnClickListener {
                listener.onAddToLibrary(item)
                items[position].inLibrary=true
                notifyItemChanged(position)

            }
        }
        for (artist in artists) {
            for (ar in item.artists) {
                if (ar.id == artist.artistId) {
                    holder.binding?.artist=artist
                    break
                }
            }
        }
        holder.binding?.album=item
        var artists=""
        for (i in 0 until item.artists.size) {
            artists += item.artists[i].name
            if (i<item.artists.size-1) {
                artists= "$artists, "
            }
        }
        artists=artists.trim()
        if (artists[artists.lastIndex] == ',') {
            artists=artists.substring(0,artists.lastIndex)
        }
        if (item.album_group=="appears_on") {
            holder.binding?.type?.text="Featured"
        }
        else if (item.album_group=="album") {
            holder.binding?.type?.text = "Album"
        } else if (item.album_group == "single") {
            holder.binding?.type?.text = "Single/EP"
        }

        holder.binding?.artists?.text = artists
        holder.binding?.item?.setOnClickListener {
            listener.onItemClick(position)
        }
        holder.binding?.artistName?.setOnClickListener {
            listener.onArtistClick(
                holder.binding.artist!!.name,
                holder.binding.artist!!.artistId,
                holder.binding.artist!!.image
            )
        }
        holder.binding?.artists?.setOnClickListener {
            listener.onArtistClick(
                holder.binding.artist!!.name,
                holder.binding.artist!!.artistId,
                holder.binding.artist!!.image
            )
        }
        holder.binding?.profileImage?.setOnClickListener {
            listener.onArtistClick(
                holder.binding.artist!!.name,
                holder.binding.artist!!.artistId,
                holder.binding.artist!!.image
            )
        }

        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return items.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemTimelineAlbumsBinding? = DataBindingUtil.bind(itemView)
    }
}