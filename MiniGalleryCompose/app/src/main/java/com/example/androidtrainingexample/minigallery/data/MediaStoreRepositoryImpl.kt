package com.example.androidtrainingexample.minigallery.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.androidtrainingexample.minigallery.model.MediaImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

/** Đọc ảnh qua ContentResolver, quan sát MediaStore và đọc URI nguồn từ Photo Picker. */
class MediaStoreRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val context: Context
) : MediaRepository {

    // Only source URIs and selection dates are saved; no image files are copied.
    private val pickedPhotos = context.getSharedPreferences("picked_photos", Context.MODE_PRIVATE)

    override suspend fun persistPickedImage(image: MediaImage): Unit = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        contentResolver.takePersistableUriPermission(image.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val key = image.uri.toString()
        // Keep the original selection date when the same URI is selected again.
        val selectedAt = pickedPhotos.getLong(key, image.dateAddedSeconds)
        if (!pickedPhotos.edit().putLong(key, selectedAt).commit()) {
            throw IOException("Không thể lưu ảnh đã chọn")
        }
    }

    override suspend fun loadPickedImages(): List<MediaImage> = withContext(Dispatchers.IO) {
        val restored = mutableListOf<MediaImage>()
        for ((source, selectedAt) in pickedPhotos.all) {
            currentCoroutineContext().ensureActive()
            if (selectedAt !is Long) continue
            try {
                val image = getMediaInfo(Uri.parse(source))
                restored.add(image.copy(dateAddedSeconds = selectedAt))
            } catch (_: SecurityException) {
                // The source may have lost its URI grant; do not publish an unreadable image.
            } catch (_: IOException) {
                // Deleted or currently unavailable sources are skipped without affecting others.
            }
        }
        restored
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    override fun observeImages(): Flow<List<MediaImage>> {
        return callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    trySend(Unit)
                }
            }

            // Đăng ký theo dõi toàn bộ collection ảnh trên bộ nhớ ngoài
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )

            // Phát tín hiệu ban đầu để tải dữ liệu ngay khi vừa collect
            trySend(Unit)

            // Hủy đăng ký khi flow bị đóng / scope bị cancel
            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
            .conflate() // Bỏ qua các tín hiệu dồn dập
            .debounce(300L) // Gộp các thông báo thay đổi liên tiếp để tránh query MediaStore quá nhiều
            .map { queryImages() }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun queryImages(): List<MediaImage> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<MediaImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )

        // Sắp xếp mặc định: ảnh mới thêm vào trước
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Images.Media.IS_PENDING} = 0",
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                currentCoroutineContext().ensureActive()
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "IMG_$id.jpg"
                val mime = cursor.getString(mimeColumn) ?: "image/jpeg"
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateColumn)

                // Tạo Uri theo quy chuẩn MediaStore Content Provider
                val itemUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                resultList.add(
                    MediaImage(
                        id = id,
                        uri = itemUri,
                        displayName = name,
                        mimeType = mime,
                        sizeBytes = size,
                        dateAddedSeconds = dateAdded
                    )
                )
            }
        }
        // Để lỗi quyền/I/O tới ViewModel, không báo sai thành thư viện trống.

        resultList
    }

    /** Read the source URI directly; no MediaStore insert, copy, or write occurs. */
    override suspend fun getMediaInfo(uri: Uri): MediaImage = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        var displayName = "selected_photo.jpg"
        var sizeBytes = 0L

        contentResolver.openInputStream(uri)?.use { }
            ?: throw IOException("Không thể đọc ảnh đã chọn")
        try {
            contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex)?.let { displayName = it }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0) sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        } catch (error: Exception) {
            if (error is CancellationException || error is SecurityException) throw error
            // Some providers cannot supply optional metadata; the readable source is still usable.
        }
        currentCoroutineContext().ensureActive()
        MediaImage(
            id = 0L,
            uri = uri,
            displayName = displayName,
            mimeType = contentResolver.getType(uri) ?: "image/jpeg",
            sizeBytes = sizeBytes,
            dateAddedSeconds = System.currentTimeMillis() / 1000L
        )
    }
}
