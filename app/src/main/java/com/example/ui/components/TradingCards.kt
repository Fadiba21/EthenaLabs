package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CryptoAmber
import com.example.ui.theme.CryptoDarkBorder
import com.example.ui.theme.CryptoDarkCard
import com.example.ui.theme.CryptoDarkCardElevated
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenBg
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoRedBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun BalanceOverviewCard(
    totalBalance: Double,
    availableBalance: Double,
    unrealisedPnl: Double,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPnlPositive = unrealisedPnl >= 0
    val pnlColor = if (isPnlPositive) CryptoGreen else CryptoRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balance_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.6f), CryptoDarkBorder, Color.Transparent)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOTAL ASSETS (BYBIT FUTURES)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CryptoDarkCardElevated)
                            .clickable { onSyncClick() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Sync ⟳",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$${String.format(java.util.Locale.US, "%,.2f", totalBalance)} USDT",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Available Margin",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%,.2f", availableBalance)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Unrealised PnL",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPnlPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = pnlColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isPnlPositive) "+" else ""}$${String.format(java.util.Locale.US, "%,.2f", unrealisedPnl)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                                color = pnlColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChipsRow(
    telegramConnected: Boolean,
    geminiConnected: Boolean,
    bybitConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusPill(
            name = "Telegram",
            connected = telegramConnected,
            modifier = Modifier.weight(1f)
        )
        StatusPill(
            name = "Gemini AI",
            connected = geminiConnected,
            modifier = Modifier.weight(1f)
        )
        StatusPill(
            name = "Bybit V5",
            connected = bybitConnected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatusPill(
    name: String,
    connected: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = if (connected) CryptoGreen else CryptoRed
    val bgColor = if (connected) CryptoGreenBg else CryptoRedBg

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CryptoDarkCard)
            .border(1.dp, CryptoDarkBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_stat_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
        border = BorderStroke(1.dp, CryptoDarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}
