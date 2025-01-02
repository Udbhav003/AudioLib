package com.blackend.udbhav.audiolib

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(UnstableApi::class)
class AudioPlayerManager (private val context: Context) {

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaLibrarySession? = null
    private val playerScope = CoroutineScope(Dispatchers.Main)
    private val stateUpdateJob = CoroutineScope(Dispatchers.Main + Job())

    // State flows for playback and position tracking
    private val _playbackState = MutableStateFlow(PlaybackState.Idle.state)
    val playbackState: StateFlow<String> get() = _playbackState

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> get() = _playbackPosition

    init {
        // Setup ExoPlayer
        setupPlayer()

        // Init Media Session
        initializeMediaSession()
    }

    private fun initializeMediaSession() {
        playerScope.launch {
            mediaSession = MediaLibrarySession.Builder(context, player,
                object : MediaLibrarySession.Callback {})
                .setId(UUID.randomUUID().toString())
                .build()
        }
    }

    internal fun getSession(): MediaLibrarySession? {
        return mediaSession
    }

    private fun setupPlayer() {
        playerScope.launch {

            // Initialize Audio Attributes
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            // Initialize ExoPlayer
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(audioAttributes,true)
                .setHandleAudioBecomingNoisy(true)
                .build()

            // Listen for player state changes
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_IDLE -> updateState(_playbackState,{ PlaybackState.Idle.state })
                        Player.STATE_BUFFERING -> updateState(_playbackState,{ PlaybackState.Buffering.state })
                        Player.STATE_READY -> updateState(_playbackState,{ PlaybackState.Ready.state })
                        Player.STATE_ENDED -> updateState(_playbackState,{ PlaybackState.Ended.state })
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateState(_playbackState, {
                        if (isPlaying) { PlaybackState.Playing.state }
                        else { PlaybackState.Paused.state }
                    })
                }
            })

            updateState(_playbackPosition, { player.currentPosition }, true)
        }
    }

    private fun <T> updateState(
        state: MutableStateFlow<T>,
        getValue: () -> T,
        isContinuous: Boolean = false) {

        stateUpdateJob.launch {
            if (isContinuous) {
                while (isActive) {
                    state.update { getValue() }
                    delay(100)
                }
            } else state.update { getValue() }
        }
    }

    fun setupQueue(mediaUrls: List<String>?) {
        playerScope.launch {
            mediaUrls?.let {
                val mediaItems = it.map { url ->
                    MediaItem.Builder().setUri(url).build()
                }
                player.setMediaItems(mediaItems)
                player.prepare()
            }
        }
    }

    fun addToQueue(mediaUrl: String) {
        playerScope.launch {
            val mediaItem = MediaItem.Builder().setUri(mediaUrl).build()
            player.addMediaItem(mediaItem)
            player.prepare()
        }
    }

    fun addToQueue(mediaUrl: String, index: Int) {
        playerScope.launch {
            val mediaItem = MediaItem.Builder().setUri(mediaUrl).build()
            player.addMediaItem(index, mediaItem)
            player.prepare()
        }
    }

    fun removeFromQueue(index: Int) {
        playerScope.launch {
            player.removeMediaItem(index)
        }
    }

    fun play() {
        playerScope.launch {
            player.playWhenReady = true
            player.play()
        }
    }

    fun pause() {
        playerScope.launch {
            player.pause()
        }
    }

    fun stop() {
        playerScope.launch {
            player.stop()
        }
    }

    fun release() {
        playerScope.launch {
            mediaSession?.release()
            player.release()
        }
    }
}