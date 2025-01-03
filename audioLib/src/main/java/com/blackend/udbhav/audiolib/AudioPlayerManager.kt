package com.blackend.udbhav.audiolib

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import com.blackend.udbhav.audiolib.converters.PlayerStateConverter
import com.blackend.udbhav.audiolib.listeners.PlaybackEventListener
import com.blackend.udbhav.audiolib.models.PlayerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(UnstableApi::class)
class AudioPlayerManager (private val context: Context) {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaLibrarySession? = null

    private val playerScope = CoroutineScope(Dispatchers.Main)
    private val stateUpdateScope = CoroutineScope(Dispatchers.Main + Job())

    // Playback State
    private val _playbackState = MutableStateFlow(PlaybackState.Idle.state)
    val playbackState: StateFlow<String> get() = _playbackState

    // Playback Progress
    private val _playbackProgress = MutableStateFlow(0L)
    val playbackProgress: StateFlow<Long> get() = _playbackProgress
    private var progressUpdateJob: Job? = null


    fun setupPlayer(
        playerConfig: PlayerConfig = PlayerConfig(),
        eventListener: PlaybackEventListener = object: PlaybackEventListener {}
    ) {
        playerScope.launch {

            // Initialize Audio Attributes
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(playerConfig.audioCapabilities.audioType.type)
                .setUsage(C.USAGE_MEDIA)
                .build()

            // Initialize ExoPlayer
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(audioAttributes, playerConfig.shouldHandleAudioFocus)
                .setHandleAudioBecomingNoisy(playerConfig.shouldHandleAudioBecomingNoisy)
                .build()

            // Listen for player state changes
            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    eventListener.onPlaybackStateChanged(PlayerStateConverter.convert(state).state)
                    Utils.updateState(
                        stateUpdateScope,
                        _playbackState,
                        { PlayerStateConverter.convert(state).state }
                    )
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {

                    eventListener.onIsPlayingChanged(isPlaying)
                    Utils.updateState(stateUpdateScope, _playbackState, {
                        if (isPlaying) {
                            progressUpdateJob = Utils.updateState(
                                stateUpdateScope,
                                _playbackProgress,
                                { player?.currentPosition ?: 0 },
                                true,
                                playerConfig.progressUpdateInterval
                            )
                            PlaybackState.Playing.state
                        }
                        else {
                            progressUpdateJob?.cancel()
                            if (_playbackState.value == PlaybackState.Playing.state) {
                                PlaybackState.Paused.state
                            } else _playbackState.value
                        }
                    })
                }

                override fun onPlayerError(error: PlaybackException) {
                    Utils.updateState(stateUpdateScope, _playbackState,
                        { PlaybackState.Error.state }
                    )
                    Utils.updateState(stateUpdateScope, _playbackProgress, { 0L })

                    eventListener.onPlaybackError(
                        error.errorCode,
                        error.message ?: "Unknown Error",
                        error.stackTraceToString()
                    )
                    super.onPlayerError(error)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    eventListener.onMediaItemTransition(
                        mediaItem?.mediaId ?: String(),
                        reason
                    )
                    super.onMediaItemTransition(mediaItem, reason)
                }
            })
        }
    }

    fun initSession() {
        playerScope.launch {
            player?.let {
                mediaSession = MediaLibrarySession.Builder(context, it,
                    object : MediaLibrarySession.Callback {})
                    .setId(UUID.randomUUID().toString())
                    .build()
            } ?: throw IllegalStateException("Player not initialized")
        }
    }

    internal fun getSession(): MediaLibrarySession? {
        return mediaSession
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    fun getCurrentMediaItemIndex(): Int {
        return player?.currentMediaItemIndex ?: -1
    }

    fun getCurrentPlaybackPosition(): Long {
        return player?.currentPosition ?: 0
    }

    fun getQueueSize(): Int {
        return player?.mediaItemCount ?: 0
    }

    fun setupQueue(mediaUrls: List<String>?) {
        playerScope.launch {
            mediaUrls?.let {
                val mediaItems = it.map { url ->
                    MediaItem.Builder().setUri(url).build()
                }
                player?.setMediaItems(mediaItems)
                player?.prepare()
            }
        }
    }

    fun addToQueue(mediaUrl: String) {
        playerScope.launch {
            val mediaItem = MediaItem.Builder().setUri(mediaUrl).build()
            player?.addMediaItem(mediaItem)
            player?.prepare()
        }
    }

    fun addToQueue(mediaUrl: String, index: Int) {
        playerScope.launch {
            val mediaItem = MediaItem.Builder().setUri(mediaUrl).build()
            player?.addMediaItem(index, mediaItem)
            player?.prepare()
        }
    }

    fun removeFromQueue(index: Int) {
        playerScope.launch {
            player?.removeMediaItem(index)
        }
    }

    fun play() {
        playerScope.launch {
            player?.playWhenReady = true
            player?.play()
        }
    }

    fun pause() {
        playerScope.launch {
            player?.pause()
        }
    }

    fun stop() {
        playerScope.launch {
            Utils.updateState(stateUpdateScope, _playbackProgress, { 0L })
            player?.stop()
        }
    }

    fun skipToNext() {
        playerScope.launch {
            if (player?.hasNextMediaItem() == true) player?.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        playerScope.launch {
            if (player?.hasPreviousMediaItem() == true) player?.seekToPreviousMediaItem()
            else player?.seekTo(0L)
        }
    }

    fun release() {
        playerScope.launch {
            stop()
            mediaSession?.release()
            player?.release()
        }
    }
}