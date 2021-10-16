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
import androidx.recyclerview.widget.RecyclerView
import com.musichub.app.R
import com.musichub.app.adapters.ArtistsAdapter
import com.musichub.app.adapters.ArtistsShortAdapter
import com.musichub.app.databinding.FragmentArtistsBinding
import com.musichub.app.helpers.GridSpacingItemDecoration
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.FollowedArtist
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.SpotifyArtistItem
import com.musichub.app.viewmodels.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistsFragment : Fragment(),RecyclerViewItemClick {
    private lateinit var binding:FragmentArtistsBinding
    private lateinit var viewModel:ArtistViewModel
    private lateinit var artistsAdapter: ArtistsAdapter
    private lateinit var artistsShortAdapter: ArtistsShortAdapter
    val artists:ArrayList<SpotifyArtistItem> = ArrayList()
    val followedArtists:ArrayList<FollowedArtist> = ArrayList()
    var offset:Int=50
    var term:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_artists, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        Log.d("initView","initView")
        viewModel=ViewModelProvider(this).get(ArtistViewModel::class.java)
        viewModel.getFollowedArtist()
        artistsAdapter= ArtistsAdapter(requireContext(),artists,this)
        artistsShortAdapter = ArtistsShortAdapter(requireContext(),followedArtists,this)
        binding.artistRecycler.layoutManager= GridLayoutManager(requireContext(),3)
        binding.artistRecycler.adapter=artistsShortAdapter
        viewModel.followedArtist.observe(viewLifecycleOwner,{
            if (it.isNotEmpty()) {
                binding.descText.visibility = View.GONE
            }
            followedArtists.clear()
            followedArtists.addAll(it)
            artistsShortAdapter.notifyDataSetChanged()

        })
        val spanCount = 3
        val spacing = 48
        val includeEdge = false
        binding.artistRecycler.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))

        binding.artistRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager?
                    val total = layoutManager!!.itemCount
                    val currentLastItem = layoutManager!!.findLastVisibleItemPosition()
                    if (currentLastItem == total - 1) {
                        Log.d("recyclerPagination",term)
                        if (term.isNotEmpty() && artists.size >= 50) {
                            viewModel.searchArtist(term, offset)
                        }
                    }
            }
        })
        //binding.artistRecycler.layoutManager= StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL)

        binding.searchArtist.setOnEditorActionListener { textView, action, keyEvent ->
            var handled=false
            if (action==EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_NEXT) {
                artists.clear()
                term=binding.searchArtist.text.toString()
                Log.d("OnEditorActionListener",term)
                viewModel.searchArtist(term,0)
                handled=true
            }
            handled
        }
        viewModel.spotifyArtists.observe(viewLifecycleOwner, { artist ->
            term = binding.searchArtist.text.toString()
            Log.d("artistSearch", "got " + term)
            if (term.isNotEmpty()) {
                binding.artistRecycler.adapter = artistsAdapter
                binding.descText.visibility = View.GONE
                offset = artist.offset + 50
                artists.addAll(artist!!.items)
                artistsAdapter.notifyDataSetChanged()
            }
        })
        viewModel.isLoading.observe(viewLifecycleOwner, {
            binding.loading = it
        })
        binding.cancelButton.setOnClickListener {
            cancelSearch()
            it.hideKeyboard()
        }

    }

    private fun cancelSearch() {
        binding.searchArtist.clearFocus()
        binding.artistRecycler.adapter = artistsShortAdapter
        artists.clear()
        term = ""
        artistsAdapter.notifyDataSetChanged()
        binding.searchArtist.setText("")

    }

    fun View.hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getFollowedArtist()
    }

    override fun onItemClick(position: Int) {
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        if (binding.artistRecycler.adapter == artistsShortAdapter) {
            val artist=followedArtists[position]
            val action = ArtistsFragmentDirections.actionArtistsFragmentToArtistDetailsFragment(
                artist.name,
                artist.artistId,
                artist.image
            )
            navHostFragment.navController.navigate(action)
        }
        else {
            val artist = artists[position]
              if (artist.images!!.isNotEmpty()) {
                val action = ArtistsFragmentDirections.actionArtistsFragmentToArtistDetailsFragment(
                    artist.name,
                    artist.id,
                    artist.images[0].url
                )
                navHostFragment.navController.navigate(action)
            } else {
                val action = ArtistsFragmentDirections.actionArtistsFragmentToArtistDetailsFragment(
                    artist.name,
                    artist.id,
                    ""
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