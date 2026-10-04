package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_SERVICE_ID = "channel_crypto_service"
    const val CHANNEL_SIGNALS_ID = "channel_crypto_signals"
    const val NOTIFICATION_SERVICE_ID = 1001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Trading Engine Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors Telegram signals and executes Bybit futures trades"
            }

            val signalChannel = NotificationChannel(
                CHANNEL_SIGNALS_ID,
                "AI Trading Signals & Orders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts for new signals and order execution results"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(signalChannel)
        }
    }

    fun buildForegroundNotification(context: Context, statusText: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE_ID)
            .setContentTitle("🤖 AI Crypto Signal Trader Active")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun sendSignalAlert(
        context: Context,
        symbol: String,
        side: String,
        confidence: Int,
        status: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val directionEmoji = if (side == "LONG") "🟢" else "🔴"
        val title = "🤖 AI Trader: New Signal $symbol $side $directionEmoji"
        val message = "AI Approval: $confidence% • Status: $status"

        val notification = NotificationCompat.Builder(context, CHANNEL_SIGNALS_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\nAnalyzed with Gemini Flash AI risk engine."))
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    fun sendOrderSuccess(
        context: Context,
        symbol: String,
        side: String,
        orderId: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SIGNALS_ID)
            .setContentTitle("Order Success ✅ $symbol $side")
            .setContentText("Bybit Order ID: $orderId • Position Opened")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
