package com.example.accessibility

import android.content.Context
import kotlinx.coroutines.delay

class UiNavigator(private val context: Context) {

    private val service: PhonePilotAccessibilityService?
        get() = PhonePilotAccessibilityService.getInstance()

    val isAccessibilityEnabled: Boolean
        get() = service != null

    suspend fun clickText(targetText: String): Boolean {
        val s = service ?: return false
        val success = s.clickNodeByText(targetText)
        if (!success) {
            // Try fuzzy case-insensitive match from hierarchy
            val hierarchy = s.dumpScreenHierarchy()
            val matchLine = hierarchy.lines().firstOrNull { it.contains(targetText, ignoreCase = true) }
            if (matchLine != null) {
                // Parse bounds if present
                val boundsStr = matchLine.substringAfter("bounds=", "").trim()
                if (boundsStr.isNotEmpty()) {
                    val coords = parseBoundsCenter(boundsStr)
                    if (coords != null) {
                        return GestureExecutor(context).tap(coords.first, coords.second)
                    }
                }
            }
        }
        return success
    }

    suspend fun clickSearchButton(): Boolean {
        val s = service ?: return false
        val searchKeywords = listOf("Search", "Find", "Magnifier", "search_button", "btn_search")
        for (kw in searchKeywords) {
            if (s.clickNodeByText(kw)) return true
        }
        val nodes = s.findNodesByText("Search")
        if (nodes.isNotEmpty()) {
            return s.clickNodeByText("Search")
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
