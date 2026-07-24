package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UiCapturedEvent(
    val eventType: String,
    val packageName: String,
    val className: String,
    val text: String,
    val contentDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PhonePilotAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isConnected.value = true
        Log.d(TAG, "PhonePilot Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { evt ->
            val pkgName = evt.packageName?.toString() ?: ""
            if (pkgName.isNotEmpty()) {
                _currentPackageName.value = pkgName
            }

            val typeName = AccessibilityEvent.eventTypeToString(evt.eventType)
            val eventText = evt.text.joinToString(", ")
            val contentDesc = evt.contentDescription?.toString() ?: ""
            val clsName = evt.className?.toString() ?: ""

            val captured = UiCapturedEvent(
                eventType = typeName,
                packageName = pkgName,
                className = clsName,
                text = eventText,
                contentDescription = contentDesc
            )
            _lastUiEvent.value = captured

            if (evt.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                evt.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                _lastScreenHierarchy.value = dumpScreenHierarchy()
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "PhonePilot Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isConnected.value = false
        }
    }

    fun findNodesByText(text: String): List<AccessibilityNodeInfo> {
        val root = rootInActiveWindow ?: return emptyList()
        return root.findAccessibilityNodeInfosByText(text) ?: emptyList()
    }

    fun findNodeByViewId(viewId: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        return nodes?.firstOrNull()
    }

    fun clickNodeByText(text: String): Boolean {
        val nodes = findNodesByText(text)
        for (node in nodes) {
            if (performClickOnNodeOrParent(node)) {
                return true
            }
        }
        return false
    }

    fun performClickOnNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    fun performLongClickOnNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        while (current != null) {
            if (current.isLongClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            }
            current = current.parent
        }
        return false
    }

    fun scrollNode(forward: Boolean = true): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return findAndPerformScroll(root, action)
    }

    private fun findAndPerformScroll(node: AccessibilityNodeInfo, action: Int): Boolean {
        if (node.isScrollable) {
            if (node.performAction(action)) return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && findAndPerformScroll(child, action)) {
                return true
            }
        }
        return false
    }

    fun inputTextToFocusedField(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }

    fun dumpScreenHierarchy(): String {
        val root = rootInActiveWindow ?: return "Screen content empty or unavailable."
        val sb = StringBuilder()
        sb.append("Current App: ").append(currentPackageName.value).append("\n")
        traverseNode(root, sb, 0)
        return sb.toString()
    }

    private fun traverseNode(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int) {
        val indent = "  ".repeat(depth)
        val text = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val viewId = node.viewIdResourceName ?: ""
        val isClickable = node.isClickable
        val isEditable = node.isEditable

        if (text.isNotBlank() || contentDesc.isNotBlank() || isClickable || isEditable) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            sb.append(indent)
                .append("[")
                .append(node.className?.toString()?.substringAfterLast(".") ?: "View")
                .append("]")
            if (viewId.isNotBlank()) sb.append(" id=").append(viewId)
            if (text.isNotBlank()) sb.append(" text=\"").append(text).append("\"")
            if (contentDesc.isNotBlank()) sb.append(" desc=\"").append(contentDesc).append("\"")
            if (isClickable) sb.append(" (clickable)")
            if (isEditable) sb.append(" (editable)")
            sb.append(" bounds=").append(bounds.toShortString())
            sb.append("\n")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNode(child, sb, depth + 1)
            }
        }
    }

    fun performTapAt(x: Float, y: Float, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    fun performQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    fun performPowerDialog(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
    }

    companion object {
        private const val TAG = "PhonePilotAccessibility"
        private var instance: PhonePilotAccessibilityService? = null

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _currentPackageName = MutableStateFlow("")
        val currentPackageName: StateFlow<String> = _currentPackageName.asStateFlow()

        private val _lastUiEvent = MutableStateFlow<UiCapturedEvent?>(null)
        val lastUiEvent: StateFlow<UiCapturedEvent?> = _lastUiEvent.asStateFlow()

        private val _lastScreenHierarchy = MutableStateFlow("")
        val lastScreenHierarchy: StateFlow<String> = _lastScreenHierarchy.asStateFlow()

        fun getInstance(): PhonePilotAccessibilityService? = instance
    }
}

