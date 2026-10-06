package com.example.notificationpractice

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Chỉ lưu hai cờ đã đọc để quan sát kết quả action sau khi quay lại app. */
class ReadStateStore(context: Context) {
    private val preferences = context.getSharedPreferences("message_read_state", Context.MODE_PRIVATE)
    fun isRead(id: String): Boolean = preferences.getBoolean(id, false)
    fun markRead(id: String) = preferences.edit { putBoolean(id, true) }
    fun reset() = preferences.edit { clear() }
    fun register(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        preferences.registerOnSharedPreferenceChangeListener(listener)
    fun unregister(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
}
