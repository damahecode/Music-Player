package com.code.damahe.app

object Config {

    const val CHANNEL_ID = "leaf_music_player_channel"
    const val NOTIFICATION_ID = 1056
    const val ACTION_PREV = "com.code.damahe.leaf.music.ACTION_PREV"
    const val ACTION_PLAY_PAUSE = "com.code.damahe.leaf.music.ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "com.code.damahe.leaf.music.ACTION_NEXT"
    const val REQUEST_CODE = 108
}

enum class PlayerState {
    PLAYING,
    PAUSED,
    STOPPED
}

sealed class MusicEvent {
    data object PauseResumeSong : MusicEvent()
    data object SkipToNextSong : MusicEvent()
    data object SkipToPreviousSong : MusicEvent()
    data class SeekSongToPosition(val position: Long) : MusicEvent()
}