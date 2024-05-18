package com.code.damahe.data

import android.content.Context
import com.code.damahe.app.PlayerState
import com.code.damahe.modal.Music
import com.code.damahe.modal.getAudioList
import com.code.damahe.service.MediaManager
import com.code.damahe.service.PlayerBuilder
import com.code.damahe.service.PlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(@ApplicationContext context: Context, val playerBuilder: PlayerBuilder) {

    val allAudioList = MutableStateFlow(getAudioList(context))

    fun getService(): PlayerService? {
        return playerBuilder.getService()
    }

    fun getMediaManager(): MediaManager? {
        return getService()?.getMediaManager()
    }

    fun getPlayerState() = if (getMediaManager()?.isPlaying() == true) PlayerState.PLAYING else PlayerState.PAUSED

    fun getCurrentMusic(): Music? {
        return getMediaManager()?.getCurrentMusic()
    }

    fun getCurrentPosition(): Long {
        return getMediaManager()?.getCurrentPosition() ?: 0
    }

    fun getDuration(): Long {
        return getMediaManager()?.getDuration() ?: 0
    }

    fun seekTo(pos: Long) {
        getMediaManager()?.seekTo(pos)
    }

    fun playPause() {
        getMediaManager()?.playPause()
    }

    fun prev() {
        getMediaManager()?.prev()
    }

    fun next() {
        getMediaManager()?.next()
    }
}