package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlowCyanBorder
import com.example.ui.theme.RgbCyan
import com.example.ui.theme.RgbLime
import com.example.ui.theme.RgbMagenta

@Composable
fun VoiceAssistantScreen(
    isListening: Boolean,
    isSpeaking: Boolean,
    recognizedText: String,
    onToggleListen: () -> Unit,
    onSpeakTest: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Voice Assistant HUD",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isListening) "Listening..." else if (isSpeaking) "Speaking..." else "Tap mic to activate",
                fontSize = 14.sp,
                color = if (isListening) RgbCyan else RgbLime
            )
        }

        // Pulse Orb Center
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (isListening) RgbCyan.copy(alpha = 0.3f)
                        else RgbMagenta.copy(alpha = 0.15f)
                    )
            )

            IconButton(
                onClick = onToggleListen,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) RgbCyan
                        else DarkSurfaceVariant
                    )
                    .border(2.dp, if (isListening) GlowCyanBorder else DarkBorder, CircleShape)
                    .testTag("voice_hud_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Microphone Toggle",
                    tint = if (isListening) Color.Black else RgbCyan,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Live Transcript Box
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, GlowCyanBorder),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE SPEECH TRANSCRIPT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RgbCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recognizedText.ifBlank { "Say something like \"Open YouTube and search for Davis\" or \"Gaming Mode\"..." },
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = if (recognizedText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onSpeakTest("PhonePilot voice system active and operational.") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = RgbLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test TTS", color = Color.White)
            }
        }
    }
}
