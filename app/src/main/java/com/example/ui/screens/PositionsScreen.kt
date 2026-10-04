package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.Locale

@Composable
fun PositionsScreen(
    viewModel: TraderViewModel,
    modifier: Modifier = Modifier
) {
    val openTrades by viewModel.openTrades.collectAsStateWithLifecycle()
    val livePrices by viewModel.livePrices.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ACTIVE POSITIONS",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = TextPrimary
        )
        Text(
            text = "Live perpetual contracts on Bybit (${openTrades.size} Active)",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (openTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CandlestickChart,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No open positions",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Positions opened by signals will appear here realtime",
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
                items(openTrades, key = { it.id }) { trade ->
                    val currentPrice = livePrices[trade.symbol] ?: trade.entryPrice
                    val priceDiff = if (trade.side.equals("LONG", true)) {
                        currentPrice - trade.entryPrice
                    } else {
                        trade.entryPrice - currentPrice
                    }
                    val pnlPercent = if (trade.entryPrice > 0) {
                        (priceDiff / trade.entryPrice) * 100.0 * trade.leverage
                    } else 0.0
                    val profitUsdt = (trade.amountUsdt / trade.leverage) * (pnlPercent / 100.0)

                    PositionCard(
                        trade = trade,
                        currentPrice = currentPrice,
                        pnlPercent = pnlPercent,
                        profitUsdt = profitUsdt,
                        onClose = { viewModel.closeTrade(trade) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun PositionCard(
    trade: TradeHistory,
    currentPrice: Double,
    pnlPercent: Double,
    profitUsdt: Double,
    onClose: () -> Unit
) {
    val isLong = trade.side.equals("LONG", true)
    val sideColor = if (isLong) CryptoGreen else CryptoRed
    val sideBg = if (isLong) CryptoGreenBg else CryptoRedBg
    val isPnlPositive = pnlPercent >= 0
    val pnlColor = if (isPnlPositive) CryptoGreen else CryptoRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("position_card_${trade.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
        border = BorderStroke(1.dp, CryptoDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Symbol, Direction, Leverage, Margin Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trade.symbol,
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
                            text = "${trade.side} ${if (isLong) "🟢" else "🔴"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = sideColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CryptoDarkCardElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${trade.leverage}x ${trade.marginMode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time PnL Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(pnlColor.copy(alpha = 0.1f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Unrealized PnL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPnlPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = pnlColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isPnlPositive) "+" else ""}$${String.format(Locale.US, "%.2f", profitUsdt)} USDT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = pnlColor
                        )
                    }
                }

                Text(
                    text = "${if (isPnlPositive) "+" else ""}${String.format(Locale.US, "%.2f", pnlPercent)}%",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    ),
                    color = pnlColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing Details: Entry vs Mark Price vs Position Size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoDarkCardElevated)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Entry Price", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", trade.entryPrice)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Current Price", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", currentPrice)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Position Size", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = "$${String.format(Locale.US, "%,.1f", trade.amountUsdt)} USDT",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Close Position Button
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("close_position_btn_${trade.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = CryptoRed, contentColor = TextPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CLOSE POSITION (MARKET)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
