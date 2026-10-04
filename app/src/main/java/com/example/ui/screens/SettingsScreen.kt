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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CryptoAmber
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkBorder
import com.example.ui.theme.CryptoDarkCard
import com.example.ui.theme.CryptoDarkCardElevated
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.TraderViewModel

@Composable
fun SettingsScreen(
    viewModel: TraderViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Telegram State
    var tgApiId by remember { mutableStateOf("") }
    var tgApiHash by remember { mutableStateOf("") }
    var tgPhone by remember { mutableStateOf("") }
    var tgChannel by remember { mutableStateOf("") }
    var showTgHash by remember { mutableStateOf(false) }

    // Bybit State
    var bybitKey by remember { mutableStateOf("") }
    var bybitSecret by remember { mutableStateOf("") }
    var isTestnet by remember { mutableStateOf(true) }
    var showBybitSecret by remember { mutableStateOf(false) }

    // Trading Rules State
    var autoTrade by remember { mutableStateOf(true) }
    var marginMode by remember { mutableStateOf("Cross") }
    var riskPercent by remember { mutableFloatStateOf(20f) }
    var leverage by remember { mutableIntStateOf(20) }
    var minConfidence by remember { mutableIntStateOf(75) }
    var manualApproval by remember { mutableStateOf(false) }
    var trailingStop by remember { mutableStateOf(false) }
    var autoBreakeven by remember { mutableStateOf(true) }

    // Security PIN State
    var pinCode by remember { mutableStateOf("") }

    LaunchedEffect(settings) {
        settings?.let { s ->
            tgApiId = s.telegramApiId
            tgPhone = s.telegramPhone
            tgChannel = s.telegramChannel
            bybitKey = s.bybitApiKey
            isTestnet = s.bybitTestnet
            autoTrade = s.autoTrade
            marginMode = s.marginMode
            riskPercent = s.riskPercent
            leverage = s.leverage
            minConfidence = s.minimumConfidence
            manualApproval = s.manualApproval
            trailingStop = s.trailingStop
            autoBreakeven = s.autoBreakeven
        }
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
            Text(
                text = "TRADING & API CONFIG",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Protected with Android Keystore hardware-backed encryption",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Trading Automation Rules Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_trading_rules_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
                border = BorderStroke(1.dp, CryptoDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Trading Parameters", icon = Icons.Default.Tune)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto Trade Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto Trade Futures", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Execute Bybit positions automatically", color = TextTertiary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoTrade,
                            onCheckedChange = { autoTrade = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = NeonCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Margin Mode (Cross vs Isolated)
                    Text("Margin Mode", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Cross", "Isolated").forEach { mode ->
                            val isSelected = marginMode.equals(mode, true)
                            Button(
                                onClick = { marginMode = mode },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) NeonCyan else CryptoDarkCardElevated,
                                    contentColor = if (isSelected) CryptoDarkBackground else TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(mode, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Risk Percent Slider (1% - 100%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Position Size (Risk % of Balance)", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text("${riskPercent.toInt()}%", color = CryptoAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = riskPercent,
                        onValueChange = { riskPercent = it },
                        valueRange = 1f..100f,
                        steps = 99,
                        colors = SliderDefaults.colors(thumbColor = CryptoAmber, activeTrackColor = CryptoAmber)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Leverage Slider (1x - 125x)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Default Leverage", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text("${leverage}x", color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = leverage.toFloat(),
                        onValueChange = { leverage = it.toInt() },
                        valueRange = 1f..125f,
                        steps = 124,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Minimum AI Confidence Slider (50% - 100%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Minimum Gemini AI Confidence", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text("${minConfidence}%", color = CryptoGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = minConfidence.toFloat(),
                        onValueChange = { minConfidence = it.toInt() },
                        valueRange = 50f..100f,
                        steps = 50,
                        colors = SliderDefaults.colors(thumbColor = CryptoGreen, activeTrackColor = CryptoGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggles for Auto Breakeven & Manual Approval
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto Breakeven on TP1", color = TextPrimary)
                        Switch(
                            checked = autoBreakeven,
                            onCheckedChange = { autoBreakeven = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = NeonCyan)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Manual Approval Mode", color = TextPrimary)
                        Switch(
                            checked = manualApproval,
                            onCheckedChange = { manualApproval = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = NeonCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateTradingSettings(
                                autoTrade = autoTrade,
                                marginMode = marginMode,
                                riskPercent = riskPercent,
                                leverage = leverage,
                                minimumConfidence = minConfidence,
                                manualApproval = manualApproval,
                                trailingStop = trailingStop,
                                autoBreakeven = autoBreakeven
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_trading_rules_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CryptoDarkBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Trading Rules", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bybit V5 Configuration Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_bybit_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
                border = BorderStroke(1.dp, CryptoDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Bybit V5 Futures API", icon = Icons.AutoMirrored.Filled.ShowChart)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Environment: ${if (isTestnet) "Testnet" else "Mainnet"}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = !isTestnet,
                            onCheckedChange = { isTestnet = !it },
                            colors = SwitchDefaults.colors(checkedTrackColor = CryptoGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsInputField(
                        label = "Bybit API Key",
                        value = bybitKey,
                        onValueChange = { bybitKey = it },
                        placeholder = "e.g. 5xM2G0... (From Bybit API Management)"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsInputField(
                        label = "Bybit API Secret",
                        value = bybitSecret,
                        onValueChange = { bybitSecret = it },
                        placeholder = "Enter Secret Key",
                        isPassword = true,
                        showPassword = showBybitSecret,
                        onTogglePassword = { showBybitSecret = !showBybitSecret }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.saveBybitConfig(bybitKey, bybitSecret, isTestnet)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_bybit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CryptoDarkBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Encrypt & Save Bybit Keys", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Telegram Signal Reader Configuration Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_telegram_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
                border = BorderStroke(1.dp, CryptoDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Telegram Signal Reader", icon = Icons.AutoMirrored.Filled.Send)

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsInputField(
                        label = "Telegram API ID",
                        value = tgApiId,
                        onValueChange = { tgApiId = it },
                        placeholder = "e.g. 2489102"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsInputField(
                        label = "Telegram API Hash",
                        value = tgApiHash,
                        onValueChange = { tgApiHash = it },
                        placeholder = "e.g. 9b78a...",
                        isPassword = true,
                        showPassword = showTgHash,
                        onTogglePassword = { showTgHash = !showTgHash }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsInputField(
                        label = "Target Signal Channel",
                        value = tgChannel,
                        onValueChange = { tgChannel = it },
                        placeholder = "@CryptoVipSignals"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.saveTelegramConfig(tgApiId, tgApiHash, tgPhone, tgChannel)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_telegram_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CryptoDarkBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Telegram Reader Config", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Security & App Lock Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_security_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkCard),
                border = BorderStroke(1.dp, CryptoDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "App Security & Keystore", icon = Icons.Default.Security)

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsInputField(
                        label = "App PIN Code (Optional 4-6 digits)",
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        placeholder = "Leave empty to disable PIN lock",
                        isPassword = true,
                        keyboardType = KeyboardType.NumberPassword
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.savePinCode(pinCode)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_pin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoDarkCardElevated, contentColor = NeonCyan),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update App Security PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CryptoDarkCardElevated,
                unfocusedContainerColor = CryptoDarkCardElevated,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CryptoDarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            placeholder = { Text(placeholder, color = TextTertiary, fontSize = 12.sp) },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = TextSecondary
                        )
                    }
                }
            } else null
        )
    }
}
