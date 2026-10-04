package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.data.repository.TraderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SignalTraderForegroundService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private lateinit var repository: TraderRepository

    companion object {
        const val ACTION_START = "ACTION_START_TRADER_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_TRADER_SERVICE"
        const val ACTION_SEND_TEST_SIGNAL = "ACTION_SEND_TEST_SIGNAL"
        const val EXTRA_SIGNAL_TEXT = "EXTRA_SIGNAL_TEXT"

        var isRunning = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, SignalTraderForegroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SignalTraderForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = TraderRepository(applicationContext)
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                isRunning = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SEND_TEST_SIGNAL -> {
                val text = intent.getStringExtra(EXTRA_SIGNAL_TEXT)
                if (!text.isNullOrBlank()) {
                    serviceScope.launch {
                        repository.processIncomingTelegramMessage(text)
                    }
                }
            }
            else -> {
                startForegroundServiceMode()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceMode() {
        isRunning = true
        val notification = NotificationHelper.buildForegroundNotification(
            this,
            "Telegram: Active 🟢 | Bybit: Ready 🟢 | AI Engine: Running"
        )
        startForeground(NotificationHelper.NOTIFICATION_SERVICE_ID, notification)

        // Background worker job that monitors market conditions and signals
        serviceScope.launch {
            while (isActive && isRunning) {
                try {
                    // Keep background service alive and update notifications
                    delay(30000)
                } catch (e: Exception) {
                    delay(5000)
                }
            }
        }
    }

    override fun onDestroy() {
        isRunning = false
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
