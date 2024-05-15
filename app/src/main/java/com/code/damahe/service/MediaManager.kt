package com.code.damahe.service

import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.ContentUris
import android.content.Intent
import android.media.MediaPlayer
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.code.damahe.modal.Music

class MediaManager(private val service: PlayerService) : MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val tag = MediaManager::class.java.name
    private var notificationManager: LeafNotificationManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex: Int = 0
    private val musicList = ArrayList<Music>()

    fun getCurrentMusic(): Music {
        return musicList[currentIndex]
    }

    private fun initMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer?.reset()
        } else {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.setOnCompletionListener(this)
            mediaPlayer?.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK)
            notificationManager = service.getNotificationManager()
        }

        try {
            val trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                getCurrentMusic().id.toLong()
            )

            mediaPlayer?.setDataSource(service, trackUri)
            mediaPlayer?.prepareAsync()
            setPlayerState()
        } catch (e: IllegalStateException) {
            Log.d(tag, e.message ?: " check : initMediaPlayer() -> try block")
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun getDuration(): Long {
        val duration = (mediaPlayer?.duration ?: 0).toLong()

        if (duration < 0) {
            return 0
        }
        return duration
    }

    fun getCurrentPosition(): Long {
        return (mediaPlayer?.currentPosition ?: 0).toLong()
    }

    fun setMusic(index: Int, list: List<Music> = emptyList()) {
        currentIndex = index
        musicList.clear()
        musicList.addAll(list)

        initMediaPlayer()
    }

    val mediaSessionCallback = object: MediaSessionCompat.Callback() {
        override fun onPlay() {
            playPause()
        }
        override fun onPause() {
            playPause()
        }
        override fun onSkipToPrevious() {
            prev()
        }
        override fun onSkipToNext() {
            next()
        }
        override fun onStop() {
            release()
        }
        override fun onSeekTo(pos: Long) {
            seekTo(pos)
        }
        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            return super.onMediaButtonEvent(mediaButtonIntent)
        }
    }

    private fun setPlayerState() {
        notificationManager?.updateNotification(true)

        if (service.playerListener != null) {
            service.playerListener?.updateOnChange()
        }
    }

    fun playPause() {
        if (isPlaying()) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        setPlayerState()
    }

    fun seekTo(pos: Long) {
        mediaPlayer?.seekTo(pos.toInt())
        setPlayerState()
    }

    fun prev() {
        currentIndex = if (isCurrentPositionOutOfBound(currentIndex - 1)) musicList.size - 1 else --currentIndex
        initMediaPlayer()
    }

    fun next() {
        currentIndex = if (isCurrentPositionOutOfBound(currentIndex + 1)) 0 else ++currentIndex
        initMediaPlayer()
    }

    private fun isCurrentPositionOutOfBound(pos: Int): Boolean {
        return pos >= musicList.size || pos <= 0
    }

    fun release() {
        service.stopForeground(STOP_FOREGROUND_REMOVE)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCompletion(mp: MediaPlayer) {
        // Handle song completion (e.g., play next song)
        if (musicList.size > 1 && currentIndex + 1 != musicList.size)
            next()
        else
            release()
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()
        notificationManager?.updateNotification()
    }
}