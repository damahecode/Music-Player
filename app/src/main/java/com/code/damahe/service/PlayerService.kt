package com.code.damahe.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.code.damahe.res.R
import com.code.damahe.app.Config

interface PlayerListener {
    fun updateOnChange()
}

class PlayerService : Service() {

    private val binder = MusicBinder()

    private var mediaManager: MediaManager? = null
    private var nManager: LeafNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    var playerListener: PlayerListener? = null
    private var notificationReceiver: PlayerService.NotificationReceiver? = null

    fun setListener(listener: PlayerListener) {
        playerListener = listener
    }

    inner class MusicBinder: Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        if (mediaManager == null) {
            mediaSession = MediaSessionCompat(this, getString(R.string.app_name))
            nManager = LeafNotificationManager(this)
            mediaManager = MediaManager(this)
            registerActionsReceiver()
        }
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // Acquire wake lock to prevent service from being paused by the system.
        val wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Leaf_Music:player_service_lock")
        }
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun getMediaSession(): MediaSessionCompat? {
        return mediaSession
    }

    fun getMediaManager(): MediaManager? {
        return mediaManager
    }

    fun getNotificationManager(): LeafNotificationManager? {
        return nManager
    }

    inner class NotificationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action

            if (action != null) {
                when(action) {
                    Config.ACTION_PREV -> mediaManager?.prev()
                    Config.ACTION_PLAY_PAUSE -> mediaManager?.playPause()
                    Config.ACTION_NEXT -> mediaManager?.next()
                    Config.ACTION_STOP -> mediaManager?.release()
                }
            }
        }
    }

    private fun registerActionsReceiver() {
        notificationReceiver = NotificationReceiver()
        val intentFilter = IntentFilter()

        intentFilter.addAction(Config.ACTION_PREV)
        intentFilter.addAction(Config.ACTION_PLAY_PAUSE)
        intentFilter.addAction(Config.ACTION_NEXT)
        intentFilter.addAction(Config.ACTION_STOP)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, intentFilter)
        }
    }

    private fun unregisterActionsReceiver() {
        if (notificationReceiver != null) {
            try {
                unregisterReceiver(notificationReceiver)
            } catch (e: IllegalArgumentException) {
                Log.d(this::class.java.name, e.message ?: "error : unregisterActionsReceiver()")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActionsReceiver()
    }
}