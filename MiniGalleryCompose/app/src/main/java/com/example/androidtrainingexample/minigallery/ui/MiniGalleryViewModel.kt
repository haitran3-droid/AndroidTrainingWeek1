package com.example.androidtrainingexample.minigallery.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.androidtrainingexample.minigallery.data.MediaRepository
import com.example.androidtrainingexample.minigallery.model.MediaImage
import com.example.androidtrainingexample.minigallery.model.PermissionState
import com.example.androidtrainingexample.minigallery.model.SortOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/** Picker sources appear directly in the gallery; this ViewModel never creates copies. */
class MiniGalleryViewModel(private val repository: MediaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MiniGalleryUiState())
    val uiState: StateFlow<MiniGalleryUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null
    private var deviceImages: List<MediaImage> = emptyList()
    private var pickedImages: List<MediaImage> = emptyList()

    private val restorePickedImagesJob = viewModelScope.launch {
        try {
            pickedImages = repository.loadPickedImages()
            publishImages()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            _uiState.update {
                it.copy(userMessage = "Không thể khôi phục ảnh đã chọn: ${error.localizedMessage}")
            }
        }
    }

    fun updatePermissionState(newState: PermissionState) {
        _uiState.update { it.copy(permissionState = newState) }
        when (newState) {
            PermissionState.GrantedFull, is PermissionState.GrantedPartial -> startObservingMedia()
            PermissionState.Denied -> {
                observeJob?.cancel()
                observeJob = null
                deviceImages = emptyList()
                publishImages()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startObservingMedia() {
        val previousJob = observeJob
        previousJob?.cancel()
        observeJob = viewModelScope.launch {
            previousJob?.join()
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.observeImages().collect { images ->
                    deviceImages = images
                    publishImages()
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            permissionState = if (current.permissionState is PermissionState.GrantedPartial) {
                                PermissionState.GrantedPartial(images.size)
                            } else current.permissionState
                        )
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update {
                    it.copy(isLoading = false, userMessage = "Lỗi khi đọc thư viện ảnh: ${error.localizedMessage}")
                }
            }
        }
    }

    /** Combine both sources so a permission refresh does not erase picker selections. */
    private fun publishImages(clearSearch: Boolean = false) {
        val images = (pickedImages + deviceImages).distinctBy { it.uri.toString() }
        _uiState.update { current ->
            val query = if (clearSearch) "" else current.searchQuery
            current.copy(
                allImages = images,
                searchQuery = query,
                displayedImages = applyFilterAndSort(images, query, current.sortOrder)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                displayedImages = applyFilterAndSort(current.allImages, query, current.sortOrder)
            )
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { current ->
            current.copy(
                sortOrder = order,
                displayedImages = applyFilterAndSort(current.allImages, current.searchQuery, order)
            )
        }
    }

    fun onPhotosSelected(uris: List<Uri>) {
        if (uris.isEmpty() || _uiState.value.isAddingPhotos) return
        _uiState.update { it.copy(isAddingPhotos = true) }
        viewModelScope.launch {
            var failedCount = 0
            var lastError: String? = null
            try {
                restorePickedImagesJob.join()
                for (uri in uris.take(10).distinctBy { it.toString() }) {
                    try {
                        val image = repository.getMediaInfo(uri)
                        repository.persistPickedImage(image)
                        pickedImages = (pickedImages + image).distinctBy { it.uri.toString() }
                        publishImages(clearSearch = true)
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        failedCount++
                        lastError = error.localizedMessage
                    }
                }
                _uiState.update {
                    it.copy(userMessage = when {
                        failedCount > 0 -> "Không thể đọc $failedCount ảnh đã chọn: $lastError"
                        uris.size > 10 -> "Đã giới hạn chọn 10 ảnh đầu tiên."
                        else -> null
                    })
                }
            } finally {
                _uiState.update { it.copy(isAddingPhotos = false) }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilterAndSort(list: List<MediaImage>, query: String, order: SortOrder): List<MediaImage> {
        val trimmedQuery = query.trim()
        val filtered = if (trimmedQuery.isEmpty()) list else {
            list.filter { it.displayName.contains(trimmedQuery, ignoreCase = true) }
        }
        return when (order) {
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.dateAddedSeconds }
            SortOrder.DATE_ASC -> filtered.sortedBy { it.dateAddedSeconds }
            SortOrder.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase(Locale.getDefault()) }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase(Locale.getDefault()) }
        }
    }

    class Factory(private val repository: MediaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MiniGalleryViewModel::class.java)) {
                return MiniGalleryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
