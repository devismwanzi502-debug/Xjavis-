package com.example.device

import android.content.Context
import android.graphics.Bitmap
import com.example.accessibility.PhonePilotAccessibilityService

class ScreenshotManager(private val context: Context) {

    fun captureAccessibilityHierarchyText(): String {
        val service = PhonePilotAccessibilityService.getInstance()
        return service?.dumpScreenHierarchy() ?: "Screen content unavailable. Accessibility Service is disabled."
    }

    fun isScreenReadable(): Boolean {
        return PhonePilotAccessibilityService.getInstance() != null
    }
}
