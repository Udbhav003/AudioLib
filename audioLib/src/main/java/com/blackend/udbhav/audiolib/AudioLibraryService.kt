package com.blackend.udbhav.audiolib

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class AudioLibraryService : MediaLibraryService() {

    private lateinit var audioPlayerManager: AudioPlayerManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        audioPlayerManager = AudioPlayerManager(this)
        setListener(MediaSessionServiceListener(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent != null) {
                when(intent.action) {
                    "ACTION_SETUP_QUEUE" -> {
                        val mediaUrls = intent.getStringArrayListExtra("mediaUrls")
                        audioPlayerManager.setupQueue(mediaUrls)
                    }
                    "ACTION_PLAY" -> {
                        audioPlayerManager.play()
                    }
                    "ACTION_PAUSE" -> {
                        audioPlayerManager.pause()
                    }
                    "ACTION_STOP" -> {
                        audioPlayerManager.stop()
                    }
                }
            }
            return super.onStartCommand(intent, flags, startId)
        } catch (e: Exception) {
            e.printStackTrace()
            return START_STICKY
        }
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.release()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return audioPlayerManager.getSession()
    }
}