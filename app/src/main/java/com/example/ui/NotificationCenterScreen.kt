package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifications.InterceptedNotification
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlowCyanBorder
import com.example.ui.theme.RgbCyan
import com.example.ui.theme.RgbLime
import com.example.ui.theme.RgbMagenta

@Composable
fun NotificationCenterScreen(
    notifications: List<InterceptedNotification>,
    onAutoReply: (InterceptedNotification, String) -> Unit,
    onChatbotReply: (InterceptedNotification) -> Unit
) {
    var activeReplyNotif by remember { mutableStateOf<InterceptedNotification?>(null) }
    var customReplyText by remember { mutableStateOf("") }

    if (activeReplyNotif != null) {
        AlertDialog(
            onDismissRequest = { activeReplyNotif = null },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(
                    "Send Specific Text Reply",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        "To: ${activeReplyNotif?.appLabel} (${activeReplyNotif?.title})",
                        fontSize = 12.sp,
                        color = RgbCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customReplyText,
                        onValueChange = { customReplyText = it },
                        label = { Text("Specific Text Message") },
                        placeholder = { Text("e.g. Thanks! I'll call you back soon.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RgbCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val notif = activeReplyNotif
                        if (notif != null && customReplyText.isNotBlank()) {
                            onAutoReply(notif, customReplyText.trim())
                        }
                        activeReplyNotif = null
                        customReplyText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RgbCyan, contentColor = Color.Black)
                ) {
                    Text("Send Text", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeReplyNotif = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(RgbMagenta, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Notification Center",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose Chatbot AI or Specific Text replies for incoming messages",
                    fontSize = 12.sp,
                    color = RgbMagenta
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = RgbCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No notifications intercepted yet",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Incoming app notifications will appear here with dynamic AI reply toggles.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notif ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, GlowCyanBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.appLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = RgbCyan
                                )
                                Text(
                                    text = "ACTIVE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = RgbLime
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = notif.text,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Choose Reply Mode:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Chatbot AI Reply Button
                                Button(
                                    onClick = { onChatbotReply(notif) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RgbCyan, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Chatbot AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Specific Text Reply Button
                                OutlinedButton(
                                    onClick = {
                                        activeReplyNotif = notif
                                        customReplyText = "Thank you! Received via PhonePilot."
                                    },
                                    border = BorderStroke(1.dp, RgbMagenta),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RgbMagenta),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Specific Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
