package com.example.notificationpractice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class MarkReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MessageNotifications.ACTION_MARK_READ) return
        val message =
            DemoMessages.find(intent.getStringExtra(MessageNotifications.EXTRA_MESSAGE_ID))
                ?: return
        ReadStateStore(context).markRead(message.id)
        NotificationManagerCompat.from(context).cancel(message.notificationId)
        Log.d(
            MessageNotifications.TAG,
            "MARK_READ message_id=${message.id}, canceled=${message.notificationId}"
        )
        // Không mở Activity ở receiver; action này chỉ đánh dấu đã đọc và hủy thông báo.
    }
}