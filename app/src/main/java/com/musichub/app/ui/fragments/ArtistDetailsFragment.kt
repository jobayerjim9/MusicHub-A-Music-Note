package com.musichub.app.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

import com.musichub.app.R
import com.musichub.app.adapters.AlbumAdapter
import com.musichub.app.databinding.FragmentArtistDetailsBinding
import com.musichub.app.helpers.listeners.OnArtistClick
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.ArtistShort
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.SpotifyArtistItem
import com.musichub.app.viewmodels.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ArtistDetailsFragment : Fragment(), RecyclerViewItemClick, OnArtistClick {
    private val args: ArtistDetailsFragmentArgs by navArgs()
    private lateinit var viewModel: ArtistViewModel
    lateinit var binding: FragmentArtistDetailsBinding
    private val albumsAll: ArrayList<AlbumItems> = ArrayList()
    private val albumsSingle: ArrayList<AlbumItems> = ArrayList()
    private val albumsFeatured: ArrayList<AlbumItems> = ArrayList()
    private val albumsOnly: ArrayList<AlbumItems> = ArrayList()
    var offsetAll = 0
    var offsetSingle = 0
    var offsetFeatured = 0
    var offsetOnly=0
    lateinit var albumAdapterAll:AlbumAdapter
    lateinit var albumAdapterSingle:AlbumAdapter
    lateinit var albumAdapterFeatured:AlbumAdapter
    lateinit var albumAdapterAlbum:AlbumAdapter
    lateinit var navHostFragment: NavHostFragment
    private val libraryItems: ArrayList<AlbumItems> = ArrayList()
    var launched = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_artist_details, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        Log.d("navArgs",args.name+" "+args.image+" "+args.artistId)

        navHostFragment=requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        viewModel = ViewModelProvider(this).get(ArtistViewModel::class.java)
        setupObservers()
        viewModel.searchOnGenius(args.name)
        binding.image = args.image
        binding.name = args.name
        binding.bio = "Loading..."
        binding.back.setOnClickListener {
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
            navHostFragment.navController.popBackStack()
        }

        val artist = ArtistShort(args.name, args.artistId, args.image.toString())
        val layoutManager = LinearLayoutManager(requireContext())
        binding.albumRecycler.layoutManager = layoutManager

        binding.albumRecycler.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                when (action) {
                    MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }

        })
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("adasd", binding.tabLayout.selectedTabPosition.toString())
                if (binding.tabLayout.selectedTabPosition == 0) {
                    binding.albumRecycler.adapter = albumAdapterAll
                } else if (binding.tabLayout.selectedTabPosition == 1) {
                    binding.albumRecycler.adapter = albumAdapterSingle
                } else if (binding.tabLayout.selectedTabPosition == 2) {
                    binding.albumRecycler.adapter = albumAdapterAlbum
                } else if (binding.tabLayout.selectedTabPosition == 3) {
                    binding.albumRecycler.adapter = albumAdapterFeatured
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        albumAdapterAll = AlbumAdapter(requireContext(), albumsAll, artist, this, this)
        binding.albumRecycler.adapter = albumAdapterAll
        albumAdapterSingle = AlbumAdapter(requireContext(), albumsSingle, artist, this, this)
        albumAdapterAlbum = AlbumAdapter(requireContext(), albumsOnly, artist, this, this)
        albumAdapterFeatured = AlbumAdapter(requireContext(), albumsFeatured, artist, this, this)

        binding.albumRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val total = layoutManager!!.itemCount
                val currentLastItem = layoutManager!!.findLastVisibleItemPosition()
                if (currentLastItem == total - 1) {
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        viewModel.getAllAlbums(args.artistId, offsetAll)
                    } else if (binding.tabLayout.selectedTabPosition == 1) {
                        viewModel.getSingleAlbums(args.artistId, offsetSingle)
                    } else if (binding.tabLayout.selectedTabPosition == 2) {
                        viewModel.getOnlyAlbums(args.artistId, offsetOnly)
                    } else if (binding.tabLayout.selectedTabPosition == 3) {
                        viewModel.getFeaturedAlbums(args.artistId, offsetFeatured)
                    }
                }

            }
        })
        viewModel.getLibraryItems()


        binding.share.setOnClickListener {
            val shareIntent = Intent()
            val text =
                "Checkout " + args.name + " On MusicHub:- " + "https://musichub.com/artist/" + args.artistId
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    private fun setupObservers() {
        viewModel.artistBio.observe(viewLifecycleOwner, {
            Log.d("bioFromView", it)
            if (it.length >= 240) {
                val shortBio = it.substring(0, 240) + "...<font color=#928F92>(Read More)</font>"
                binding.bio = shortBio
            } else {
                val shortBio = "$it...<font color=#928F92>(Read More)</font>"
                binding.bio = shortBio
            }
            binding.textView7.setOnClickListener { v ->
                val action =
                    ArtistDetailsFragmentDirections.actionArtistDetailsFragmentToFullBioFragment2(
                        args.name,
                        it.toString()
                    )
                navHostFragment.navController.navigate(action)
            }
        })
        viewModel.artistSocialMedia.observe(viewLifecycleOwner,{
            binding.socialMediaLayout.visibility=View.VISIBLE
            if (it.facebook_name.isEmpty()) {
                binding.facebook.alpha=0.5f
            } else {
                binding.facebook.setOnClickListener { _->
                    val uri =
                        Uri.parse("https://www.facebook.com/"+it.facebook_name)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            if (it.instagram_name.isEmpty()) {
                binding.instagram.alpha=0.5f
            } else {

                binding.instagram.setOnClickListener { _->
                    val uri =
                        Uri.parse("https://www.instagram.com/"+it.instagram_name)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            if (it.twitter_name.isEmpty()) {
                binding.twitter.alpha=0.5f
            } else {
                binding.twitter.setOnClickListener { _->
                    val uri =
                        Uri.parse("https://mobile.twitter.com/"+it.twitter_name)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
        })
        viewModel.isFollowed(args.artistId)
        viewModel.isFollowed.observe(viewLifecycleOwner, {
            if (it) {
                unfollowState()

            } else {
                followState()
            }
        })
        viewModel.getAllAlbums(args.artistId,0)
        viewModel.spotifyAlbumsAll.observe(viewLifecycleOwner,{
            offsetAll = it.offset + 50
            for(album in it.items) {
                if (!contains(
                        album,
                        albumsAll
                    ) && album.album_type != "compilation" && !isVariousArtist(album)
                ) {
                    albumsAll.add(album)
                }
                val splitDate = album.release_date?.split("-")
                try {
                    album.formattedDate = splitDate!![2] + "/" + splitDate[1] + "/" + splitDate[0]
                } catch (e: IndexOutOfBoundsException) {
                    album.formattedDate = "01/01/" + splitDate!![0]
                    Log.e("dateNotFound", album.release_date.toString())
                }
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary = true
                    }
                }
                if (offsetAll <= 50) {
                    viewModel.sortWith(albumsAll)
                }
            }

            albumAdapterAll.notifyDataSetChanged()
        })
        viewModel.getSingleAlbums(args.artistId,0)
        viewModel.spotifyAlbumsSingle.observe(viewLifecycleOwner, {
            offsetSingle = it.offset + 50

            for (album in it.items) {
                if (!contains(album, albumsSingle)) {
                    albumsSingle.add(album)
                }
                val splitDate = album.release_date?.split("-")
                try {
                    album.formattedDate = splitDate!![2] + "/" + splitDate[1] + "/" + splitDate[0]
                } catch (e: IndexOutOfBoundsException) {
                    album.formattedDate = "01/01/" + splitDate!![0]
                    Log.e("dateNotFound", album.release_date.toString())
                }
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary = true
                    }
                }
            }
            if (offsetSingle <= 50) {
                viewModel.sortWith(albumsSingle)
            }
            albumAdapterSingle.notifyDataSetChanged()

        })
        viewModel.getOnlyAlbums(args.artistId,0)
        viewModel.spotifyAlbumsOnly.observe(viewLifecycleOwner,{
            offsetOnly = it.offset + 50
            for(album in it.items) {
                if (!contains(album, albumsOnly)) {
                    albumsOnly.add(album)
                }
                val splitDate = album.release_date?.split("-")
                try {
                    album.formattedDate = splitDate!![2] + "/" + splitDate[1] + "/" + splitDate[0]
                } catch (e: IndexOutOfBoundsException) {
                    album.formattedDate = "01/01/" + splitDate!![0]
                    Log.e("dateNotFound", album.release_date.toString())
                }
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary = true
                    }
                }
            }
            if (offsetOnly <= 50) {
                viewModel.sortWith(albumsOnly)
            }
            albumAdapterAlbum.notifyDataSetChanged()

        })
        viewModel.getFeaturedAlbums(args.artistId,0)
        viewModel.spotifyAlbumsFeatured.observe(viewLifecycleOwner,{
            offsetFeatured=it.offset+50
            for(album in it.items) {
                val splitDate = album.release_date?.split("-")
                try {
                    album.formattedDate = splitDate!![2] + "/" + splitDate[1] + "/" + splitDate[0]
                } catch (e: IndexOutOfBoundsException) {
                    album.formattedDate = "01/01/" + splitDate!![0]
                    Log.e("dateNotFound", album.release_date.toString())
                }
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary = true
                    }
                }

                if (!contains(album, albumsFeatured) && !isVariousArtist(album)) {
                    albumsFeatured.add(album)
                }
            }
            if (offsetFeatured <= 50) {
                viewModel.sortWith(albumsFeatured)
            }
            albumAdapterFeatured.notifyDataSetChanged()
        })
        viewModel.libraryItems.observe(viewLifecycleOwner,{
            libraryItems.clear()
            if (it.isNotEmpty()) {
                libraryItems.addAll(it)
                for (album in albumsAll) {
                    for (library in libraryItems) {
                        if (album.id == library.id) {
                            album.inLibrary = true
                        }
                    }
                }
                for (album in albumsFeatured) {
                    for (library in libraryItems) {
                        if (album.id == library.id) {
                            album.inLibrary = true
                        }
                    }
                }
                for (album in albumsSingle) {
                    for (library in libraryItems) {
                        if (album.id == library.id) {
                            album.inLibrary = true
                        }
                    }
                }
                for (album in albumsOnly) {
                    for (library in libraryItems) {
                        if (album.id == library.id) {
                            album.inLibrary = true
                        }
                    }
                }

                albumAdapterAll.notifyDataSetChanged()
                albumAdapterAlbum.notifyDataSetChanged()
                albumAdapterSingle.notifyDataSetChanged()
                albumAdapterFeatured.notifyDataSetChanged()
            }
        })
        viewModel.foundArtist.observe(viewLifecycleOwner, {
            if (!launched) {
                if (it.images!!.isNotEmpty()) {
                    val action =
                        ArtistDetailsFragmentDirections.actionArtistDetailsFragmentSelf(
                            it.name,
                            it.id,
                            it.images[0].url
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val action =
                        ArtistDetailsFragmentDirections.actionArtistDetailsFragmentSelf(
                            it.name,
                            it.id,
                            ""
                        )
                    navHostFragment.navController.navigate(action)
                }
                launched = true
            }
        })
    }

    companion object {
        fun isVariousArtist(album: AlbumItems): Boolean {
            for (artist in album.artists) {
                if (artist.name == "Various Artists") {
                    return true
                }
            }
            return false
        }
    }

    private fun unfollowState() {
        binding.cardColor = "#4E1E75"
        binding.followText.setTextColor(Color.parseColor("#FFFFFF"))
        binding.followText.text = "Unfollow"
        binding.followArtist.setOnClickListener {
            viewModel.unfollowArtist(args.artistId)
            followState()
        }
    }


    private fun followState() {
        binding.cardColor="#FFFFFF"
        binding.followText.setTextColor(Color.parseColor("#001C6A"))
        binding.followText.text="Follow"
        binding.followArtist.setOnClickListener {
            viewModel.followArtist(args.artistId,args.name,args.image.toString())
            unfollowState()
        }
    }

    override fun onItemClick(position: Int) {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> {
                val action=ArtistDetailsFragmentDirections.actionArtistDetailsFragmentToAlbumDetailsFragment(albumsAll[position],args.name)
                navHostFragment.navController.navigate(action)
            }
            1 -> {
                val action=ArtistDetailsFragmentDirections.actionArtistDetailsFragmentToAlbumDetailsFragment(albumsSingle[position],args.name)
                navHostFragment.navController.navigate(action)
            }
            2 -> {
                val action =
                    ArtistDetailsFragmentDirections.actionArtistDetailsFragmentToAlbumDetailsFragment(
                        albumsOnly[position],
                        args.name
                    )
                navHostFragment.navController.navigate(action)
            }
            3 -> {
                val action =
                    ArtistDetailsFragmentDirections.actionArtistDetailsFragmentToAlbumDetailsFragment(
                        albumsFeatured[position],
                        args.name
                    )
                navHostFragment.navController.navigate(action)
            }
        }
    }

    private fun contains(item: AlbumItems, albums: ArrayList<AlbumItems>): Boolean {
        for (album in albums) {
            if (album.name == item.name) {
                return true
            }
        }
        return false
    }

    override fun onAddToLibrary(albumItems: AlbumItems) {
        viewModel.addToLibrary(albumItems)
        Toast.makeText(requireContext(), "Added To Library", Toast.LENGTH_SHORT).show()
    }

    override fun onRemoveFromLibrary(albumItems: AlbumItems) {
        viewModel.removeFromLibrary(albumItems)
        Toast.makeText(requireContext(), "Removed From Library", Toast.LENGTH_SHORT).show()

    }

    override fun onArtistClick(name: String, id: String, image: String) {

    }

    override fun onArtistClick(name: String) {
        launched = false
        viewModel.searchArtistSpotify(name)
    }


}