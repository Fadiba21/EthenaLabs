package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CryptoDarkBorder
import com.example.ui.theme.CryptoDarkCard
import com.example.ui.theme.CryptoDarkCardElevated
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun QuickSignalDialog(
    onDismiss: () -> Unit,
    onSubmitSignal: (String) -> Unit
) {
    val btcSample = """
        BTC LONG

        Entry:
        62000

        TP:
        63000
        64000

        SL:
        61000

        Leverage:
        20x
    """.trimIndent()

    val ethSample = """
        ETH SHORT

        Entry:
        3450

        TP:
        3350
        3280

        SL:
        3520

        Leverage:
        25x
    """.trimIndent()

    val solSample = """
        SOL/USDT LONG
        Entry Zone: 148.0
        Targets: 155.0, 162.0
        Stoploss: 143.5
        Lev: 15x
    """.trimIndent()

    var textInput by remember { mutableStateOf(btcSample) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("quick_signal_dialog"),
        containerColor = CryptoDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telegram Signal Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(label = "BTC LONG", onClick = { textInput = btcSample }, modifier = Modifier.weight(1f))
                    PresetChip(label = "ETH SHORT", onClick = { textInput = ethSample }, modifier = Modifier.weight(1f))
                    PresetChip(label = "SOL LONG", onClick = { textInput = solSample }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("signal_text_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CryptoDarkCard,
                        unfocusedContainerColor = CryptoDarkCard,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CryptoDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    placeholder = {
                        Text("Paste crypto signal here...", color = TextTertiary)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Signal will be parsed -> validated with Gemini Flash AI -> automatically executed on Bybit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSubmitSignal(textInput)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CryptoDarkSurface),
                modifier = Modifier.testTag("submit_signal_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Process Signal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CryptoDarkCardElevated)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
