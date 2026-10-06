package com.example.androidtrainingexample.minigallery.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.androidtrainingexample.minigallery.model.PermissionState

/**
 * Tiện ích kiểm tra và trả về danh sách quyền đọc ảnh tùy theo phiên bản Android.
 *
 * Chiến lược:
 * - API 34+ (Android 14+): Hỗ trợ Partial Access với READ_MEDIA_VISUAL_USER_SELECTED.
 * - API 33 (Android 13): Sử dụng READ_MEDIA_IMAGES.
 * - API 29-32 (Android 10 - 12): Sử dụng READ_EXTERNAL_STORAGE.
 */
object PermissionHelper {

    /**
     * Lấy danh sách các quyền cần xin runtime từ người dùng.
     */
    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Kiểm tra trạng thái quyền hiện tại của ứng dụng.
     */
    fun checkPermissionState(context: Context, currentKnownImageCount: Int = 0): PermissionState {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val hasFull = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                val hasPartial = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED

                when {
                    hasFull -> PermissionState.GrantedFull
                    hasPartial -> PermissionState.GrantedPartial(currentKnownImageCount)
                    else -> PermissionState.Denied
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val hasImages = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                if (hasImages) PermissionState.GrantedFull else PermissionState.Denied
            }

            else -> {
                val hasStorage = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (hasStorage) PermissionState.GrantedFull else PermissionState.Denied
            }
        }
    }
}
