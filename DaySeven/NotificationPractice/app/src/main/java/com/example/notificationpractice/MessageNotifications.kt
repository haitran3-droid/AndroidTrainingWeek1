package com.example.notificationpractice

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MessageNotifications(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "practice_messages"
        const val EXTRA_MESSAGE_ID = "message_id"
        const val ACTION_OPEN = "com.example.notificationpractice.OPEN_MESSAGE"
        const val ACTION_MARK_READ = "com.example.notificationpractice.MARK_READ"
        const val TAG = "NotificationPractice"
        const val TOKEN_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    fun createChannel() {
        // minSdk = 29 nên luôn có NotificationChannel (API 26+).
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPost(): Boolean {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED) return false
        val manager = context.getSystemService(NotificationManager::class.java)
        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                manager.getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun contentPendingIntent(message: DemoMessage, brokenMode: Boolean): PendingIntent {
        val intent = Intent(context, MessageDetailActivity::class.java).apply {
            action = ACTION_OPEN
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_MESSAGE_ID, message.id)
        }
        // Extras KHÔNG phân biệt token. Chế độ thử lỗi cố ý dùng requestCode = 0.
        val requestCode = if (brokenMode) 0 else message.notificationId
        return PendingIntent.getActivity(context, requestCode, intent, TOKEN_FLAGS)
    }

    fun readPendingIntent(message: DemoMessage): PendingIntent {
        val intent = Intent(context, MarkReadReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_MESSAGE_ID, message.id)
        }
        // Token cho action luôn riêng, kể cả trong chế độ thử lỗi Content Intent.
        return PendingIntent.getBroadcast(context, message.notificationId, intent, TOKEN_FLAGS)
    }

    fun show(message: DemoMessage, brokenMode: Boolean): Boolean {
        // Kiểm tra ngay trước notify; không giả định quyền lúc mở app còn hiệu lực.
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED) return false
        if (!canPost()) return false

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(context.getString(message.titleRes))
            .setContentText(context.getString(message.bodyRes))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(message.bodyRes)))
            .setContentIntent(contentPendingIntent(message, brokenMode)) // ← Content Intent
            .addAction(R.drawable.ic_message, context.getString(R.string.mark_read), readPendingIntent(message))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(message.notificationId, notification)
        Log.d(TAG, "POST message_id=${message.id}, notificationId=${message.notificationId}, " +
                "contentRequestCode=${if (brokenMode) 0 else message.notificationId}")
        return true
    }

    fun cancelAll() {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(DemoMessages.a.notificationId)
        manager.cancel(DemoMessages.b.notificationId)
    }
}