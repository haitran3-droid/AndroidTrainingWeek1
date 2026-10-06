package com.example.androidtrainingexample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidtrainingexample.minigallery.data.MediaStoreRepositoryImpl
import com.example.androidtrainingexample.minigallery.ui.MiniGalleryScreen
import com.example.androidtrainingexample.minigallery.ui.MiniGalleryViewModel
import com.example.androidtrainingexample.minigallery.ui.theme.MiniGalleryTheme
import com.example.androidtrainingexample.minigallery.util.PermissionHelper

class MainActivity : ComponentActivity() {
    private val viewModel: MiniGalleryViewModel by viewModels {
        MiniGalleryViewModel.Factory(
            MediaStoreRepositoryImpl(applicationContext.contentResolver, applicationContext)
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshPermission() }

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris -> viewModel.onPhotosSelected(uris) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniGalleryTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.userMessage) {
                    state.userMessage?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearUserMessage()
                    }
                }
                MiniGalleryScreen(
                    state = state,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onSortOrderChange = viewModel::setSortOrder,
                    onRequestPermission = {
                        permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                    },
                    onAddPhotos = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh on return from Settings or Android partial-access photo selection.
        refreshPermission()
    }

    private fun refreshPermission() {
        viewModel.updatePermissionState(
            PermissionHelper.checkPermissionState(this, viewModel.uiState.value.totalCount)
        )
    }
}
