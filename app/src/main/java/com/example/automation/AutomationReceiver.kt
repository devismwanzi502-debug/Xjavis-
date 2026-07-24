package com.example.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutomationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("AutomationReceiver", "Broadcast received: $action")

        val scope = CoroutineScope(Dispatchers.IO)
        val engine = AutomationEngine(context)

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                scope.launch { engine.onChargerConnected() }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("AutomationReceiver", "Boot completed - PhonePilot background triggers active")
            }
        }
    }
}
