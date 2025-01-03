package com.blackend.udbhav.audiolib.models

data class PlayerConfig(
    val audioCapabilities: AudioCapabilities = AudioCapabilities.default(),
    val progressUpdateInterval: Long = 1000L,
    val shouldHandleAudioFocus: Boolean = true,
    val shouldHandleAudioBecomingNoisy: Boolean = true
)
