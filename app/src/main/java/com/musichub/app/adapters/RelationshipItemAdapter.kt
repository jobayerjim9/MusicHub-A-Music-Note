package com.musichub.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemArtistTextBinding
import com.musichub.app.helpers.listeners.RelationshipItemClick
import com.musichub.app.models.genius.RelationshipItem

class RelationshipItemAdapter(
    private val context: Context,
    private val items: ArrayList<RelationshipItem>,
    private val listener: RelationshipItemClick
) : RecyclerView.Adapter<RelationshipItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_artist_text, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.name = items[position].full_title
        holder.binding?.nameView?.setOnClickListener {
            listener.onRelationShipClick(
                items[position].full_title,
                items[position].primary_artist.name
            )
        }
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemArtistTextBinding? = DataBindingUtil.bind(itemView)
    }
}