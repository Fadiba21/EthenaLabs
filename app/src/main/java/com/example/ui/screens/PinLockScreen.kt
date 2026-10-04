package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkCard
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PinLockScreen(
    onUnlock: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CryptoDarkCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AI Crypto Signal Trader",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextPrimary
            )

            Text(
                text = "Enter your security PIN to unlock",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = enteredPin,
                onValueChange = {
                    enteredPin = it
                    errorText = null
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .testTag("pin_unlock_input"),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CryptoDarkCard,
                    unfocusedContainerColor = CryptoDarkCard,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CryptoDarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                placeholder = {
                    Text("• • • •", color = TextSecondary, modifier = Modifier.fillMaxWidth())
                }
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText ?: "",
                    color = CryptoRed,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val success = onUnlock(enteredPin)
                    if (!success) {
                        errorText = "Incorrect PIN. Please try again."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("unlock_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CryptoDarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Unlock Trader", fontWeight = FontWeight.Bold)
            }
        }
    }
}
