package com.kaizm.learnmusicplayer

import android.content.*
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaizm.learnmusicplayer.MyService.Companion.MUSIC_PAUSE
import com.kaizm.learnmusicplayer.MyService.Companion.MUSIC_RESUME
import com.kaizm.learnmusicplayer.databinding.ActivityMainBinding
import java.lang.RuntimeException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var musicAdapter: MusicAdapter
    private var myService: MyService? = null
    var isConnected: Boolean = false
    val musicFragment = MusicFragment()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyService.MyBinder
            myService = binder.getService()
            isConnected = true
            Log.e("AAA", "onServiceConnected: Success")
            hideFooter()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, musicFragment).addToBackStack("music fragment")
                .commit()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            myService = null
            isConnected = false
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.getBoolean("status")?.let {
                if (it) {
                    binding.btnFooterPlay.background =
                        getDrawable(R.drawable.ic_baseline_pause_circle_24)
                } else {
                    binding.btnFooterPlay.background =
                        getDrawable(R.drawable.ic_baseline_play_circle_24)
                }
            }
            intent?.extras?.getBoolean("stop")?.let {
                if (it) {
                    cancelService()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideFooter()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("action_to_main"))

        musicAdapter = MusicAdapter {
            val bundle = Bundle().apply {
                putSerializable("music", it)
            }
            musicFragment.arguments = bundle

            cancelService()

            val intent = Intent(this, MyService::class.java).apply {
                putExtras(Bundle().apply {
                    putSerializable("music", it)
                })
            }
            startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)


            binding.footerName.text = it.name
            binding.footerAuthor.text = it.author


        }.apply {
            listMusic = Music.getList()
        }

        binding.rvMusic.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.btnFooterPlay.setOnClickListener {
            myService?.let {
                if (it.mediaPlayer.isPlaying) {
                    it.mediaPlayer.pause()
                    binding.btnFooterPlay.background =
                        getDrawable(R.drawable.ic_baseline_play_circle_24)

                    val intent = Intent(this@MainActivity, MyService::class.java).apply {
                        putExtras(Bundle().apply {
                            putInt("action_receiver", MUSIC_PAUSE)
                        })
                    }
                    startService(intent)
                } else {
                    it.mediaPlayer.start()
                    binding.btnFooterPlay.background =
                        getDrawable(R.drawable.ic_baseline_pause_circle_24)

                    val intent = Intent(this@MainActivity, MyService::class.java).apply {
                        putExtras(Bundle().apply {
                            putInt("action_receiver", MUSIC_RESUME)
                        })
                    }
                    startService(intent)
                }
            }
        }

        binding.btnFooterCancel.setOnClickListener {
            cancelService()
        }
    }

    fun cancelService() {
        if (isConnected) {
            supportFragmentManager.beginTransaction().remove(musicFragment).commit()
            stopService(Intent(this, MyService::class.java))
            unbindService(connection)
            hideFooter()
            isConnected = false
        }
    }

    fun showFooter() {
        binding.footerContainer.visibility = View.VISIBLE
        binding.btnFooterPlay.background = getDrawable(R.drawable.ic_baseline_pause_circle_24)
    }

    fun hideFooter() {
        binding.footerContainer.visibility = View.GONE
    }


    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, MyService::class.java))
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }


    suspend fun getMedia() = myService?.mediaPlayer
    fun getConnection() = connection
}