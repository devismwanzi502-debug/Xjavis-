package com.example.accessibility

import android.content.Context
import kotlinx.coroutines.delay

class UiNavigator(private val context: Context) {

    private val service: PhonePilotAccessibilityService?
        get() = PhonePilotAccessibilityService.getInstance()

    val isAccessibilityEnabled: Boolean
        get() = service != null

    suspend fun clickText(targetText: String, maxRetries: Int = 3): Boolean {
        val s = service ?: return false
        for (attempt in 0 until maxRetries) {
            var success = s.clickNodeByText(targetText) || s.clickNodeByDescription(targetText)
            if (success) return true

            // Try fuzzy case-insensitive match from hierarchy
            val hierarchy = s.dumpScreenHierarchy()
            val matchLine = hierarchy.lines().firstOrNull { it.contains(targetText, ignoreCase = true) }
            if (matchLine != null) {
                val boundsStr = matchLine.substringAfter("bounds=", "").trim()
                if (boundsStr.isNotEmpty()) {
                    val coords = parseBoundsCenter(boundsStr)
                    if (coords != null) {
                        val tapped = GestureExecutor(context).tap(coords.first, coords.second)
                        if (tapped) return true
                    }
                }
            }

            // Scroll down on 2nd attempt if target is off-screen
            if (attempt == 1) {
                GestureExecutor(context).swipeUp()
            }
            delay(600)
        }
        return false
    }

    suspend fun clickSearchButton(maxRetries: Int = 3): Boolean {
        val s = service ?: return false
        for (attempt in 0 until maxRetries) {
            if (s.clickSearchIconOrButton()) return true
            delay(500)
        }
        return false
    }

    suspend fun clickSendButton(): Boolean {
        val s = service ?: return false
        val sendKeywords = listOf("Send", "Submit", "Search", "Enter", "send_button")
        for (kw in sendKeywords) {
            if (s.clickNodeByText(kw) || s.clickNodeByDescription(kw)) return true
        }
        return false
    }

    suspend fun typeText(text: String): Boolean {
        val s = service ?: return false
        return s.inputTextToFocusedField(text)
    }

    suspend fun waitForWindowAndClick(targetText: String, timeoutMs: Long = 5000): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (clickText(targetText)) return true
            delay(500)
        }
        return false
    }

    fun getScreenDump(): String {
        return service?.dumpScreenHierarchy() ?: "Accessibility Service Not Connected"
    }

    private fun parseBoundsCenter(boundsStr: String): Pair<Float, Float>? {
        return try {
            // Bounds format: Rect(left, top - right, bottom)
            val cleaned = boundsStr.replace("Rect(", "").replace(")", "")
            val parts = cleaned.split("-")
            val leftTop = parts[0].trim().split(",")
            val rightBottom = parts[1].trim().split(",")

            val left = leftTop[0].trim().toFloat()
            val top = leftTop[1].trim().toFloat()
            val right = rightBottom[0].trim().toFloat()
            val bottom = rightBottom[1].trim().toFloat()

            Pair((left + right) / 2f, (top + bottom) / 2f)
        } catch (e: Exception) {
            null
        }
    }
}
