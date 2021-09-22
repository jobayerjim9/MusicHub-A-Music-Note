package com.musichub.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemArtistTextBinding
import com.musichub.app.models.genius.Artist

class SongArtistAdapter(private val context:Context,private val artists: ArrayList<Artist>) : RecyclerView.Adapter<SongArtistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_artist_text,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.name=artists[position].name
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return artists.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding : ItemArtistTextBinding? = DataBindingUtil.bind(itemView)
    }
}