package com.code.damahe.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code.damahe.app.MusicEvent
import com.code.damahe.app.PlayerState
import com.code.damahe.data.PlayerRepository
import com.code.damahe.modal.Music
import com.code.damahe.modal.MusicControllerUiState
import com.code.damahe.modal.getAudioList
import com.code.damahe.service.PlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val playerRepo: PlayerRepository
): ViewModel() {

    private val context = WeakReference(context)
    private var runningPosition = false

    val mBound = playerRepo.playerBuilder.mBound

    fun getService(): PlayerService? {
        return playerRepo.getService()
    }

    var musicControllerUiState by mutableStateOf(MusicControllerUiState())
        private set

    val getAllAudio: StateFlow<List<Music>> = playerRepo.allAudioList

    fun fetchAllAudio(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            playerRepo.allAudioList.emit(getAudioList(context))
        }
    }

    fun bindService() {
        if (playerRepo.getService() == null)
            playerRepo.playerBuilder.bindService()
    }

    fun unBindService() {
        if (playerRepo.getService() != null)
            playerRepo.playerBuilder.unBindService()
    }

    fun setMusic(index: Int, list: List<Music> = emptyList()) {
        playerRepo.getMediaManager()?.setMusic(index, list)
    }

    fun updateMusicState() {
        musicControllerUiState = musicControllerUiState.copy(
            playerState = playerRepo.getPlayerState(),
            currentSong = playerRepo.getCurrentMusic(),
            currentPosition = playerRepo.getCurrentPosition(),
            totalDuration = playerRepo.getDuration(),
        )
    }

    fun updateCurrentPosition(run: Boolean) {
//        viewModelScope.launch {
//            while (true) {
//                if (playerRepo.getPlayerState() == PlayerState.PLAYING) {
//                    delay(2.seconds)
//                    musicControllerUiState = musicControllerUiState.copy(
//                        currentPosition = playerRepo.getCurrentPosition()
//                    )
//                }
//            }
//        }

        viewModelScope.launch {
            if (!runningPosition) {
                runningPosition = run
                try {
                    while (runningPosition) {
                        if (playerRepo.getPlayerState() == PlayerState.PLAYING) {
                            musicControllerUiState = musicControllerUiState.copy(
                                currentPosition = playerRepo.getCurrentPosition()
                            )
                        }
                        delay(1.seconds)
                    }
                } catch (e: Exception) {
                    runningPosition = false
                    Log.d(PlayerViewModel::class.java.name, e.message ?: "Exception : updateCurrentPosition()")
                }
            } else {
                runningPosition = run
            }
        }
    }

    fun onEvent(event: MusicEvent) {
        when (event) {
            MusicEvent.PauseResumeSong -> playerRepo.playPause()
            is MusicEvent.SeekSongToPosition -> playerRepo.seekTo(event.position)
            MusicEvent.SkipToNextSong -> playerRepo.next()
            MusicEvent.SkipToPreviousSong -> playerRepo.prev()
        }
    }
}