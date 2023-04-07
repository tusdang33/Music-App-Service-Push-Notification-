package com.kaizm.learnmusicplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kaizm.learnmusicplayer.AppNotification.Companion.CHANNEL_ID

class MyService : Service() {
    lateinit var mediaPlayer: MediaPlayer
    private var music: Music? = null

    companion object {
        const val MUSIC_STOP = 101
        const val MUSIC_PAUSE = 102
        const val MUSIC_RESUME = 103
    }

    inner class MyBinder : Binder() {
        fun getService(): MyService {
            return this@MyService
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        return MyBinder()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()


    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(music == null){
            music = intent?.extras?.getSerializable("music") as? Music
        }
        if(!mediaPlayer.isPlaying){
            music?.let {
                playMusic(it)
                pushNotification()
            }
        }

        Log.e("STCM", "onStartCommand music: $music")

        val action = intent?.extras?.getInt("action_receiver", 0) as? Int
        Log.e("AAA", "onStartCommand action: $action")
        action?.let { handlerAction(it) }

        return START_NOT_STICKY
    }

    private fun playMusic(music: Music) {
        mediaPlayer = MediaPlayer.create(this, music.music)
        mediaPlayer.start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun pushNotification() {
        val remoteViews = RemoteViews(packageName, R.layout.item_notification).apply {
            setTextViewText(R.id.name, music!!.name)
            setTextViewText(R.id.author, music!!.author)
            setImageViewBitmap(
                R.id.imgNoti,
                BitmapFactory.decodeResource(resources, R.drawable.syt)
            )

            if (mediaPlayer.isPlaying) {
                setOnClickPendingIntent(
                    R.id.btnNotiPlay,
                    getPendingIntent(this@MyService, MUSIC_PAUSE)
                )
                setImageViewResource(R.id.btnNotiPlay, R.drawable.ic_baseline_pause_circle_24)
            } else {
                setOnClickPendingIntent(
                    R.id.btnNotiPlay,
                    getPendingIntent(this@MyService, MUSIC_RESUME)
                )
                setImageViewResource(R.id.btnNotiPlay, R.drawable.ic_baseline_play_circle_24)
            }

            setOnClickPendingIntent(
                R.id.btnNotiCancel,
                getPendingIntent(this@MyService, MUSIC_STOP)
            )
        }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .build()

        startForeground(1, notification)
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(this, MyReceiver::class.java).apply {
            putExtras(Bundle().apply {
                putInt("action_service", action)
            })
        }

        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handlerAction(action: Int) {
        when (action) {
            MUSIC_STOP -> musicStop()
            MUSIC_RESUME -> musicResume()
            MUSIC_PAUSE -> musicPause()
        }

    }

    private fun sendStatusToActivity(){
        val intent = Intent("action_to_main").apply {
            putExtras(Bundle().apply {
                putBoolean("status", mediaPlayer.isPlaying)
            })
        }
        LocalBroadcastManager.getInstance(this,).sendBroadcast(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun musicPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            pushNotification()
            sendStatusToActivity()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun musicResume() {
        mediaPlayer.start()
        pushNotification()
        sendStatusToActivity()
    }

    private fun musicStop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            val intent = Intent("action_to_main").apply {
                putExtras(Bundle().apply {
                    putBoolean("stop", true)
                })
            }
            LocalBroadcastManager.getInstance(this,).sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}