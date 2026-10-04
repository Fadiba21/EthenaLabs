package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.LiveSignalsScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.PositionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AICryptoSignalTraderTheme
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkBorder
import com.example.ui.theme.CryptoDarkCardElevated
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.TraderViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TraderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AICryptoSignalTraderTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: TraderViewModel) {
    val context = LocalContext.current
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val openTrades by viewModel.openTrades.collectAsStateWithLifecycle()
    val signals by viewModel.signals.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Request Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* handled */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    if (isAppLocked) {
        PinLockScreen(
            onUnlock = { pin -> viewModel.unlock(pin) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CryptoDarkBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = CryptoDarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Screen.Dashboard,
                        Screen.LiveSignals,
                        Screen.Positions,
                        Screen.Journal,
                        Screen.Settings
                    )

                    navItems.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                if (screen == Screen.Positions && openTrades.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = NeonCyan, contentColor = CryptoDarkBackground) {
                                                Text("${openTrades.size}", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else if (screen == Screen.LiveSignals && signals.any { it.status == "NEW" || it.status == "APPROVED" }) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = CryptoGreen, contentColor = CryptoDarkBackground) {
                                                Text("!", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CryptoDarkBackground,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSignals = { currentScreen = Screen.LiveSignals },
                        onNavigateToPositions = { currentScreen = Screen.Positions }
                    )
                    Screen.LiveSignals -> LiveSignalsScreen(viewModel = viewModel)
                    Screen.Positions -> PositionsScreen(viewModel = viewModel)
                    Screen.Journal -> JournalScreen(viewModel = viewModel)
                    Screen.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
