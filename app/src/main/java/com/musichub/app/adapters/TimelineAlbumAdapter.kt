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
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.Toast
import com.musichub.app.helpers.listeners.OnArtistClick
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList


class TimelineAlbumAdapter(
    private val context: Context,
    private val items: ArrayList<AlbumItems>,
    private val artists: ArrayList<FollowedArtist>,
    private val libraryItems: ArrayList<AlbumItems>,
    private val listener: RecyclerViewItemClick,
    private val artistClick: OnArtistClick
) : RecyclerView.Adapter<TimelineAlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_timeline_albums, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = items[position]
            val format = SimpleDateFormat("yyyy-MM-dd")
            val releaseDate = Calendar.getInstance()
            releaseDate.time = format.parse(item.release_date!!)!!
            val now = Calendar.getInstance()
            if (releaseDate.after(now)) {
                holder.binding?.comingSoonLabel?.visibility = View.VISIBLE
            } else {
                holder.binding?.comingSoonLabel?.visibility = View.GONE
            }
            if (items[position].inLibrary!!) {
                holder.binding?.cardColor = "#4E1E75"
                holder.binding?.libraryText?.setTextColor(Color.parseColor("#FFFFFF"))
                holder.binding?.libraryText?.text = "✔ Library"
                holder.binding?.addToLibrary?.setOnClickListener {
                    listener.onRemoveFromLibrary(item)
                    items[position].inLibrary = false
                    notifyItemChanged(position)
                }
            } else {
                holder.binding?.cardColor = "#FFFFFF"
                holder.binding?.libraryText?.setTextColor(Color.parseColor("#001C6A"))
                holder.binding?.libraryText?.text = "+ Library"
                holder.binding?.addToLibrary?.setOnClickListener {
                    listener.onAddToLibrary(item)
                    items[position].inLibrary = true
                    notifyItemChanged(position)

                }
            }
            for (artist in artists) {
                for (ar in item.artists) {
                    if (ar.id == artist.artistId) {
                        holder.binding?.artist = artist
                        break
                    }
                }
            }
            holder.binding?.album = item
            var artists = ""
            val artistArray: ArrayList<String> = ArrayList()
            for (i in 0 until item.artists.size) {
                artists += item.artists[i].name

                if (i < item.artists.size - 1) {
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
            if (item.album_group == "appears_on") {
                holder.binding?.type?.text = "Featured"
            } else if (item.album_group == "album") {
                holder.binding?.type?.text = "Album"
            } else if (item.album_group == "single") {
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
            holder.binding?.artistName?.setOnClickListener {
                listener.onArtistClick(
                    holder.binding.artist!!.name,
                    holder.binding.artist!!.artistId,
                    holder.binding.artist!!.image
                )
            }
//        holder.binding?.artists?.setOnClickListener {
//            listener.onArtistClick(
//                holder.binding.artist!!.name,
//                holder.binding.artist!!.artistId,
//                holder.binding.artist!!.image
//            )
//        }
            holder.binding?.profileImage?.setOnClickListener {
                listener.onArtistClick(
                    holder.binding.artist!!.name,
                    holder.binding.artist!!.artistId,
                    holder.binding.artist!!.image
                )
            }

            holder.binding?.executePendingBindings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemTimelineAlbumsBinding? = DataBindingUtil.bind(itemView)
    }
}