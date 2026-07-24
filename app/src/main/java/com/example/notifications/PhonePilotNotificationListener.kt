package com.example.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.automation.AutomationEngine
import com.example.data.PhonePilotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InterceptedNotification(
    val id: Long = System.currentTimeMillis(),
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sbn: StatusBarNotification? = null
)

class PhonePilotNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isConnected.value = true
        Log.d(TAG, "Notification Listener Connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val pkg = it.packageName ?: ""
            if (pkg == packageName) return // Ignore own notifications

            val extras = it.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            if (title.isBlank() && text.isBlank()) return

            val appLabel = getAppLabel(pkg)
            val item = InterceptedNotification(
                packageName = pkg,
                appLabel = appLabel,
                title = title,
                text = text,
                sbn = it
            )

            val current = _recentNotifications.value.toMutableList()
            current.add(0, item)
            if (current.size > 50) current.removeAt(current.size - 1)
            _recentNotifications.value = current

            scope.launch {
                val repository = PhonePilotRepository(applicationContext)
                repository.log(
                    title = "Notification from $appLabel",
                    detail = "$title: $text",
                    type = "NOTIFICATION"
                )

                AutomationEngine(applicationContext).onNotificationReceived(appLabel, title, text)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
            _isConnected.value = false
        }
    }

    private fun getAppLabel(pkg: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            pkg.substringAfterLast(".")
        }
    }

    companion object {
        private const val TAG = "NotificationListener"
        private var instance: PhonePilotNotificationListener? = null

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _recentNotifications = MutableStateFlow<List<InterceptedNotification>>(emptyList())
        val recentNotifications: StateFlow<List<InterceptedNotification>> = _recentNotifications.asStateFlow()

        fun getInstance(): PhonePilotNotificationListener? = instance
    }
}
