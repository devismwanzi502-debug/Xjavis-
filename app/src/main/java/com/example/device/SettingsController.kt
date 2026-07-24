package com.example.device

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings

class SettingsController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun setVolume(percent: Int): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = ((percent.coerceIn(0, 100) / 100f) * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setDoNotDisturb(enable: Boolean): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    val filter = if (enable) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL
                    notificationManager.setInterruptionFilter(filter)
                    true
                } else {
                    openNotificationPolicyAccessSettings()
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openNotificationPolicyAccessSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openSettingsScreen(action: String) {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun openDisplaySettings() {
        openSettingsScreen(Settings.ACTION_DISPLAY_SETTINGS)
    }

    fun setDarkMode(enable: Boolean): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
                uiModeManager?.setApplicationNightMode(
                    if (enable) android.app.UiModeManager.MODE_NIGHT_YES else android.app.UiModeManager.MODE_NIGHT_NO
                )
            }
            openDisplaySettings()
            true
        } catch (e: Exception) {
            openDisplaySettings()
            true
        }
    }
}
