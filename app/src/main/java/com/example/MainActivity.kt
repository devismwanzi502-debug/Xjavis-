package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.ActivityLogsScreen
import com.example.ui.AutomationBuilderScreen
import com.example.ui.ChatScreen
import com.example.ui.MainViewModel
import com.example.ui.MemoryViewerScreen
import com.example.ui.NotificationCenterScreen
import com.example.ui.PermissionManagerScreen
import com.example.ui.SettingsScreen
import com.example.ui.VoiceAssistantScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PhonePilotTheme
import com.example.voice.WakeWordService

enum class AppTab(val title: String, val icon: ImageVector) {
    CHAT("Chat", Icons.Default.Chat),
    VOICE("Voice", Icons.Default.Mic),
    AUTOMATION("Automate", Icons.Default.AutoAwesome),
    MEMORY("Memory", Icons.Default.Psychology),
    PERMISSIONS("Permissions", Icons.Default.Security),
    NOTIFICATIONS("Alerts", Icons.Default.Notifications),
    LOGS("Logs", Icons.Default.ListAlt),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            WakeWordService.startService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkMicrophonePermission()

        setContent {
            PhonePilotTheme {
                var selectedTab by remember { mutableStateOf(AppTab.CHAT) }

                val isExecuting by viewModel.isExecuting.collectAsState()
                val currentPlan by viewModel.currentPlan.collectAsState()
                val lastMessage by viewModel.lastMessage.collectAsState()

                val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
                val isNotificationListenerEnabled by viewModel.isNotificationListenerEnabled.collectAsState()

                val rules by viewModel.rules.collectAsState()
                val memories by viewModel.memories.collectAsState()
                val logs by viewModel.logs.collectAsState()
                val notifications by viewModel.recentNotifications.collectAsState()

                val isListening by viewModel.isListening.collectAsState()
                val isSpeaking by viewModel.isSpeaking.collectAsState()
                val recognizedText by viewModel.recognizedText.collectAsState()

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurfaceVariant,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        when (selectedTab) {
                            AppTab.CHAT -> ChatScreen(
                                isExecuting = isExecuting,
                                currentPlan = currentPlan,
                                lastMessage = lastMessage,
                                onSendCommand = { viewModel.sendUserCommand(it) },
                                onVoiceClick = { selectedTab = AppTab.VOICE }
                            )
                            AppTab.VOICE -> VoiceAssistantScreen(
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                recognizedText = recognizedText,
                                onToggleListen = { viewModel.toggleVoiceListening() },
                                onSpeakTest = { viewModel.speakText(it) }
                            )
                            AppTab.AUTOMATION -> AutomationBuilderScreen(
                                rules = rules,
                                onAddRule = { name, tType, tVal, aType, aVal ->
                                    viewModel.addAutomationRule(name, tType, tVal, aType, aVal)
                                },
                                onToggleRule = { viewModel.toggleAutomationRule(it) },
                                onDeleteRule = { viewModel.deleteAutomationRule(it) },
                                onTestTrigger = { tType, tVal ->
                                    viewModel.fireTriggerTest(tType, tVal)
                                }
                            )
                            AppTab.MEMORY -> MemoryViewerScreen(
                                memories = memories,
                                onAddMemory = { key, value -> viewModel.addMemory(key, value) },
                                onDeleteMemory = { viewModel.deleteMemory(it) },
                                onClearAll = { viewModel.clearAllMemory() }
                            )
                            AppTab.PERMISSIONS -> PermissionManagerScreen(
                                isAccessibilityEnabled = isAccessibilityEnabled,
                                isNotificationListenerEnabled = isNotificationListenerEnabled,
                                onRequestMicrophonePermission = { checkMicrophonePermission() }
                            )
                            AppTab.NOTIFICATIONS -> NotificationCenterScreen(
                                notifications = notifications,
                                onAutoReply = { notif, replyText ->
                                    viewModel.sendAutoReply(notif, replyText)
                                },
                                onChatbotReply = { notif ->
                                    viewModel.sendChatbotAutoReply(notif)
                                }
                            )
                            AppTab.LOGS -> ActivityLogsScreen(
                                logs = logs,
                                onClearLogs = { viewModel.clearLogs() }
                            )
                            AppTab.SETTINGS -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            WakeWordService.startService(this)
        }
    }
}
