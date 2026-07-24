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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AutomationRule
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlowCyanBorder
import com.example.ui.theme.RgbCyan
import com.example.ui.theme.RgbLime
import com.example.ui.theme.RgbMagenta
import com.example.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationBuilderScreen(
    rules: List<AutomationRule>,
    onAddRule: (name: String, triggerType: String, triggerValue: String, actionType: String, actionValue: String) -> Unit,
    onToggleRule: (AutomationRule) -> Unit,
    onDeleteRule: (AutomationRule) -> Unit,
    onTestTrigger: (String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    var ruleName by remember { mutableStateOf("") }
    var selectedTriggerType by remember { mutableStateOf("NOTIFICATION") }
    var triggerValue by remember { mutableStateOf("") }
    var selectedActionType by remember { mutableStateOf("LAUNCH_APP") }
    var actionValue by remember { mutableStateOf("") }

    var triggerExpanded by remember { mutableStateOf(false) }
    var actionExpanded by remember { mutableStateOf(false) }

    val triggerTypes = listOf("NOTIFICATION", "CHARGER", "VOICE")
    val actionTypes = listOf("AUTO_REPLY_AI", "AUTO_REPLY_TEXT", "LAUNCH_APP", "TOGGLE_DND", "SPEAK", "VOLUME")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = !showDialog },
                containerColor = RgbCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_automation_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Automation")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        text = "Automations Engine",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Configure rules with Chatbot AI or Specific Text responses",
                        fontSize = 12.sp,
                        color = RgbCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Rule Card Form
            if (showDialog) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, GlowCyanBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create New Automation Rule", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ruleName,
                            onValueChange = { ruleName = it },
                            label = { Text("Rule Name") },
                            placeholder = { Text("e.g. Smart Auto-Reply") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RgbCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Trigger Dropdown
                        ExposedDropdownMenuBox(
                            expanded = triggerExpanded,
                            onExpandedChange = { triggerExpanded = !triggerExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTriggerType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Trigger Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = triggerExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RgbCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = triggerExpanded,
                                onDismissRequest = { triggerExpanded = false }
                            ) {
                                triggerTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedTriggerType = type
                                            triggerExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = triggerValue,
                            onValueChange = { triggerValue = it },
                            label = { Text("Trigger Detail") },
                            placeholder = { Text(if (selectedTriggerType == "NOTIFICATION") "e.g. WhatsApp" else "e.g. Hello Pilot") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RgbCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Dropdown
                        ExposedDropdownMenuBox(
                            expanded = actionExpanded,
                            onExpandedChange = { actionExpanded = !actionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedActionType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Action Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RgbCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = actionExpanded,
                                onDismissRequest = { actionExpanded = false }
                            ) {
                                actionTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (type) {
                                                    "AUTO_REPLY_AI" -> "🤖 Chatbot AI Auto-Reply"
                                                    "AUTO_REPLY_TEXT" -> "💬 Specific Text Auto-Reply"
                                                    else -> type
                                                }
                                            )
                                        },
                                        onClick = {
                                            selectedActionType = type
                                            actionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = actionValue,
                            onValueChange = { actionValue = it },
                            label = { Text("Action Value / Guidance") },
                            placeholder = { Text(if (selectedActionType == "AUTO_REPLY_TEXT") "e.g. Thanks! I am busy right now." else "e.g. Be professional and friendly") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RgbCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (ruleName.isNotBlank()) {
                                    onAddRule(ruleName, selectedTriggerType, triggerValue, selectedActionType, actionValue)
                                    showDialog = false
                                    ruleName = ""
                                    triggerValue = ""
                                    actionValue = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RgbCyan, contentColor = Color.Black)
                        ) {
                            Text("Save Rule", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Rules List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rules) { rule ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rule.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "WHEN ${rule.triggerType} (${rule.triggerValue}) -> THEN ${
                                        when(rule.actionType) {
                                            "AUTO_REPLY_AI" -> "🤖 Chatbot AI Auto-Reply"
                                            "AUTO_REPLY_TEXT" -> "💬 Specific Text Reply"
                                            else -> rule.actionType
                                        }
                                    } (${rule.actionValue})",
                                    fontSize = 12.sp,
                                    color = RgbCyan
                                )
                            }

                            IconButton(onClick = { onTestTrigger(rule.triggerType, rule.triggerValue) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test Rule",
                                    tint = RgbLime
                                )
                            }

                            IconButton(onClick = { onDeleteRule(rule) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Rule",
                                    tint = StatusRed
                                )
                            }

                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { onToggleRule(rule) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RgbCyan,
                                    checkedTrackColor = RgbCyan.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
