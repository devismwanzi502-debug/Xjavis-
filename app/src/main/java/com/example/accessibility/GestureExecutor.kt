package com.example.accessibility

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GestureExecutor(private val context: Context) {

    private val service: PhonePilotAccessibilityService?
        get() = PhonePilotAccessibilityService.getInstance()

    val isAvailable: Boolean
        get() = service != null

    suspend fun tap(x: Float, y: Float): Boolean = suspendCoroutine { continuation ->
        val s = service
        if (s == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }
        s.performTapAt(x, y) { success ->
            continuation.resume(success)
        }
    }

    suspend fun swipeUp(): Boolean {
        val (width, height) = getScreenDimensions()
        return swipe(width / 2f, height * 0.8f, width / 2f, height * 0.2f, 400)
    }

    suspend fun swipeDown(): Boolean {
        val (width, height) = getScreenDimensions()
        return swipe(width / 2f, height * 0.2f, width / 2f, height * 0.8f, 400)
    }

    suspend fun swipeLeft(): Boolean {
        val (width, height) = getScreenDimensions()
        return swipe(width * 0.9f, height / 2f, width * 0.1f, height / 2f, 300)
    }

    suspend fun swipeRight(): Boolean {
        val (width, height) = getScreenDimensions()
        return swipe(width * 0.1f, height / 2f, width * 0.9f, height / 2f, 300)
    }

    suspend fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300): Boolean = suspendCoroutine { continuation ->
        val s = service
        if (s == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }
        s.performSwipe(startX, startY, endX, endY, durationMs) { success ->
            continuation.resume(success)
        }
    }

    fun performGlobalBack(): Boolean {
        return service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK) ?: false
    }

    fun performGlobalHome(): Boolean {
        return service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME) ?: false
    }

    fun performGlobalRecents(): Boolean {
        return service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS) ?: false
    }

    fun performGlobalNotifications(): Boolean {
        return service?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) ?: false
    }

    private fun getScreenDimensions(): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }
}
