package com.example.androidtrainingexample.minigallery.data

import android.net.Uri
import com.example.androidtrainingexample.minigallery.model.MediaImage
import kotlinx.coroutines.flow.Flow

/**
 * Đọc ảnh từ MediaStore hoặc URI nguồn do Photo Picker trả về.
 */
interface MediaRepository {

    fun observeImages(): Flow<List<MediaImage>>

    suspend fun queryImages(): List<MediaImage>

    suspend fun getMediaInfo(uri: Uri): MediaImage

    suspend fun persistPickedImage(image: MediaImage)

    suspend fun loadPickedImages(): List<MediaImage>
}
