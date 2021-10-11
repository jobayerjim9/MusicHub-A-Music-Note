package com.musichub.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemTracksBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.TrackItems
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class TrackAdapter(private val context: Context, private val tracks: ArrayList<TrackItems>,private val listener: RecyclerViewItemClick) :
    RecyclerView.Adapter<TrackAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tracks, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]
        holder.binding?.trackNumber?.text = (position + 1).toString()
        holder.binding?.name?.text = track.name
        var artists = ""
        for (i in 0 until track.artists.size) {
            artists += track.artists[i].name
            if (i < track.artists.size - 1) {
                artists = "$artists, "
            }
        }
        holder.binding?.artistName?.text = artists
        val minute = TimeUnit.MILLISECONDS.toMinutes(track.duration_ms)
        val second = TimeUnit.MILLISECONDS.toSeconds(track.duration_ms) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.duration_ms))
        var durationS = ""
        if (second.toString().length == 1) {
            durationS = "$minute:0$second"
        } else {
            durationS = "$minute:$second"
        }

        holder.binding?.item?.setOnClickListener {
            listener.onItemClick(position)
        }
        holder.binding?.durationMs?.text = durationS
        holder.binding?.executePendingBindings()

    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemTracksBinding? = DataBindingUtil.bind(itemView)
    }

}