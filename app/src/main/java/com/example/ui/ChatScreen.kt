package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.StepExecutionState
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlowCyanBorder
import com.example.ui.theme.RgbCyan
import com.example.ui.theme.RgbLime
import com.example.ui.theme.RgbMagenta
import com.example.ui.theme.StatusGreen

@Composable
fun ChatScreen(
    isExecuting: Boolean,
    currentPlan: List<StepExecutionState>,
    lastMessage: String?,
    onSendCommand: (String) -> Unit,
    onVoiceClick: () -> Unit
) {
    var inputQuery by remember { mutableStateOf("") }

    val quickPrompts = listOf(
        "Open YouTube and search for Davis",
        "Gaming Mode",
        "Set volume to 80%",
        "Turn Do Not Disturb ON",
        "Go Home"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Status Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, GlowCyanBorder),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RgbCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Status",
                        tint = RgbCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PhonePilot AI Agent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (isExecuting) "Executing Command..." else "Ready for instructions",
                        fontSize = 12.sp,
                        color = if (isExecuting) RgbCyan else RgbLime
                    )
                }
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = RgbCyan,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Suggestion Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { onSendCommand(prompt) },
                    label = { Text(prompt, fontSize = 12.sp, color = RgbCyan) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = DarkSurfaceVariant
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = DarkBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Plan Execution Progress
        AnimatedVisibility(visible = currentPlan.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, RgbMagenta.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Execution Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = RgbMagenta
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    currentPlan.forEach { step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.Pending,
                                contentDescription = null,
                                tint = if (step.isCompleted) RgbLime else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.description,
                                fontSize = 13.sp,
                                color = if (step.isCompleted) RgbLime else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Response Banner
        if (!lastMessage.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RgbCyan.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, RgbCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = lastMessage,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = RgbCyan
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Input Field Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask PhonePilot...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RgbCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, RgbMagenta, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = RgbMagenta
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        onSendCommand(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(RgbCyan)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}
