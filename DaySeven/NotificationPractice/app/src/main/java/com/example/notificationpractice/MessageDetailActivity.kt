package com.example.notificationpractice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MessageDetailActivity : ComponentActivity() {
    private var messageId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readIntent(intent, "onCreate")
        setContent {
            PracticeTheme {
                MessageDetailScreen(messageId = messageId, onBackToList = {
                    // Hoạt động cả khi task chưa có MainActivity.
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntent(intent, "onNewIntent")
    }

    private fun readIntent(intent: Intent, callback: String) {
        messageId = intent.getStringExtra(MessageNotifications.EXTRA_MESSAGE_ID)
        Log.d(MessageNotifications.TAG, "$callback OPEN message_id=$messageId")
    }
}