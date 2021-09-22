package com.musichub.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemArtistBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.SpotifyArtistItem

class ArtistsAdapter(private val context: Context,private val artists:ArrayList<SpotifyArtistItem>,private val listener:RecyclerViewItemClick) : RecyclerView.Adapter<ArtistsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_artist,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=artists[position]
        holder.binding?.data=item
        holder.binding?.item?.setOnClickListener {
            listener.onItemClick(position)
        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return artists.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemArtistBinding? = DataBindingUtil.bind(itemView)
    }
}