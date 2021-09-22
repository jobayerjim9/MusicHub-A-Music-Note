package com.musichub.app.ui.fragments

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
import com.musichub.app.models.genius.Song
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class TrackDetailsFragment : Fragment() {
    private val args: TrackDetailsFragmentArgs by navArgs()
    lateinit var binding:FragmentTrackDetailsBinding
    lateinit var viewModel:TrackViewModel
    lateinit var navHostFragment: NavHostFragment
    lateinit var mediaPlayer: MediaPlayer
    private var countDownTimer:CountDownTimer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_track_details, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        navHostFragment=requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        binding.name=args.name
        binding.image=args.image

        binding.bio="Loading..."
        viewModel= ViewModelProvider(this).get(TrackViewModel::class.java)
        viewModel.getSongBio(args.songId)
        viewModel.getSongDetails(args.songId)
        viewModel.songBio.observe(viewLifecycleOwner,{
            binding.bio=it
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
        binding.primaryArtist.text=it.primary_artist.name

        val writerAdapter= SongArtistAdapter(requireContext(),it.writer_artists)
        binding.writerRecycler.layoutManager= LinearLayoutManager(requireContext())
        binding.writerRecycler.adapter=writerAdapter

        val producerAdapter= SongArtistAdapter(requireContext(),it.producer_artists)
        binding.producerRecycler.layoutManager= LinearLayoutManager(requireContext())
        binding.producerRecycler.adapter=producerAdapter

        val performanceAdapter= PerformanceArtistAdapter(requireContext(),it.custom_performances)
        binding.performanceRecycler.layoutManager= LinearLayoutManager(requireContext())
        binding.performanceRecycler.adapter=performanceAdapter
        var youtubeFound=false
        var soundcloudFound=false

        for(media in it.media) {
            if (media.provider == "youtube") {
                youtubeFound=true
                binding.youtubeIcon.setOnClickListener { v->
                    val uri=Uri.parse(media.url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            else if (media.provider == "soundcloud") {
                soundcloudFound=true
                binding.soundcloud.setOnClickListener { v->
                    val uri=Uri.parse(media.url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
        }
        if (!youtubeFound) {
            binding.youtubeIcon.alpha = 0.5f
        }
        if (!soundcloudFound) {
            binding.soundcloud.alpha = 0.5f
        }

    }

}