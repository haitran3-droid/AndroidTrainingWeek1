package com.example.androidtrainingexample.minigallery.model

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

data class MediaImage(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val dateAddedSeconds: Long
) {
    /**
     * Định dạng dung lượng sang dạng người dùng dễ đọc (B, KB, MB, GB).
     */
    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            val digitGroups = (ln(sizeBytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.size - 1)
            return String.format(Locale.getDefault(), "%.1f %s", sizeBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
        }

    /**
     * Định dạng ngày thêm sang dạng ngày giờ đọc được.
     */
    val formattedDate: String
        get() {
            if (dateAddedSeconds <= 0) return "Không xác định"
            val date = Date(dateAddedSeconds * 1000L)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(date)
        }
}
