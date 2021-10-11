package com.musichub.app.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemLibraryBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.TrackItems
import java.lang.Exception

class LibraryAdapter(private val context:Context,private val albums:ArrayList<AlbumItems>,private val tracks: ArrayList<TrackItems>,private val listener : RecyclerViewItemClick) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_library,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Log.d("positionAll",position.toString())
        if (position<tracks.size) {
            val track = tracks[position]
            holder.binding?.name = track.name
            var artists = ""
            for (i in 0 until track.artists.size) {
                artists += track.artists[i].name
                if (i < track.artists.size - 1) {
                    artists = "$artists, "
                }
            }
            holder.binding?.artistName = artists
            try {
                holder.binding?.image = track.album.images?.get(0)?.url
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Log.d("positionAlbum",position.toString() + " "+albums[position].name)
        }
        else {
            val pos=position%albums.size
           // Log.d("positionTracks",pos.toString()+ " "+tracks[pos].name)
            val album=albums[pos]
            holder.binding?.name=album.name
            var artists=""
            for (i in 0 until album.artists.size) {
                artists += album.artists[i].name
                if (i<album.artists.size-1) {
                    artists= "$artists, "
                }
            }
            holder.binding?.artistName = artists
            holder.binding?.image= album.images!![0].url
        }
        holder.binding?.item?.setOnClickListener {
            listener.onItemClick(position)
        }
        holder.binding?.executePendingBindings()

    }

    override fun getItemCount(): Int {
        return (albums.size + tracks.size)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemLibraryBinding? = DataBindingUtil.bind(itemView)
    }
}