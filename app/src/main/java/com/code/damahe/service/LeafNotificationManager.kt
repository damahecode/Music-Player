package com.code.damahe.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.code.damahe.activity.MainActivity
import com.code.damahe.app.Config
import com.code.damahe.modal.Music
import com.code.damahe.modal.getDominantColor
import com.code.damahe.modal.getImage
import com.code.damahe.modal.getImgArt
import com.code.damahe.res.R

class LeafNotificationManager(private val service: PlayerService) {

    private val context = service.applicationContext
    private var notificationManager: NotificationManager? = null
    private var notificationBuilder: NotificationCompat.Builder? = null

    private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    init {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager?.getNotificationChannel(Config.CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    Config.CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.description = context.getString(R.string.app_name)
                channel.enableLights(false)
                channel.enableVibration(false)
                channel.setShowBadge(false)

                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    fun updateNotification(update: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(service, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            val music = service.getMediaManager()?.getCurrentMusic()
            val mediaSession = service.getMediaSession()
            val imgArt = getImgArt(service, music?.trackUri)
            val isPlaying = service.getMediaManager()?.isPlaying() ?: false

            val openPlayerIntent = Intent(context, MainActivity::class.java)
            openPlayerIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            val contentIntent = PendingIntent.getActivity(context, Config.REQUEST_CODE, openPlayerIntent, flag)

            if (!update) {
                createNotificationChannel()

                notificationBuilder = NotificationCompat.Builder(context, Config.CHANNEL_ID)
                    .setShowWhen(false)
                    .setOngoing(true)
                    .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Replace with your icon drawable
                    .setContentTitle(music?.title)
                    .setContentText(music?.artist)
                    .setSubText(music?.album)
                    .setLargeIcon(getImage(imgArt))
                    .setColor(getDominantColor(getImage(imgArt)))
                    .setColorized(false)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession?.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .addAction(notificationAction(Config.ACTION_PREV))
                    .addAction(notificationAction(Config.ACTION_PLAY_PAUSE))
                    .addAction(notificationAction(Config.ACTION_NEXT))
                    .addAction(notificationAction(Config.ACTION_STOP))

                getPlayBackState(music)
                service.startForeground(Config.NOTIFICATION_ID, notificationBuilder?.build())

            } else {
                notificationBuilder?.let {
                    if (it.mActions.size > 0)
                        it.mActions[1] = notificationAction(Config.ACTION_PLAY_PAUSE)
                    it.setOngoing(isPlaying)

                    getPlayBackState(music, true)
                    NotificationManagerCompat.from(service).notify(Config.NOTIFICATION_ID, it.build())
                }
            }
        }
    }

    private fun getPlayBackState(music: Music?, update: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val isPlaying = service.getMediaManager()?.isPlaying() ?: false
            val getCurrentPosition = service.getMediaManager()?.getCurrentPosition() ?: 0
            val mediaSession = service.getMediaSession()
            val getDuration = service.getMediaManager()?.getDuration() ?: 0
            val playbackSpeed = if (isPlaying) 1F else 0F
            val imgArt = getImgArt(service, music?.trackUri)

            if (!update) {
                mediaSession?.setCallback(service.getMediaManager()?.mediaSessionCallback)
                mediaSession?.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getImage(imgArt))
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration)
                        .build()
                )
            }

            val playbackState = PlaybackStateCompat.Builder().setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED, getCurrentPosition, playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO)
                .build()

            mediaSession?.setPlaybackState(playbackState)
        }
    }

    private fun playerAction(action: String): PendingIntent {
        val intent = Intent().also {
            it.action = action
        }
        return PendingIntent.getBroadcast(context, Config.REQUEST_CODE, intent, flag)
    }

    private fun notificationAction(action: String): NotificationCompat.Action {
        val icon = when(action) {
            Config.ACTION_PREV -> android.R.drawable.ic_media_previous
            Config.ACTION_PLAY_PAUSE -> if (service.getMediaManager()!!.isPlaying()) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            Config.ACTION_NEXT -> android.R.drawable.ic_media_next
            Config.ACTION_STOP -> android.R.drawable.ic_menu_close_clear_cancel
            else -> android.R.drawable.ic_media_pause
        }

        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }
}