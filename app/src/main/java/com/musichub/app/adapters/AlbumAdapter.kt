package com.musichub.app.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemAlbumsBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.ArtistShort
import com.musichub.app.models.spotify.AlbumItems

class AlbumAdapter(private val context:Context,private val items:ArrayList<AlbumItems>,private val artist:ArtistShort,private val listener:RecyclerViewItemClick) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_albums,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=items[position]
        holder.binding?.artist=artist
        holder.binding?.album=items[position]
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
        var artists=""
        for (i in 0 until items[position].artists.size) {
            artists += items[position].artists[i].name
            if (i<items[position].artists.size-1) {
                artists= "$artists, "
            }
        }

        if (items[position].album_group=="appears_on") {
            holder.binding?.type?.text="Featured"
        }
        else if (items[position].album_group=="album") {
            holder.binding?.type?.text="Album"
        }
        else if (items[position].album_group=="single") {
            holder.binding?.type?.text="Single"
        }

        holder.binding?.artists?.text=artists
        holder.binding?.item?.setOnClickListener {
            listener.onItemClick(position)
        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return items.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemAlbumsBinding? = DataBindingUtil.bind(itemView)
    }
}