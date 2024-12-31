package com.blackend.udbhav.audiolib.react

import android.content.Context
import android.content.Intent
import com.blackend.udbhav.audiolib.AudioLibraryService
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.turbomodule.core.interfaces.TurboModule

class RNAudioPlayerModule (reactContext: ReactApplicationContext) : TurboModule {

    private val context = reactContext
    private val audioServiceIntent = Intent(context, AudioLibraryService::class.java)

    fun setupQueue(context: Context, mediaUrls: List<String>) {
        audioServiceIntent.action = "ACTION_SETUP_QUEUE"
        audioServiceIntent.putExtra("mediaUrls", ArrayList(mediaUrls))
        context.startService(audioServiceIntent)
    }

    fun play() {
        audioServiceIntent.action = "ACTION_PLAY"
        context.startService(audioServiceIntent)
    }

    fun pause() {
        audioServiceIntent.action = "ACTION_PAUSE"
        context.startService(audioServiceIntent)
    }

    fun stop() {
        audioServiceIntent.action = "ACTION_STOP"
        context.startService(audioServiceIntent)
    }

    override fun initialize() {
        context.startService(audioServiceIntent)
    }

    override fun invalidate() {
        context.stopService(audioServiceIntent)
    }
}