package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TradeHistory
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkBorder
import com.example.ui.theme.CryptoDarkCard
import com.example.ui.theme.CryptoDarkCardElevated
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenBg
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
fun JournalScreen(
    viewModel: TraderViewModel,
    modifier: Modifier = Modifier
) {
    val trades by viewModel.trades.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val closedTrades = trades.filter { it.status == "CLOSED" }
    val totalTrades = closedTrades.size
    val winningTrades = closedTrades.filter { (it.profit ?: 0.0) > (it.loss ?: 0.0) }
    val losingTrades = closedTrades.filter { (it.loss ?: 0.0) > (it.profit ?: 0.0) }

    val totalProfitAmount = closedTrades.sumOf { it.profit ?: 0.0 }
    val totalLossAmount = closedTrades.sumOf { it.loss ?: 0.0 }
    val netPnl = totalProfitAmount - totalLossAmount

    val winRate = if (totalTrades > 0) {
        (winningTrades.size.toDouble() / totalTrades.toDouble()) * 100.0
    } else 0.0

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?", color = TextPrimary) },
            text = { Text("This will delete all signal history and closed trade records from local database.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CryptoDarkCard
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header with Backup export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRADING JOURNAL",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Verified performance metrics & local history",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.exportBackup { json ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Trading Database"))
                            }
                        },
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp)
                    }
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_summary_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
                border = BorderStroke(1.dp, CryptoDarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NET REALIZED PNL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${if (netPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", netPnl)} USDT",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = if (netPnl >= 0) CryptoGreen else CryptoRed
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Win rate bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Win Rate: ${String.format(Locale.US, "%.1f", winRate)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${winningTrades.size}W - ${losingTrades.size}L",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { if (totalTrades > 0) (winRate / 100f).toFloat() else 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CryptoGreen,
                        trackColor = CryptoRed
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Profits", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                "+$${String.format(Locale.US, "%,.2f", totalProfitAmount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = CryptoGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Total Losses", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                "-$${String.format(Locale.US, "%,.2f", totalLossAmount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = CryptoRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Trades", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                "$totalTrades",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLOSED TRADES HISTORY",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                if (closedTrades.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text("Clear", color = CryptoRed, fontSize = 12.sp)
                    }
                }
            }
        }

        if (closedTrades.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No closed trades recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        } else {
            items(closedTrades, key = { it.id }) { trade ->
                ClosedTradeItem(trade = trade)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ClosedTradeItem(trade: TradeHistory) {
    val isProfit = (trade.profit ?: 0.0) >= (trade.loss ?: 0.0)
    val amount = if (isProfit) trade.profit ?: 0.0 else -(trade.loss ?: 0.0)
    val amountColor = if (isProfit) CryptoGreen else CryptoRed
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trade.closeTimestamp ?: trade.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_trade_item_${trade.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
        border = BorderStroke(1.dp, CryptoDarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trade.symbol,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trade.side,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (trade.side.equals("LONG", true)) CryptoGreen else CryptoRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${trade.leverage}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${if (amount >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    ),
                    color = amountColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Entry: $${trade.entryPrice}  →  Exit: $${trade.exitPrice ?: "-"}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = TextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
