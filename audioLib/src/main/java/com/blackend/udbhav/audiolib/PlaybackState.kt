package com.blackend.udbhav.audiolib

sealed class PlaybackState (val state: String) {

    data object Idle : PlaybackState ("IDLE")
    data object Buffering : PlaybackState ("BUFFERING")
    data object Ready : PlaybackState ("READY")
    data object Playing : PlaybackState ("PLAYING")
    data object Paused : PlaybackState ("PAUSED")
    data object Ended : PlaybackState ("ENDED")
    data object Error : PlaybackState ("ERROR")
}
