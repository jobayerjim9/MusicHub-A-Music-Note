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
import com.musichub.app.helpers.listeners.OnArtistClick
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.ArtistShort
import com.musichub.app.models.spotify.SpotifyAlbum
import com.musichub.app.models.spotify.SpotifyArtistItem
import com.musichub.app.ui.fragments.ArtistDetailsFragment.Companion.isVariousArtist
import com.musichub.app.viewmodels.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

@AndroidEntryPoint
class TimelineFragment : Fragment(), RecyclerViewItemClick, OnArtistClick {
    private lateinit var viewModel: ArtistViewModel
    private lateinit var binding: FragmentTimelineBinding
    private val followedArtistsId: ArrayList<FollowedArtist> = ArrayList()
    private val albumsAll: ArrayList<AlbumItems> = ArrayList()
    private val albumsSingle: ArrayList<AlbumItems> = ArrayList()
    private val albumsFeatured: ArrayList<AlbumItems> = ArrayList()
    private val albumsOnly: ArrayList<AlbumItems> = ArrayList()
    private val libraryItems: ArrayList<AlbumItems> = ArrayList()
    var offsetAll = 0
    var offsetSingle = 0
    var offsetFeatured = 0
    var offsetOnly = 0
    lateinit var albumAdapterAll: TimelineAlbumAdapter
    lateinit var albumAdapterSingle: TimelineAlbumAdapter
    lateinit var albumAdapterFeatured: TimelineAlbumAdapter
    lateinit var albumAdapterAlbum: TimelineAlbumAdapter
    lateinit var navHostFragment: NavHostFragment
    var emptyFollowed = false
    var launched = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_timeline, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        viewModel = ViewModelProvider(this).get(ArtistViewModel::class.java)
        navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        setupObservers()
        binding.albumRecycler.layoutManager = LinearLayoutManager(requireContext())
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

        binding.swipeRefresh.setOnRefreshListener {
            refreshTimeline()
        }
        binding.albumRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val total = layoutManager!!.itemCount
                val currentLastItem = layoutManager!!.findLastVisibleItemPosition()
                if (currentLastItem >= total - 1 && albumsAll.size > 0) {
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        for (followed in followedArtistsId) {
                            viewModel.getFeaturedAlbums(
                                followed.artistId,
                                offsetFeatured,
                                100
                            )
                            viewModel.getSingleAlbums(
                                followed.artistId,
                                offsetSingle
                            )
                            viewModel.getOnlyAlbums(followed.artistId, offsetOnly)
                        }

                    } else if (binding.tabLayout.selectedTabPosition == 1) {
                        for (followed in followedArtistsId) {
                            viewModel.getSingleAlbums(
                                followed.artistId,
                                offsetSingle
                            )
                        }

                    } else if (binding.tabLayout.selectedTabPosition == 2) {
                        for (followed in followedArtistsId) {
                            viewModel.getOnlyAlbums(
                                followed.artistId,
                                offsetOnly
                            )
                        }
                    } else if (binding.tabLayout.selectedTabPosition == 3) {
                        for (followed in followedArtistsId) {
                            viewModel.getFeaturedAlbums(
                                followed.artistId,
                                offsetOnly,
                                100
                            )
                        }
                    }
                }

            }
        })

    }

    private fun setupObservers() {
        viewModel.foundArtist.observe(viewLifecycleOwner, {
            if (!launched) {
                if (it.images!!.isNotEmpty()) {
                    val action =
                        TimelineFragmentDirections.actionTimelineFragmentToArtistDetailsFragment(
                            it.name,
                            it.id,
                            it.images[0].url
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val action =
                        TimelineFragmentDirections.actionTimelineFragmentToArtistDetailsFragment(
                            it.name,
                            it.id,
                            ""
                        )
                    navHostFragment.navController.navigate(action)
                }
                launched = true
            }
        })
        viewModel.followedArtist.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                binding.tabLayout.visibility = View.VISIBLE
                binding.noItemText.visibility = View.GONE

                for (artist in it) {
                    if (!contains(artist)) {
                        followedArtistsId.add(artist)
                    }
                }
                followedArtistsId.shuffle()
                for (followed in followedArtistsId) {
                    viewModel.getFeaturedAlbums(
                        followed.artistId,
                        0,
                        100
                    )
                    viewModel.getSingleAlbums(
                        followed.artistId,
                        0
                    )
                    viewModel.getOnlyAlbums(followed.artistId, 0)
                }
                albumAdapterAll = TimelineAlbumAdapter(
                    requireContext(),
                    albumsAll,
                    followedArtistsId,
                    libraryItems,
                    this, this
                )
                binding.albumRecycler.adapter = albumAdapterAll
                albumAdapterSingle = TimelineAlbumAdapter(
                requireContext(),
                albumsSingle,
                followedArtistsId,
                libraryItems,
                this, this
            )
            albumAdapterAlbum = TimelineAlbumAdapter(
                requireContext(),
                albumsOnly,
                followedArtistsId,
                libraryItems,
                this, this
            )
                albumAdapterFeatured = TimelineAlbumAdapter(
                    requireContext(),
                    albumsFeatured,
                    followedArtistsId,
                    libraryItems,
                    this, this
                )
            } else {
                albumsAll.clear()
                albumsSingle.clear()
                albumsFeatured.clear()
                albumsSingle.clear()
                binding.tabLayout.visibility = View.GONE
                if (binding.swipeRefresh.isRefreshing) {
                    binding.swipeRefresh.isRefreshing = false
                }
                binding.noItemText.visibility = View.VISIBLE
            }
        })
        viewModel.spotifyAlbumsAll.observe(viewLifecycleOwner, {
            offsetAll = it.offset + 50
            for (album in it.items) {
                if (!contains(album, albumsAll) && !isVariousArtist(album)) {
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

            }
            viewModel.sortWith(albumsAll)
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = false
            }
            if (!emptyFollowed) {
                albumAdapterAll.notifyDataSetChanged()
            }
        })
        viewModel.spotifyAlbumsSingle.observe(viewLifecycleOwner, {
            offsetSingle = it.offset + 50
            for (album in it.items) {
                if (!contains(album, albumsSingle)) {
                    albumsSingle.add(album)
                }
                if (!contains(album, albumsAll)) {
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
            }
            viewModel.sortWith(albumsSingle)
            viewModel.sortWith(albumsAll)
            albumAdapterAll.notifyDataSetChanged()
            if (!emptyFollowed) {
                albumAdapterSingle.notifyDataSetChanged()
            }
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = false
            }
        })
        viewModel.spotifyAlbumsOnly.observe(viewLifecycleOwner, {
            offsetOnly = it.offset + 50

            for (album in it.items) {
                if (!contains(album, albumsOnly)) {
                    albumsOnly.add(album)
                }
                if (!contains(album, albumsAll)) {
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
            }
            viewModel.sortWith(albumsOnly)
            viewModel.sortWith(albumsAll)


            albumAdapterAll.notifyDataSetChanged()

            if (!emptyFollowed) {
                albumAdapterAlbum.notifyDataSetChanged()
            }
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = false
            }
        })
        viewModel.spotifyAlbumsFeatured.observe(viewLifecycleOwner, {
            offsetFeatured = it.offset + 50

            for (album in it.items) {
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
                if (!contains(album, albumsAll) && !isVariousArtist(album)) {
                    albumsAll.add(album)
                }

                if (!contains(album, albumsFeatured) && !isVariousArtist(album)) {
                    albumsFeatured.add(album)
                }

            }
            viewModel.sortWith(albumsAll)
            viewModel.sortWith(albumsFeatured)
            albumAdapterAll.notifyDataSetChanged()
            if (!emptyFollowed) {
                albumAdapterFeatured.notifyDataSetChanged()
            }
            if (binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = false
            }
        })
        viewModel.libraryItems.observe(viewLifecycleOwner, {
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
    }



    private fun contains(item: AlbumItems, albums: ArrayList<AlbumItems>): Boolean {

        for (album in albums) {
            if (album.name == item.name) {
                return true
            }
        }
        return false
    }

    private fun contains(followedArtist: FollowedArtist): Boolean {
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
                val action =
                    TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(
                        albumsAll[position],
                        albumsAll[position].artists[0].name
                    )
                navHostFragment.navController.navigate(action)
            }
            1 -> {
                val action =
                    TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(
                        albumsSingle[position],
                        albumsSingle[position].artists[0].name
                    )
                navHostFragment.navController.navigate(action)
            }
            2 -> {
                val action =
                    TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(
                        albumsOnly[position],
                        albumsOnly[position].artists[0].name
                    )
                navHostFragment.navController.navigate(action)
            }
            3 -> {
                val action =
                    TimelineFragmentDirections.actionTimelineFragmentToAlbumDetailsFragment(
                        albumsFeatured[position],
                        albumsFeatured[position].artists[0].name
                    )
                navHostFragment.navController.navigate(action)
            }
        }
    }

    private fun refreshTimeline() {
        viewModel.getLibraryItems()
        viewModel.getFollowedArtist()
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
        val action = TimelineFragmentDirections.actionTimelineFragmentToArtistDetailsFragment(
            name,
            id,
            image
        )
        navHostFragment.navController.navigate(action)
    }

    override fun onArtistClick(name: String) {
        launched = false
        viewModel.searchArtistSpotify(name)
    }


}