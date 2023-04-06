package com.kaizm.learnmusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log


class MyReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val action = p1?.extras?.getInt("action_service", 0)
        val intent = Intent(p0, MyService::class.java).apply {
            putExtras(Bundle().apply {
                putInt("action_receiver", action!!)
            })
        }
        p0?.startService(intent)
    }
}