package com.musichub.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R

import com.musichub.app.databinding.ItemSongRelationshipsBinding
import com.musichub.app.helpers.listeners.RelationshipItemClick
import com.musichub.app.models.genius.SongRelationships

class SongRelationshipAdapter(
    private val context: Context,
    private val relationships: ArrayList<SongRelationships>,
    private val listener: RelationshipItemClick
) :
    RecyclerView.Adapter<SongRelationshipAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemSongRelationshipsBinding? = DataBindingUtil.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_song_relationships, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (relationships[position].songs.isNotEmpty()) {
            holder.binding?.title =
                relationships[position].relationship_type.uppercase().replace("_", " ")
            holder.binding?.relationShipItems?.layoutManager = LinearLayoutManager(context)
            holder.binding?.relationShipItems?.adapter =
                RelationshipItemAdapter(context, relationships[position].songs, listener)
        } else {
            holder.binding?.item?.visibility = View.GONE
        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return relationships.size
    }
}