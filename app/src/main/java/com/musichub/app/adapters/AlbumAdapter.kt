package com.musichub.app.adapters

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.databinding.ItemAlbumsBinding
import com.musichub.app.helpers.listeners.OnArtistClick
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.ArtistShort
import com.musichub.app.models.spotify.AlbumItems
import java.lang.Exception
import java.lang.IndexOutOfBoundsException

class AlbumAdapter(
    private val context: Context,
    private val items: ArrayList<AlbumItems>,
    private val artist: ArtistShort,
    private val listener: RecyclerViewItemClick,
    private val artistClick: OnArtistClick
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_albums, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding?.artist = artist
        holder.binding?.album = items[position]
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
        var artists = ""
        val artistArray: ArrayList<String> = ArrayList()
        for (i in 0 until items[position].artists.size) {
            artists += items[position].artists[i].name
            if (i < items[position].artists.size - 1) {
                artists = "$artists, "
                artistArray.add(item.artists[i].name + " ")
            } else {
                artistArray.add(item.artists[i].name)
            }
        }
        artists = artists.trim()
        if (artists[artists.lastIndex] == ',') {
            artists = artists.substring(0, artists.lastIndex)
        }
        if (items[position].album_group == "appears_on") {
            holder.binding?.type?.text = "Featured"
        } else if (items[position].album_group == "album") {
            holder.binding?.type?.text = "Album"
        } else if (items[position].album_group == "single") {
            holder.binding?.type?.text = "Single/EP"
        }
        val ss = SpannableString(artists)
        var totalL = 0

        for (i in 0 until artistArray.size) {
            if (i == 0) {
                totalL = 0
            }

            try {
                if (i == (artistArray.size - 1)) {
                    ss.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(p0: View) {
                                artistClick.onArtistClick(artistArray[i])
                            }

                        },
                        artists.length - artistArray[i].length,
                        artists.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    ss.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(p0: View) {
                                artistClick.onArtistClick(artistArray[i])
                            }

                        },
                        totalL,
                        totalL + artistArray[i].length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

            } catch (e: IndexOutOfBoundsException) {
                try {
                    ss.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(p0: View) {
                                artistClick.onArtistClick(artistArray[i])
                            }

                        },
                        0,
                        artists.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            totalL += if (i == (artistArray.size - 1)) {
                artistArray[i].length
            } else {
                artistArray[i].length + 1
            }


        }
        holder.binding?.artists?.text = ss
        holder.binding?.artists?.movementMethod = LinkMovementMethod.getInstance()
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