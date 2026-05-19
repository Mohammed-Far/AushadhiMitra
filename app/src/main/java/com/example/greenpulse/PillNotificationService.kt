package com.example.greenpulse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PillNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save new token to Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseRepository().saveFCMToken(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "💊 Pill Reminder"
        val body = message.notification?.body ?: "Time to take your medicine!"
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "pill_reminders"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager

        // Create notification channel
        val channel = NotificationChannel(
            channelId,
            "Pill Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Medicine reminder notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
        notificationManager.createNotificationChannel(channel)

        // Create intent to open app when notification tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}