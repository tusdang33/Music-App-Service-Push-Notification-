package com.kaizm.learnmusicplayer

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kaizm.learnmusicplayer.databinding.FragmentMusicBinding
import kotlinx.coroutines.*
import java.lang.Runnable


class MusicFragment : Fragment() {
    private lateinit var binding: FragmentMusicBinding
    private lateinit var music: Music
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private val handle = Handler()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            lifecycleScope.launch(Dispatchers.IO) {
                mediaPlayer = async {
                    context.getMedia()!!
                }.await()
            }
        } else {
            throw RuntimeException("$context must be an instance of MainActivity")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater, container, false)
        arguments?.let {
            music = it.getSerializable("music") as Music
        }
        lifecycleScope.launch {
            fillView(music)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launch(Dispatchers.Main) {
            runnable = Runnable {
                binding.tvStart.text = timeFormat(mediaPlayer.currentPosition / 1000)
                Log.e("AAA", "postition ${mediaPlayer.currentPosition / 1000}")
                binding.sbMusic.progress = mediaPlayer.currentPosition
                handle.postDelayed(runnable, 1000)
            }
            handle.postDelayed(runnable, 2000)
        }

        mediaPlayer.setOnCompletionListener { mediaPlayer ->
            mediaPlayer.stop()
            binding.btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_24)
        }

        binding.sbMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mediaPlayer.seekTo(p1)
                    binding.tvStart.text = timeFormat(mediaPlayer.currentPosition / 1000)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.btnPlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                it.setBackgroundResource(R.drawable.ic_baseline_play_circle_24)
            } else {
                mediaPlayer.start()
                it.setBackgroundResource(R.drawable.ic_baseline_pause_circle_24)
            }
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnNext.setOnClickListener {
            music = if (music.id == Music.getList().size) {
                Music.getList()[0]
            } else {
                Music.getList()[music.id]
            }
            requestService(music)
        }

        binding.btnPrevious.setOnClickListener {
            music = if (music.id == 1) {
                Music.getList()[Music.getList().size - 1]
            } else {
                Music.getList()[music.id - 2]
            }
            requestService(music)
        }
    }

    private fun requestService(music: Music) {
        val intent = Intent(requireContext(), MyService::class.java).apply {
            putExtras(Bundle().apply {
                putSerializable("music", music)
            })
        }
        (activity as MainActivity).cancelService()
        requireContext().startService(intent)
        requireContext().bindService(
            intent,
            (activity as MainActivity).getConnection(),
            Context.BIND_AUTO_CREATE
        )
        handle.removeCallbacks(runnable)
        fillView(music)
        handle.postDelayed(runnable, 2000)
    }

    private fun fillView(music: Music) {
        binding.imgAvatar.setImageBitmap(music.img)
        binding.tvName.text = music.name
        binding.sbMusic.progress = 0
        binding.sbMusic.max = mediaPlayer.duration
        binding.tvStart.text = String.format("%d:%02d", 0, 0)

        binding.tvEnd.text = timeFormat(mediaPlayer.duration / 1000)
    }

    private fun timeFormat(currentInSeconds: Int): String {
        val minutes = currentInSeconds / 60
        val second = currentInSeconds % 60
        return String.format("%d:%02d", minutes, second)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showFooter()
        lifecycleScope.cancel()
        handle.removeCallbacks(runnable)
    }

}