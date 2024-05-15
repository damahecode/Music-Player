package com.code.damahe.modal

import com.code.damahe.app.PlayerState

data class MusicControllerUiState(
    val playerState: PlayerState? = null,
    val currentSong: Music? = null,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
)