package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.BybitOrderResult
import com.example.data.api.BybitWalletBalance
import com.example.data.local.entity.BotSettings
import com.example.data.local.entity.SignalHistory
import com.example.data.local.entity.TradeHistory
import com.example.data.repository.TraderRepository
import com.example.service.SignalTraderForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TraderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TraderRepository(application)

    val signals: StateFlow<List<SignalHistory>> = repository.allSignals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trades: StateFlow<List<TradeHistory>> = repository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openTrades: StateFlow<List<TradeHistory>> = repository.openTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<BotSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _walletBalance = MutableStateFlow(BybitWalletBalance())
    val walletBalance: StateFlow<BybitWalletBalance> = _walletBalance.asStateFlow()

    private val _livePrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val livePrices: StateFlow<Map<String, Double>> = _livePrices.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(SignalTraderForegroundService.isRunning)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        // Ensure default settings exist and start service
        viewModelScope.launch {
            val s = repository.getSettings()
            if (s.pinCode.isNotBlank()) {
                _isAppLocked.value = true
            }
            refreshBalance()
            // Auto start service if autoTrade enabled
            if (s.autoTrade && !s.emergencyStop) {
                startService()
            }
        }

        // Live ticker loop
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val symbols = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT")
                    val prices = mutableMapOf<String, Double>()
                    for (sym in symbols) {
                        prices[sym] = repository.fetchLivePrice(sym)
                    }
                    _livePrices.value = prices
                    delay(8000)
                } catch (e: Exception) {
                    delay(10000)
                }
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun startService() {
        SignalTraderForegroundService.startService(getApplication())
        _isServiceRunning.value = true
    }

    fun stopService() {
        SignalTraderForegroundService.stopService(getApplication())
        _isServiceRunning.value = false
    }

    fun refreshBalance() {
        viewModelScope.launch {
            try {
                val bal = repository.fetchWalletBalance()
                _walletBalance.value = bal
            } catch (e: Exception) {
                // Keep existing
            }
        }
    }

    fun processIncomingSignal(rawText: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val signal = repository.processIncomingTelegramMessage(rawText)
                if (signal != null) {
                    _toastMessage.value = "Parsed ${signal.symbol} ${signal.side} • AI: ${signal.aiConfidence}%"
                    refreshBalance()
                } else {
                    _toastMessage.value = "Could not parse signal. Please check format."
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun executeSignalNow(signal: SignalHistory) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result: BybitOrderResult = repository.executeTradeForSignal(signal)
                _toastMessage.value = result.message
                refreshBalance()
            } catch (e: Exception) {
                _toastMessage.value = "Execution failed: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun ignoreSignal(signalId: Long) {
        viewModelScope.launch {
            val database = com.example.data.local.CryptoDatabase.getInstance(getApplication())
            database.signalDao().updateStatus(signalId, "IGNORED")
            _toastMessage.value = "Signal ignored."
        }
    }

    fun closeTrade(trade: TradeHistory) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = repository.closeActiveTrade(trade)
                _toastMessage.value = result.message
                refreshBalance()
            } catch (e: Exception) {
                _toastMessage.value = "Close failed: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun toggleEmergencyStop() {
        viewModelScope.launch {
            val s = settings.value ?: repository.getSettings()
            if (s.emergencyStop) {
                repository.resumeTrading()
                startService()
                _toastMessage.value = "Trading Resumed ✅"
            } else {
                repository.emergencyHalt()
                stopService()
                _toastMessage.value = "EMERGENCY STOP ACTIVATED 🚨 All Auto Trading Halted"
            }
        }
    }

    fun updateTradingSettings(
        autoTrade: Boolean,
        marginMode: String,
        riskPercent: Float,
        leverage: Int,
        minimumConfidence: Int,
        manualApproval: Boolean,
        trailingStop: Boolean,
        autoBreakeven: Boolean
    ) {
        viewModelScope.launch {
            val current = settings.value ?: repository.getSettings()
            val updated = current.copy(
                autoTrade = autoTrade,
                marginMode = marginMode,
                riskPercent = riskPercent,
                leverage = leverage,
                minimumConfidence = minimumConfidence,
                manualApproval = manualApproval,
                trailingStop = trailingStop,
                autoBreakeven = autoBreakeven
            )
            repository.saveSettings(updated)
            _toastMessage.value = "Settings Saved Successfully"
        }
    }

    fun saveTelegramConfig(apiId: String, apiHash: String, phone: String, channel: String) {
        viewModelScope.launch {
            repository.saveEncryptedTelegram(apiId, apiHash, phone, channel)
            _toastMessage.value = "Telegram Config Encrypted & Saved 🔒"
        }
    }

    fun saveBybitConfig(apiKey: String, secretKey: String, isTestnet: Boolean) {
        viewModelScope.launch {
            repository.saveEncryptedBybitKeys(apiKey, secretKey, isTestnet)
            _toastMessage.value = "Bybit Keys Encrypted & Saved in Keystore 🔒"
            refreshBalance()
        }
    }

    fun savePinCode(pin: String) {
        viewModelScope.launch {
            val current = settings.value ?: repository.getSettings()
            val encryptedPin = com.example.data.security.KeystoreHelper.encrypt(pin)
            repository.saveSettings(current.copy(pinCode = encryptedPin))
            _toastMessage.value = if (pin.isEmpty()) "PIN Removed" else "App PIN Set & Encrypted 🔒"
        }
    }

    fun unlock(pin: String): Boolean {
        val current = settings.value ?: return true
        if (current.pinCode.isBlank()) {
            _isAppLocked.value = false
            return true
        }
        val decryptedPin = com.example.data.security.KeystoreHelper.decrypt(current.pinCode)
        return if (decryptedPin == pin) {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataJson()
            onResult(json)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _toastMessage.value = "All Signal and Trade History cleared."
        }
    }
}
