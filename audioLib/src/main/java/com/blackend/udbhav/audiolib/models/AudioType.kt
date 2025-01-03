package com.blackend.udbhav.audiolib.models

sealed class AudioType (val type: Int) {
    data object Others: AudioType(0)
    data object Speech : AudioType(1)
    data object Music : AudioType(2)
    data object Movie : AudioType(3)
    data object SoundEffect : AudioType(4)
}
