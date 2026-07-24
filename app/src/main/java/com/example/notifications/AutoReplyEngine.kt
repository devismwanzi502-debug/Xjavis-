package com.example.notifications

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log

class AutoReplyEngine(private val context: Context) {

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
                        Log.d(TAG, "Sent auto-reply: $replyMessage to ${sbn.packageName}")
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send auto-reply", e)
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
