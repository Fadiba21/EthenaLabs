package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object LiveSignals : Screen("live_signals", "Signals", Icons.Default.NotificationsActive)
    object Positions : Screen("positions", "Positions", Icons.Default.CandlestickChart)
    object Journal : Screen("journal", "Journal", Icons.Default.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
