package com.blackend.udbhav.audioplayer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import com.blackend.udbhav.audiolib.AudioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stateTextView: TextView
    private lateinit var positionTextView: TextView
    private val playbackScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        stateTextView = findViewById(R.id.stateTextView)
        positionTextView = findViewById(R.id.positionTextView)
        // Initialize AudioPlayerManager
        audioPlayerManager = AudioPlayerManager(this)
        // Set up a test queue
        val mediaUrls = listOf(
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        )
        audioPlayerManager.setupQueue(mediaUrls)
        // Set listeners for play/pause buttons
        playButton.setOnClickListener {
            audioPlayerManager.play()
        }
        pauseButton.setOnClickListener {
            audioPlayerManager.pause()
        }
        // Observe playback state and position
        observePlaybackState()
        observePlaybackPosition()
    }

    private fun observePlaybackState() {
        playbackScope.launch {
            audioPlayerManager.playbackState.collectLatest { state ->
                stateTextView.text = "Playback State: $state"
            }
        }
    }
    private fun observePlaybackPosition() {
        playbackScope.launch {
            audioPlayerManager.playbackPosition.collectLatest { position ->
                positionTextView.text = "Playback Position: ${position / 1000}s"
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.release()
        playbackScope.cancel()
    }
}