package com.example.notifications

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.accessibility.PhonePilotAccessibilityService
import com.example.accessibility.UiNavigator
import com.example.device.AppLauncher
import kotlinx.coroutines.delay

class AutoReplyEngine(private val context: Context) {

    private val appLauncher = AppLauncher(context)
    private val uiNavigator = UiNavigator(context)

    suspend fun sendReplyWithFallback(sbn: StatusBarNotification?, appLabel: String, replyMessage: String): Boolean {
        if (sbn != null) {
            val directSuccess = sendDirectReply(sbn, replyMessage)
            if (directSuccess) return true
        }

        // Fallback: Open notification or app via Accessibility and perform UI click & type
        return try {
            Log.d(TAG, "Direct reply failed or unavailable. Attempting accessibility fallback reply for $appLabel")
            if (sbn?.notification?.contentIntent != null) {
                sbn.notification.contentIntent.send()
            } else {
                appLauncher.launchAppByName(appLabel)
            }
            delay(1500)

            // Look for chat box or search field and type reply
            val typed = uiNavigator.typeText(replyMessage)
            if (typed) {
                delay(800)
                uiNavigator.clickSendButton()
                Log.d(TAG, "Accessibility fallback reply completed for $appLabel")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback auto-reply error", e)
            false
        }
    }

    fun sendDirectReply(sbn: StatusBarNotification, replyMessage: String): Boolean {
        val notification = sbn.notification ?: return false
        val actions = notification.actions ?: return false

        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.allowFreeFormInput) {
                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putCharSequence(remoteInput.resultKey, replyMessage)
                    RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)

                    return try {
                        action.actionIntent.send(context, 0, intent)
                        Log.d(TAG, "Sent direct notification auto-reply: '$replyMessage' to ${sbn.packageName}")
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send direct auto-reply", e)
                        false
                    }
                }
            }
        }
        return false
    }

    companion object {
        private const val TAG = "AutoReplyEngine"
    }
}

