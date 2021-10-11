package com.musichub.app.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.musichub.app.R
import com.musichub.app.adapters.LibraryAdapter
import com.musichub.app.databinding.FragmentLibraryBinding
import com.musichub.app.helpers.GridSpacingItemDecoration
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.TrackItems
import com.musichub.app.viewmodels.AlbumViewModel
import com.musichub.app.viewmodels.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() , RecyclerViewItemClick {
    lateinit var binding:FragmentLibraryBinding
    lateinit var viewModel : AlbumViewModel
    lateinit var artistViewModel: ArtistViewModel
    private val albumsAll:ArrayList<AlbumItems> = ArrayList()
    private val albumsSingle:ArrayList<AlbumItems> = ArrayList()
    private val albumsCompilation:ArrayList<AlbumItems> = ArrayList()
    private val tracksAll:ArrayList<TrackItems> = ArrayList()
    private val tracksSingle:ArrayList<TrackItems> = ArrayList()
    private val tracksAlbum:ArrayList<TrackItems> = ArrayList()
    private val tracksCompilation:ArrayList<TrackItems> = ArrayList()
    lateinit var libraryAdapterAll:LibraryAdapter
    lateinit var libraryAdapterSingle:LibraryAdapter
    lateinit var libraryAdapterOnly:LibraryAdapter
    lateinit var libraryAdapterCompilation:LibraryAdapter
    lateinit var navHostFragment:NavHostFragment
    var offset:Int=0
    var term:String=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_library, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        viewModel= ViewModelProvider(this).get(AlbumViewModel::class.java)
        artistViewModel= ViewModelProvider(this).get(ArtistViewModel::class.java)


        navHostFragment=requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        binding.searchSpotify.setOnEditorActionListener { textView, action, keyEvent ->
            var handled=false
            if (action== EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_NEXT) {
                albumsAll.clear()
                albumsSingle.clear()
                albumsCompilation.clear()
                tracksAll.clear()
                tracksCompilation.clear()
                tracksSingle.clear()
                tracksAlbum.clear()
                term=binding.searchSpotify.text.toString()
                Log.d("OnEditorActionListener",term)
                viewModel.searchTrackAlbum(term,0)
                handled=true
            }
            handled
        }
        libraryAdapterAll= LibraryAdapter(requireContext(),albumsAll,tracksAll,this)
        libraryAdapterSingle= LibraryAdapter(requireContext(),albumsSingle,tracksSingle,this)
        libraryAdapterOnly= LibraryAdapter(requireContext(),albumsAll,tracksAlbum,this)
        libraryAdapterCompilation= LibraryAdapter(requireContext(),albumsCompilation,tracksCompilation,this)
        binding.trackAlbumRecycler.layoutManager=GridLayoutManager(requireContext(),2)
        binding.trackAlbumRecycler.adapter=libraryAdapterAll
        binding.trackAlbumRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val total = layoutManager!!.itemCount
                val currentLastItem = layoutManager!!.findLastVisibleItemPosition()
                if (currentLastItem == total - 1) {
                    if (term.isNotEmpty()) {
                        viewModel.searchTrackAlbum(term, offset)
                    }
                }

            }
        })
        val spanCount = 2
        val spacing = 48
        val includeEdge = false
        binding.trackAlbumRecycler.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
        artistViewModel.libraryItems.observe(viewLifecycleOwner,{
            albumsAll.clear()
            albumsSingle.clear()
            albumsCompilation.clear()
            albumsAll.addAll(it.toSet().toList())
            for (album in it) {
                if (album.album_type=="single") {
                    albumsSingle.add(album)
                }
                else if (album.album_type == "compilation") {
                    albumsCompilation.add(album)
                }
            }
            libraryAdapterAll.notifyDataSetChanged()
            libraryAdapterSingle.notifyDataSetChanged()
            libraryAdapterOnly.notifyDataSetChanged()
            libraryAdapterCompilation.notifyDataSetChanged()
        })
        artistViewModel.getLibraryItems()
        viewModel.albums.observe(viewLifecycleOwner,{
            offset=it.offset+50
            albumsAll.addAll(it.items)
            for (album in it.items) {
                if (album.album_type=="single") {
                    albumsSingle.add(album)
                }
                else if (album.album_type == "compilation") {
                    albumsCompilation.add(album)
                }
            }
            libraryAdapterAll.notifyDataSetChanged()
            libraryAdapterSingle.notifyDataSetChanged()
            libraryAdapterOnly.notifyDataSetChanged()
            libraryAdapterCompilation.notifyDataSetChanged()
        })
        viewModel.tracks.observe(viewLifecycleOwner,{
            offset=it.offset+50
            tracksAll.addAll(it.items)
            for (track in it.items) {
                if (track.album.album_type=="single") {
                    tracksSingle.add(track)
                }
                else if (track.album.album_type == "compilation") {
                    tracksCompilation.add(track)
                }
            }
            libraryAdapterAll.notifyDataSetChanged()
            libraryAdapterSingle.notifyDataSetChanged()
            libraryAdapterOnly.notifyDataSetChanged()
            libraryAdapterCompilation.notifyDataSetChanged()

        })
        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (binding.tabLayout.selectedTabPosition==0) {
                    binding.trackAlbumRecycler.adapter=libraryAdapterAll
                }
                else if (binding.tabLayout.selectedTabPosition==1) {
                    binding.trackAlbumRecycler.adapter=libraryAdapterSingle
                }
                else if (binding.tabLayout.selectedTabPosition==2) {
                    binding.trackAlbumRecycler.adapter=libraryAdapterOnly
                }
                else if (binding.tabLayout.selectedTabPosition==3) {
                    binding.trackAlbumRecycler.adapter=libraryAdapterCompilation
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.cancelButton.setOnClickListener {
            albumsAll.clear()
            albumsSingle.clear()
            albumsCompilation.clear()
            tracksAll.clear()
            tracksAlbum.clear()
            tracksSingle.clear()
            tracksCompilation.clear()
            binding.searchSpotify.clearFocus()
            it.hideKeyboard()
            term=""
            binding.searchSpotify.setText(term)
            artistViewModel.getLibraryItems()
            Log.d("libraryCleared","cleared")
        }
        viewModel.loading.observe(viewLifecycleOwner,{
            binding.loading=it
        })

    }
    fun View.hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    override fun onItemClick(position: Int) {
        if (binding.tabLayout.selectedTabPosition == 0) {
            if (tracksAll.isNotEmpty()) {
                if (position < tracksAll.size) {
                    Log.d("track", "found")
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            tracksAll[position].album,
                            tracksAll[position].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val pos = position % tracksAll.size
                    Log.d(
                        "album",
                        albumsAll[pos].artists.size.toString() + " " + albumsAll[pos].artists[0].name
                    )
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            albumsAll[pos],
                            albumsAll[pos].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                }
            } else {
                val action =
                    LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                        albumsAll[position],
                        albumsAll[position].artists[0].name + ""
                    )
                navHostFragment.navController.navigate(action)
            }

        } else if (binding.tabLayout.selectedTabPosition == 1) {
            if (tracksSingle.isNotEmpty()) {
                if (position < tracksSingle.size) {
                    Log.d("track", "found")
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            tracksSingle[position].album,
                            tracksSingle[position].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val pos = position % tracksSingle.size
                    Log.d(
                        "album",
                        albumsSingle[pos].artists.size.toString() + " " + albumsSingle[pos].artists[0].name
                    )
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            albumsSingle[pos],
                            albumsSingle[pos].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                }
            } else {
                val action =
                    LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                        albumsSingle[position],
                        albumsSingle[position].artists[0].name + ""
                    )
                navHostFragment.navController.navigate(action)
            }

        } else if (binding.tabLayout.selectedTabPosition == 2) {
            if (tracksAlbum.isNotEmpty()) {
                if (position < tracksAlbum.size) {
                    Log.d("track", "found")
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            tracksAlbum[position].album,
                            tracksAlbum[position].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val pos = position % tracksAlbum.size
                    Log.d(
                        "album",
                        albumsAll[pos].artists.size.toString() + " " + albumsAll[pos].artists[0].name
                    )
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            albumsAll[pos],
                            albumsAll[pos].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                }
            } else {
                val action =
                    LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                        albumsAll[position],
                        albumsAll[position].artists[0].name + ""
                    )
                navHostFragment.navController.navigate(action)
            }
        } else if (binding.tabLayout.selectedTabPosition == 3) {
            if (tracksCompilation.isNotEmpty()) {
                if (position < tracksCompilation.size) {
                    Log.d("track", "found")
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            tracksCompilation[position].album,
                            tracksCompilation[position].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val pos = position % tracksCompilation.size
                    Log.d(
                        "album",
                        albumsCompilation[pos].artists.size.toString() + " " + albumsCompilation[pos].artists[0].name
                    )
                    val action =
                        LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                            albumsCompilation[pos],
                            albumsCompilation[pos].artists[0].name + ""
                        )
                    navHostFragment.navController.navigate(action)
                }
            } else {
                val action =
                    LibraryFragmentDirections.actionLibraryFragmentToAlbumDetailsFragment(
                        albumsCompilation[position],
                        albumsCompilation[position].artists[0].name + ""
                    )
                navHostFragment.navController.navigate(action)
            }

        }

    }


    override fun onAddToLibrary(albumItems: AlbumItems) {

    }

    override fun onRemoveFromLibrary(albumItems: AlbumItems) {

    }

    override fun onArtistClick(name: String, id: String, image: String) {

    }

}