package com.blackend.udbhav.audiolib.listeners

interface PlaybackEventListener {
    fun onPlaybackStateChanged(state: String) {}
    fun onIsPlayingChanged(isPlaying: Boolean) {}
    fun onMediaItemTransition(mediaId: String, reason: Int) {}
    fun onPlaybackError (code: Int, message: String, trace: String) {}
}