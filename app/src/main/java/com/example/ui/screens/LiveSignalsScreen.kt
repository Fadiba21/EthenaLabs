package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SignalHistory
import com.example.ui.components.QuickSignalDialog
import com.example.ui.theme.CryptoAmber
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkBorder
import com.example.ui.theme.CryptoDarkCard
import com.example.ui.theme.CryptoDarkCardElevated
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenBg
import com.example.ui.theme.CryptoPurple
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoRedBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.TraderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveSignalsScreen(
    viewModel: TraderViewModel,
    modifier: Modifier = Modifier
) {
    val signals by viewModel.signals.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    if (showSimulatorDialog) {
        QuickSignalDialog(
            onDismiss = { showSimulatorDialog = false },
            onSubmitSignal = { viewModel.processIncomingSignal(it) }
        )
    }

    val filteredSignals = when (selectedFilter) {
        "APPROVED" -> signals.filter { it.status == "APPROVED" || it.status == "EXECUTED" }
        "EXECUTED" -> signals.filter { it.status == "EXECUTED" }
        "REJECTED" -> signals.filter { it.status == "REJECTED_BY_AI" || it.status == "LOW_CONFIDENCE" }
        else -> signals
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CryptoDarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSimulatorDialog = true },
                containerColor = NeonCyan,
                contentColor = CryptoDarkBackground,
                modifier = Modifier.testTag("add_signal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add / Parse Signal")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE SIGNALS FEED",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time Telegram parsing & Gemini Flash AI evaluation",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signal_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "APPROVED", "EXECUTED", "REJECTED").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonCyan else CryptoDarkCard)
                            .border(1.dp, if (isSelected) NeonCyan else CryptoDarkBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CryptoDarkBackground else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredSignals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No signals yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to parse or test a Telegram signal",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredSignals, key = { it.id }) { signal ->
                        SignalCard(
                            signal = signal,
                            onExecute = { viewModel.executeSignalNow(signal) },
                            onIgnore = { viewModel.ignoreSignal(signal.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SignalCard(
    signal: SignalHistory,
    onExecute: () -> Unit,
    onIgnore: () -> Unit
) {
    val isLong = signal.side.equals("LONG", ignoreCase = true)
    val sideColor = if (isLong) CryptoGreen else CryptoRed
    val sideBg = if (isLong) CryptoGreenBg else CryptoRedBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("signal_card_${signal.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
        border = BorderStroke(1.dp, CryptoDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Coin Symbol, Direction Badge, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sideBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${signal.side} ${if (isLong) "🟢" else "🔴"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = sideColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${signal.leverage}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                StatusBadge(status = signal.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing Table: Entry, SL, TP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoDarkCardElevated)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Entry", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = "$${signal.entry}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Stop Loss", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = "$${signal.sl}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = CryptoRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Take Profit", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = "$${signal.tp.split(",").firstOrNull() ?: signal.tp}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = CryptoGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Analysis Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F1522))
                    .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini Flash AI Analyzer",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Confidence: ${signal.aiConfidence}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (signal.aiConfidence >= 75) CryptoGreen else if (signal.aiConfidence >= 50) CryptoAmber else CryptoRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (signal.aiConfidence / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (signal.aiConfidence >= 75) CryptoGreen else if (signal.aiConfidence >= 50) CryptoAmber else CryptoRed,
                    trackColor = CryptoDarkBorder
                )

                if (signal.aiReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reason: ${signal.aiReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: [EXECUTE NOW], [IGNORE]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (signal.status != "EXECUTED") {
                    Button(
                        onClick = onExecute,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("execute_signal_btn_${signal.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CryptoDarkBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXECUTE NOW", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onIgnore,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ignore_signal_btn_${signal.id}"),
                        border = BorderStroke(1.dp, CryptoDarkBorder),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("IGNORE")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CryptoGreenBg)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXECUTED ON BYBIT FUTURES", color = CryptoGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "APPROVED" -> CryptoGreen to "AI APPROVED"
        "EXECUTED" -> CryptoGreen to "EXECUTED"
        "REJECTED_BY_AI" -> CryptoRed to "REJECTED"
        "LOW_CONFIDENCE" -> CryptoAmber to "LOW CONF"
        "IGNORED" -> TextTertiary to "IGNORED"
        else -> NeonCyan to "PENDING"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
