package com.musichub.app.ui.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.musichub.app.R
import com.musichub.app.adapters.PerformanceArtistAdapter
import com.musichub.app.adapters.SongArtistAdapter
import com.musichub.app.databinding.FragmentTrackDetailsBinding
import com.musichub.app.viewmodels.AlbumViewModel
import com.musichub.app.viewmodels.TrackViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import com.musichub.app.adapters.SongRelationshipAdapter
import com.musichub.app.helpers.listeners.OnArtistClick
import com.musichub.app.helpers.listeners.RelationshipItemClick
import com.musichub.app.models.genius.Song
import com.musichub.app.models.genius.SongRelationships
import com.musichub.app.viewmodels.ArtistViewModel
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class TrackDetailsFragment : Fragment(), OnArtistClick, RelationshipItemClick {
    private val args: TrackDetailsFragmentArgs by navArgs()
    lateinit var binding: FragmentTrackDetailsBinding
    lateinit var viewModel: TrackViewModel
    lateinit var artistViewModel: ArtistViewModel
    lateinit var navHostFragment: NavHostFragment
    lateinit var mediaPlayer: MediaPlayer
    private var countDownTimer: CountDownTimer? = null
    private var launched = false
    private var launchedSample = false
    private var artistName = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_track_details, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        navHostFragment=requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        binding.name=args.name
        binding.image=args.image

        binding.bio="Loading..."
        viewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
        artistViewModel = ViewModelProvider(this).get(ArtistViewModel::class.java)
        viewModel.getSongBio(args.songId)
        viewModel.getSongDetails(args.songId)
        viewModel.songBio.observe(viewLifecycleOwner,{
            if (it.length == 1) {
                binding.bio = "No bio on Genius"
            } else {
                binding.bio = it
            }

        })
        viewModel.song.observe(viewLifecycleOwner,{
            populateSongInfo(it)
        })
        binding.back.setOnClickListener {
            navHostFragment.navController.popBackStack()
        }
        if (args.previewUrl!=null) {
            binding.previewLayout.visibility = View.VISIBLE
            mediaPlayer= MediaPlayer()
            mediaPlayer.setDataSource(args.previewUrl)
            mediaPlayer.prepare()
            binding.musicProgress.max=mediaPlayer.duration
        }
        binding.playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                countDownTimer!!.cancel()
                binding.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_play_arrow_24))
            }
            else {
                mediaPlayer.start()
                binding.playButton.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_pause_24))
                seekbarUpdate((mediaPlayer.duration - mediaPlayer.currentPosition).toLong())
            }


        }
        binding.musicProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    seekbarUpdate((mediaPlayer.duration - mediaPlayer.currentPosition).toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        setupObservers()

    }

    private fun setupObservers() {
        artistViewModel.foundArtist.observe(viewLifecycleOwner, {
            if (!launched) {
                if (it.images!!.isNotEmpty()) {
                    val action =
                        TrackDetailsFragmentDirections.actionTrackDetailsFragmentToArtistDetailsFragment(
                            it.name,
                            it.id,
                            it.images[0].url
                        )
                    navHostFragment.navController.navigate(action)
                } else {
                    val action =
                        TrackDetailsFragmentDirections.actionTrackDetailsFragmentToArtistDetailsFragment(
                            it.name,
                            it.id,
                            ""
                        )
                    navHostFragment.navController.navigate(action)
                }
                launched = true
            }
        })
        viewModel.foundSample.observe(viewLifecycleOwner, {
            if (!launchedSample) {
                val action =
                    TrackDetailsFragmentDirections.actionTrackDetailsFragmentToAlbumDetailsFragment(
                        it.album,
                        artistName
                    )
                navHostFragment.navController.navigate(action)
                launchedSample = true
            }
        })
    }

    private fun seekbarUpdate(duration: Long) {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    binding.musicProgress.progress = (mediaPlayer.duration - millisUntilFinished).toInt()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            }

            override fun onFinish() {

            }
        }.start()
    }
    private fun populateSongInfo(it: Song) {

        binding.primaryArtist.text = it.primary_artist.name

        val writerAdapter = SongArtistAdapter(requireContext(), it.writer_artists, this)
        binding.writerRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.writerRecycler.adapter = writerAdapter

        val featuredArtistAdapter = SongArtistAdapter(requireContext(), it.featured_artists, this)
        binding.featuredArtist.layoutManager = LinearLayoutManager(requireContext())
        binding.featuredArtist.adapter = featuredArtistAdapter
        if (it.featured_artists.size == 0) {
            binding.fArtist.visibility = View.GONE
        }

        val producerAdapter = SongArtistAdapter(requireContext(), it.producer_artists, this)
        binding.producerRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.producerRecycler.adapter = producerAdapter

        val performanceAdapter =
            PerformanceArtistAdapter(requireContext(), it.custom_performances, this)
        binding.performanceRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.performanceRecycler.adapter = performanceAdapter
        val relations: ArrayList<SongRelationships> = ArrayList()
        for (relation in it.song_relationships) {
            if (relation.songs.isNotEmpty()) {
                relations.add(relation)
            }
        }


        val relationshipAdapter = SongRelationshipAdapter(requireContext(), relations, this)
        binding.relationshipRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.relationshipRecycler.adapter = relationshipAdapter


        var youtubeFound = false
        var soundcloudFound = false

        for (media in it.media) {
            if (media.provider == "youtube") {
                youtubeFound = true
                binding.youtubeIcon.setOnClickListener { v ->
                    val uri = Uri.parse(media.url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            } else if (media.provider == "soundcloud") {
                soundcloudFound = true
                binding.soundcloud.setOnClickListener { v ->
                    val uri = Uri.parse(media.url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
        }
        if (!youtubeFound) {
            binding.youtubeIcon.visibility = View.GONE
        }
        if (!soundcloudFound) {
            binding.soundcloud.visibility = View.GONE
        }
        binding.geniusDetails.setOnClickListener { v ->
            val uri =
                Uri.parse(it.url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)

        }

    }

    override fun onArtistClick(name: String) {
        launched = false
        artistViewModel.searchArtistSpotify(name)
    }

    override fun onRelationShipClick(name: String, artist: String) {

        launchedSample = false
        artistName = artist
        viewModel.getAlbumByTrack(name, artist)
    }

}