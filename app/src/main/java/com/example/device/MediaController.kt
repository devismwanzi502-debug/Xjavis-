package com.example.device

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

class MediaController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playPause() {
        sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun next() {
        sendKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun previous() {
        sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    private fun sendKeyEvent(keyCode: Int) {
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }
}
