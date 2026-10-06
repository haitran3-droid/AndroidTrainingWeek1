package com.example.notificationpractice

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private lateinit var notifications: MessageNotifications
    private lateinit var readState: ReadStateStore
    private var uiState by mutableStateOf(MainUiState())
    private val readStateListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        refreshStatus()
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        refreshStatus()
        if (!granted) toast(R.string.permission_denied)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notifications = MessageNotifications(this)
        notifications.createChannel()
        readState = ReadStateStore(this)
        refreshStatus()

        setContent {
            // Compose lưu lựa chọn này qua Activity recreation bằng SavedStateRegistry.
            var brokenMode by rememberSaveable { mutableStateOf(false) }
            PracticeTheme {
                MessageListScreen(
                    state = uiState,
                    brokenMode = brokenMode,
                    onBrokenModeChange = { enabled ->
                        notifications.cancelAll()
                        brokenMode = enabled
                    },
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= 33) requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else toast(R.string.permission_needed)
                    },
                    onOpenSettings = {
                        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
                    },
                    onPostMessage = { message -> post(message, brokenMode) },
                    onReset = {
                        notifications.cancelAll()
                        readState.reset()
                        refreshStatus()
                        toast(R.string.reset_done)
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bảng notification có thể không làm Activity pause/resume.
        readState.register(readStateListener)
        refreshStatus()
    }

    override fun onStop() {
        readState.unregister(readStateListener)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun post(message: DemoMessage, brokenMode: Boolean) {
        if (notifications.show(message, brokenMode)) {
            Toast.makeText(this, getString(R.string.posted, message.id), Toast.LENGTH_SHORT).show()
        } else toast(R.string.permission_needed)
        refreshStatus()
    }

    private fun refreshStatus() {
        uiState = MainUiState(
            notificationsEnabled = notifications.canPost(),
            canRequestPermission = Build.VERSION.SDK_INT >= 33,
            isARead = readState.isRead(DemoMessages.a.id),
            isBRead = readState.isRead(DemoMessages.b.id),
        )
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}