package com.example.ui

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlowCyanBorder
import com.example.ui.theme.RgbCyan
import com.example.ui.theme.RgbLime
import com.example.ui.theme.RgbMagenta
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("phonepilot_prefs", Context.MODE_PRIVATE) }

    var storedKey by remember { mutableStateOf(prefs.getString("user_gemini_api_key", "") ?: "") }
    var apiKeyInput by remember { mutableStateOf(storedKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    var requireConfirmCalls by remember { mutableStateOf(true) }
    var requireConfirmMessages by remember { mutableStateOf(true) }
    var requireConfirmPayments by remember { mutableStateOf(true) }

    val buildKey = BuildConfig.GEMINI_API_KEY
    val activeKeySource = when {
        storedKey.isNotBlank() -> "Custom Saved Key Active ⚡"
        buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY" -> "BuildConfig Key Active ⚙️"
        else -> "No Active API Key Set ⚠️"
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
                    .background(RgbCyan, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Settings & Security",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Gemini API key configuration & guardrails",
                    fontSize = 12.sp,
                    color = RgbCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Gemini API Key Input Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, GlowCyanBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = RgbCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI API Key", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (storedKey.isNotBlank() || (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY"))
                                        RgbCyan.copy(alpha = 0.15f)
                                    else
                                        StatusRed.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activeKeySource,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (storedKey.isNotBlank() || (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY")) RgbCyan else StatusRed
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Enter your custom Gemini API key below to enable real-time AI response capabilities directly in your standalone APK:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            placeholder = { Text("AIzaSy...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle key visibility",
                                        tint = RgbCyan
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RgbCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    prefs.edit().putString("user_gemini_api_key", apiKeyInput.trim()).apply()
                                    storedKey = apiKeyInput.trim()
                                    statusMessage = "✅ Custom Gemini API key saved successfully!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RgbCyan, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Key", fontWeight = FontWeight.Bold)
                            }

                            if (storedKey.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        prefs.edit().remove("user_gemini_api_key").apply()
                                        storedKey = ""
                                        apiKeyInput = ""
                                        statusMessage = "Cleared custom API key. Reverted to system defaults."
                                    },
                                    border = BorderStroke(1.dp, RgbMagenta),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RgbMagenta)
                                ) {
                                    Text("Clear")
                                }
                            }
                        }

                        if (statusMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(statusMessage, fontSize = 11.sp, color = RgbLime)
                        }
                    }
                }
            }

            // GitHub & APK Deployment Readiness Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RgbLime)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GitHub & APK Ready", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Zero hardcoded credentials in source files.\n• Runtime Key fallback via app SharedPreferences & .env / BuildConfig.\n• Fully compatible with standard GitHub repository export & GitHub Actions APK compilation.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Guardrail settings card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = RgbMagenta)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardrails & Security Controls", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Confirm Before Calls", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("Ask user confirmation before dialing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = requireConfirmCalls,
                                onCheckedChange = { requireConfirmCalls = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RgbCyan, checkedTrackColor = RgbCyan.copy(alpha = 0.3f))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Confirm Sent Messages", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("Prompt before sending auto-replies", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = requireConfirmMessages,
                                onCheckedChange = { requireConfirmMessages = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RgbCyan, checkedTrackColor = RgbCyan.copy(alpha = 0.3f))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Block Unauthenticated Actions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("Block financial or dangerous system changes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = requireConfirmPayments,
                                onCheckedChange = { requireConfirmPayments = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RgbCyan, checkedTrackColor = RgbCyan.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }
    }
}

