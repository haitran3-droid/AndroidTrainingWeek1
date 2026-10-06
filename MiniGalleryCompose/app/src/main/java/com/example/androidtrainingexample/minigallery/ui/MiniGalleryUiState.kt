package com.example.androidtrainingexample.minigallery.ui

import com.example.androidtrainingexample.minigallery.model.MediaImage
import com.example.androidtrainingexample.minigallery.model.PermissionState
import com.example.androidtrainingexample.minigallery.model.SortOrder

/**
 * State duy nhất cho màn hình MiniGallery tuân thủ mô hình UDF (Unidirectional Data Flow).
 */
data class MiniGalleryUiState(
    val allImages: List<MediaImage> = emptyList(),
    val displayedImages: List<MediaImage> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val isLoading: Boolean = false,
    val permissionState: PermissionState = PermissionState.Denied,
    val isAddingPhotos: Boolean = false,
    val userMessage: String? = null
) {
    /**
     * Tổng số ảnh đang có quyền truy cập.
     */
    val totalCount: Int get() = allImages.size

    /**
     * Số ảnh hiện đang hiển thị sau khi lọc tìm kiếm.
     */
    val displayedCount: Int get() = displayedImages.size
}
