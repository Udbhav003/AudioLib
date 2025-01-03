package com.blackend.udbhav.audioplayer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blackend.udbhav.audiolib.AudioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var stateTextView: TextView
    private lateinit var positionTextView: TextView

    private val playbackScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        stopButton = findViewById(R.id.stopButton)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.prevButton)

        stateTextView = findViewById(R.id.stateTextView)
        positionTextView = findViewById(R.id.positionTextView)

        // Initialize AudioPlayerManager
        audioPlayerManager = AudioPlayerManager(this)

        // Set up a test queue
        val mediaUrls = listOf(
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        )
        audioPlayerManager.setupPlayer()
        audioPlayerManager.initSession()
        audioPlayerManager.setupQueue(mediaUrls)
        // Set listeners for play/pause buttons
        playButton.setOnClickListener {
            audioPlayerManager.play()
        }
        pauseButton.setOnClickListener {
            audioPlayerManager.pause()
        }
        stopButton.setOnClickListener {
            audioPlayerManager.stop()
        }
        nextButton.setOnClickListener {
            audioPlayerManager.skipToNext()
        }
        previousButton.setOnClickListener {
            audioPlayerManager.skipToPrevious()
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
            audioPlayerManager.playbackProgress.collectLatest { position ->
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