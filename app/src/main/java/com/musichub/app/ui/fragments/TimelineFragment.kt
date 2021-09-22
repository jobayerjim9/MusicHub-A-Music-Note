package com.musichub.app.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.musichub.app.R
import com.musichub.app.adapters.AlbumAdapter
import com.musichub.app.adapters.TimelineAlbumAdapter
import com.musichub.app.databinding.FragmentTimelineBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.ArtistShort
import com.musichub.app.models.spotify.SpotifyAlbum
import com.musichub.app.viewmodels.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class TimelineFragment : Fragment(),RecyclerViewItemClick {
    private lateinit var viewModel: ArtistViewModel
    private lateinit var binding:FragmentTimelineBinding
    private val followedArtistsId:ArrayList<FollowedArtist> = ArrayList()
    private val albumsAll:ArrayList<AlbumItems> = ArrayList()
    private val albumsSingle:ArrayList<AlbumItems> = ArrayList()
    private val albumsFeatured:ArrayList<AlbumItems> = ArrayList()
    private val albumsOnly:ArrayList<AlbumItems> = ArrayList()
    private val libraryItems:ArrayList<AlbumItems> = ArrayList()
    var offsetAll=0
    var offsetSingle=0
    var offsetFeatured=0
    var offsetOnly=0
    lateinit var albumAdapterAll: TimelineAlbumAdapter
    lateinit var albumAdapterSingle: TimelineAlbumAdapter
    lateinit var albumAdapterFeatured: TimelineAlbumAdapter
    lateinit var albumAdapterAlbum: TimelineAlbumAdapter
    lateinit var navHostFragment: NavHostFragment
    var emptyFollowed=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_timeline, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        viewModel= ViewModelProvider(this).get(ArtistViewModel::class.java)
        navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment

        binding.albumRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("adasd",binding.tabLayout.selectedTabPosition.toString())
                if (binding.tabLayout.selectedTabPosition==0) {
                    binding.albumRecycler.adapter=albumAdapterAll
                }
                else if (binding.tabLayout.selectedTabPosition==1) {
                    binding.albumRecycler.adapter=albumAdapterSingle
                }
                else if (binding.tabLayout.selectedTabPosition==2) {
                    binding.albumRecycler.adapter=albumAdapterAlbum
                }
                else if (binding.tabLayout.selectedTabPosition==3) {
                    binding.albumRecycler.adapter=albumAdapterFeatured
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        viewModel.followedArtist.observe(viewLifecycleOwner,{
            if (it.isNotEmpty()){
                binding.tabLayout.visibility=View.VISIBLE
                binding.noItemText.visibility=View.GONE
            }
            else {
                albumsAll.clear()
                albumsSingle.clear()
                albumsFeatured.clear()
                albumsSingle.clear()
                binding.tabLayout.visibility=View.GONE
                if (binding.swipeRefresh.isRefreshing) {
                    binding.swipeRefresh.isRefreshing=false
                }
                binding.noItemText.visibility=View.VISIBLE
            }
            for (artist in it) {
                if (!contains(artist)) {
                    followedArtistsId.add(artist)
                    viewModel.getAllAlbums(artist.artistId,0)
                    viewModel.getSingleAlbums(artist.artistId,0)
                    viewModel.getOnlyAlbums(artist.artistId,0)
                    viewModel.getFeaturedAlbums(artist.artistId,0,100)
                }
            }
            albumAdapterAll=TimelineAlbumAdapter(requireContext(),albumsAll,followedArtistsId,libraryItems,this)
            binding.albumRecycler.adapter=albumAdapterAll
            albumAdapterSingle= TimelineAlbumAdapter(requireContext(),albumsSingle,followedArtistsId,libraryItems,this)
            albumAdapterAlbum=TimelineAlbumAdapter(requireContext(),albumsOnly,followedArtistsId,libraryItems,this)
            albumAdapterFeatured=TimelineAlbumAdapter(requireContext(),albumsFeatured,followedArtistsId,libraryItems,this)
        })
        viewModel.spotifyAlbumsAll.observe(viewLifecycleOwner,{
            offsetAll=it.offset+50
            for(album in it.items) {
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary=true
                    }
                }
            }
            albumsAll.addAll(it.items)
            albumsAll.shuffle()
            albumsAll.sortBy { album -> !album.inLibrary!! }
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing=false
            }
            if (!emptyFollowed) {
                albumAdapterAll.notifyDataSetChanged()
            }
        })
        viewModel.spotifyAlbumsSingle.observe(viewLifecycleOwner,{
            offsetSingle=it.offset+50
            for(album in it.items) {
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary=true
                    }
                }
            }
            albumsSingle.addAll(it.items)
            albumsSingle.shuffle()
            albumsSingle.sortBy { album -> !album.inLibrary!! }
            if (!emptyFollowed) {
                albumAdapterSingle.notifyDataSetChanged()
            }
        })
        viewModel.spotifyAlbumsOnly.observe(viewLifecycleOwner,{
            offsetOnly=it.offset+50
            for(album in it.items) {
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary=true
                    }
                }
            }
            albumsOnly.addAll(it.items)
            albumsOnly.shuffle()
            albumsOnly.sortBy { album -> !album.inLibrary!! }
            if (!emptyFollowed) {
                albumAdapterAlbum.notifyDataSetChanged()
            }
        })
        viewModel.spotifyAlbumsFeatured.observe(viewLifecycleOwner,{
            offsetFeatured=it.offset+50
            for(album in it.items) {
                var various=false
                for (library in libraryItems) {
                    if (album.id == library.id) {
                        album.inLibrary=true
                    }
                }
                for (artist in album.artists) {
                    if (artist.name == "Various Artists") {
                        various=true
                    }
                }
                if (!various) {
                    albumsFeatured.add(album)
                }
            }
            albumsFeatured.shuffle()
            albumsFeatured.sortBy { album -> !album.inLibrary!! }
            if (!emptyFollowed) {
                albumAdapterFeatured.notifyDataSetChanged()
            }
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
                albumsAll.sortBy { album -> !album.inLibrary!! }
                albumsFeatured.sortBy { album -> !album.inLibrary!! }
                albumsSingle.sortBy { album -> !album.inLibrary!! }
                albumsOnly.sortBy { album -> !album.inLibrary!! }
                albumAdapterAll.notifyDataSetChanged()
                albumAdapterAlbum.notifyDataSetChanged()
                albumAdapterSingle.notifyDataSetChanged()
                albumAdapterFeatured.notifyDataSetChanged()
            }
        })
        binding.swipeRefresh.setOnRefreshListener {
            refreshTimeline()
        }
        binding.albumRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val total = layoutManager!!.itemCount
                val currentLastItem = layoutManager!!.findLastVisibleItemPosition()
                if (currentLastItem == total - 1) {
                    if (binding.tabLayout.selectedTabPosition==0) {
                        for (artist in followedArtistsId) {
                            viewModel.getAllAlbums(artist.artistId,offsetAll)
                        }

                    }
                    else if (binding.tabLayout.selectedTabPosition==1) {
                        for (artist in followedArtistsId) {
                            viewModel.getSingleAlbums(artist.artistId, offsetSingle)
                        }
                    }
                    else if (binding.tabLayout.selectedTabPosition==2) {
                        for (artist in followedArtistsId) {
                            viewModel.getOnlyAlbums(artist.artistId, offsetOnly)
                        }
                    }
                    else if (binding.tabLayout.selectedTabPosition==3) {
                        for (artist in followedArtistsId) {
                            viewModel.getFeaturedAlbums(artist.artistId, offsetFeatured)
                        }
                    }
                }

            }
        })
    }
    private fun contains(followedArtist: FollowedArtist) : Boolean {
        for (artistId in followedArtistsId) {
            if (followedArtist.artistId == artistId.artistId) {
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        viewModel.getLibraryItems()
        viewModel.getFollowedArtist()
    }

    override fun onItemClick(position: Int) {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> {
                val action=TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(albumsAll[position],albumsAll[position].artists[0].name)
                navHostFragment.navController.navigate(action)
            }
            1 -> {
                val action=TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(albumsSingle[position],albumsSingle[position].artists[0].name)
                navHostFragment.navController.navigate(action)
            }
            2 -> {
                val action=TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(albumsOnly[position],albumsOnly[position].artists[0].name)
                navHostFragment.navController.navigate(action)
            }
            3 -> {
                val action=TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(albumsFeatured[position],albumsFeatured[position].artists[0].name)
                navHostFragment.navController.navigate(action)
            }
        }
    }
    private fun refreshTimeline() {
        albumsAll.clear()
        albumsOnly.clear()
        albumsSingle.clear()
        albumsFeatured.clear()
        followedArtistsId.clear()
        viewModel.getLibraryItems()
        viewModel.getFollowedArtist()

    }
    override fun onAddToLibrary(albumItems: AlbumItems) {
        viewModel.addToLibrary(albumItems)
        Toast.makeText(requireContext(),"Added To Library",Toast.LENGTH_SHORT).show()
    }

    override fun onRemoveFromLibrary(albumItems: AlbumItems) {
        viewModel.removeFromLibrary(albumItems)
        Toast.makeText(requireContext(),"Removed From Library",Toast.LENGTH_SHORT).show()
    }


}