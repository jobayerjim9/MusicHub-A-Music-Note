package com.musichub.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemPerformanceArtistBinding
import com.musichub.app.models.genius.CustomPerformance

class PerformanceArtistAdapter(private val context:Context,private val performances : ArrayList<CustomPerformance>) : RecyclerView.Adapter<PerformanceArtistAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_performance_artist,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.role=performances[position].label
        val adapter= SongArtistAdapter(context,performances[position].artists)
        holder.binding?.performerRecycler?.layoutManager= LinearLayoutManager(context)
        holder.binding?.performerRecycler?.adapter=adapter
        holder.binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return performances.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemPerformanceArtistBinding? = DataBindingUtil.bind(itemView)
    }
}