package com.blackend.udbhav.audiolib.converters

import androidx.media3.common.Player
import com.blackend.udbhav.audiolib.PlaybackState

internal object PlayerStateConverter {

    fun convert(state: Int): PlaybackState {
        return when (state) {
            Player.STATE_IDLE -> PlaybackState.Idle
            Player.STATE_BUFFERING -> PlaybackState.Buffering
            Player.STATE_READY -> PlaybackState.Ready
            Player.STATE_ENDED -> PlaybackState.Ended
            else -> PlaybackState.Idle
        }
    }
}