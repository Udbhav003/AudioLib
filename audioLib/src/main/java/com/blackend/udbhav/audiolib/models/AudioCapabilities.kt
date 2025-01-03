package com.blackend.udbhav.audiolib.models

data class AudioCapabilities(
    val audioType: AudioType = AudioType.Music
) {
    companion object {
        @JvmStatic
        fun default(): AudioCapabilities {
            return AudioCapabilities(
                audioType = AudioType.Music
            )
        }
    }
}
