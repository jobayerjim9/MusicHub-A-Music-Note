package com.musichub.app.ui.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.musichub.app.BuildConfig
import com.musichub.app.R
import com.musichub.app.adapters.TrackAdapter
import com.musichub.app.databinding.FragmentAlbumDetailsBinding
import com.musichub.app.helpers.listeners.RecyclerViewItemClick
import com.musichub.app.models.spotify.AlbumItems
import com.musichub.app.models.spotify.TrackItems
import com.musichub.app.viewmodels.AlbumViewModel
import com.musichub.app.viewmodels.ArtistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumDetailsFragment : Fragment(), RecyclerViewItemClick {
    private val args: AlbumDetailsFragmentArgs by navArgs()
    private lateinit var viewModel: AlbumViewModel
    private lateinit var artistViewModel: ArtistViewModel
    private lateinit var binding:FragmentAlbumDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    private val tracks:ArrayList<TrackItems> = ArrayList()
    private lateinit var trackAdapter:TrackAdapter
    private val libraryItems : ArrayList<AlbumItems> = ArrayList()
    lateinit var progress:ProgressDialog
    lateinit var albumItems: AlbumItems
    var currentPosition=0
    var launched=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_album_details, container, false)
        initView()
        return binding.root

    }

    private fun initView() {

        progress=ProgressDialog(requireContext())
        progress.setTitle("Searching...!")
        progress.setMessage("Searching Track Details On Genius.com")
        navHostFragment=requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        viewModel= ViewModelProvider(this).get(AlbumViewModel::class.java)
        artistViewModel= ViewModelProvider(this).get(ArtistViewModel::class.java)
        albumItems = args.albumIten
        artistViewModel.getLibraryItems()
        artistViewModel.libraryItems.observe(viewLifecycleOwner,{
            for (album in it) {
                if (album.id==albumItems.id) {
                    albumItems.inLibrary=true
                    break
                }
            }
            if (albumItems.inLibrary == true) {
                inLibraryState()
            }
            else {
                notInLibrary()
            }

        })
        viewModel.getAlbumTracks(albumItems.id!!)
        binding.albumName=albumItems.name
        binding.artistName=args.artistName
        binding.image=albumItems.images!![0].url
        binding.releaseDate=albumItems.release_date
        if (albumItems.album_type=="appears_on") {
            binding.albumNameType.text=albumItems!!.name+" (Featured)"
        }
        else if (albumItems.album_type=="album") {
            binding.albumNameType.text=albumItems!!.name+" (Album)"
        }
        else if (albumItems.album_type=="single") {
            binding.albumNameType.text=albumItems!!.name+" (Single/EP)"
        }
        binding.back.setOnClickListener {
            navHostFragment.navController.popBackStack()
        }
        trackAdapter= TrackAdapter(requireContext(),tracks,this)
        binding.trackRecycler.layoutManager= LinearLayoutManager(requireContext())
        binding.trackRecycler.adapter=trackAdapter
        binding.addToLibrary.setOnClickListener {
            if (albumItems!=null) {
                artistViewModel.addToLibrary(albumItems!!)
            }
        }
        viewModel.tracks.observe(viewLifecycleOwner,{
            tracks.clear()
            tracks.addAll(it.items)
            trackAdapter.notifyDataSetChanged()

        })
        viewModel.songShort.observe(viewLifecycleOwner,{
            if (!launched) {
                launched=true
                if (it.found) {
                    val action =
                        AlbumDetailsFragmentDirections.actionAlbumDetailsFragmentToTrackDetailsFragment(
                            trackName,
                            albumItems!!.images!![0].url,
                            it.songId,
                            tracks[currentPosition].preview_url
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Track Details Not Found On Genius.com",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            if (progress.isShowing) {
                progress.dismiss()
            }
        })
        binding.spotifyButton.setOnClickListener {
            val uri =
                Uri.parse(albumItems.external_urls?.spotify)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        binding.share.setOnClickListener {
            val shareIntent = Intent()
            val text="Checkout "+albumItems.name+" On MusicHub:- "+"https://musichub.com/album/"+albumItems.id
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,text )
            startActivity(Intent.createChooser(shareIntent,"Share via"))
        }
    }
    fun inLibraryState() {
        binding.cardColor = "#4E1E75"
        binding.libraryText.setTextColor(Color.parseColor("#FFFFFF"))
        binding.libraryText.text = "✔ Library"
        binding.addToLibrary.setOnClickListener {
            artistViewModel.removeFromLibrary(albumItems)
            binding.cardColor = "#FFFFFF"
            binding.libraryText.setTextColor(Color.parseColor("#001C6A"))
            binding.libraryText.text = "+ Library"
        }
    }
    fun notInLibrary() {
        binding.cardColor = "#FFFFFF"
        binding.libraryText.setTextColor(Color.parseColor("#001C6A"))
        binding.libraryText.text = "+ Library"
        binding.addToLibrary.setOnClickListener {
            artistViewModel.addToLibrary(albumItems)
            binding.cardColor = "#4E1E75"
            binding.libraryText.setTextColor(Color.parseColor("#FFFFFF"))
            binding.libraryText.text = "✔ Library"
        }
    }
    var trackName: String = ""
    override fun onItemClick(position: Int) {
        if (tracks[position].preview_url!=null) {
            Log.d("trackDetails", "found")
        }
        trackName = tracks[position].name
        launched = false
        progress.show()
        currentPosition=position
        viewModel.searchTrackOnGenius(tracks[position].name,tracks[position].artists[0].name)
    }

    override fun onAddToLibrary(albumItems: AlbumItems) {

    }

    override fun onRemoveFromLibrary(albumItems: AlbumItems) {

    }

    override fun onArtistClick(name: String, id: String, image: String) {

    }


}