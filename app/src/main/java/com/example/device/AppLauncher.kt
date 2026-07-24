package com.example.device

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

class AppLauncher(private val context: Context) {

    fun launchApp(packageName: String): Boolean {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    fun launchAppByName(appName: String): Boolean {
        val lowerName = appName.lowercase().trim()

        // Known package mapping for common apps
        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "spotify" to "com.spotify.music",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "call of duty" to "com.activision.callofduty.shooter",
            "cod" to "com.activision.callofduty.shooter",
            "settings" to "com.android.settings",
            "camera" to "com.android.camera2",
            "gallery" to "com.google.android.apps.photos",
            "photos" to "com.google.android.apps.photos",
            "gmail" to "com.google.android.gm"
        )

        for ((key, pkg) in knownPackages) {
            if (lowerName.contains(key)) {
                if (launchApp(pkg)) return true
            }
        }

        // Query installed apps
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            if (label.contains(lowerName)) {
                val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }
        return false
    }

    fun openWebsite(url: String): Boolean {
        var formattedUrl = url
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
