package com.example.androidtrainingexample.minigallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.androidtrainingexample.R
import com.example.androidtrainingexample.minigallery.model.*
import com.example.androidtrainingexample.minigallery.ui.theme.MiniGalleryTheme

/** Stateless screen callbacks keep Android launchers and storage work outside composition. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGalleryScreen(
    state: MiniGalleryUiState,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onRequestPermission: () -> Unit,
    onAddPhotos: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    // URI survives Activity recreation; revoking access removes the detail from the screen.
    var detailUri by rememberSaveable { mutableStateOf<String?>(null) }
    val detailImage = state.allImages.firstOrNull { it.uri.toString() == detailUri }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            Button(onClick = onAddPhotos, enabled = !state.isAddingPhotos) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_add_photos))
            }
        }
    ) { contentPadding ->
        Column(Modifier.fillMaxSize().padding(contentPadding).consumeWindowInsets(contentPadding)) {
            PermissionBanner(state.permissionState, onRequestPermission)
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                label = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_search), contentDescription = null)
                },
                singleLine = true
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.photo_count_format, state.displayedCount, state.totalCount),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    TextButton(onClick = { sortMenuExpanded = true }) {
                        Icon(painterResource(R.drawable.ic_sort), stringResource(R.string.sort_tooltip))
                        Spacer(Modifier.width(4.dp))
                        Text(state.sortOrder.label)
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label) },
                                onClick = {
                                    sortMenuExpanded = false
                                    onSortOrderChange(order)
                                }
                            )
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                if (state.displayedImages.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.displayedImages, key = { it.uri.toString() }) { image ->
                            Card(onClick = { detailUri = image.uri.toString() }) {
                                Photo(image.uri, image.displayName, Modifier.fillMaxWidth().aspectRatio(1f))
                                Text(
                                    image.displayName,
                                    Modifier.padding(6.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                } else if (!state.isLoading && !state.isAddingPhotos) {
                    Column(
                        Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(painterResource(R.drawable.ic_image), null, Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(
                            if (state.searchQuery.isNotEmpty()) R.string.empty_search_msg
                            else R.string.empty_gallery_msg
                        ))
                    }
                }
                if (state.isLoading || state.isAddingPhotos) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }

    detailImage?.let { image -> ImageDetailDialog(image) { detailUri = null } }
}

@Composable
private fun PermissionBanner(permission: PermissionState, onRequestPermission: () -> Unit) {
    if (permission == PermissionState.GrantedFull) return
    val partial = permission is PermissionState.GrantedPartial
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_warning), null)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (partial) R.string.permission_partial_title else R.string.permission_denied_title),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(
                stringResource(if (partial) R.string.permission_partial_desc else R.string.permission_denied_desc),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onRequestPermission) {
                Text(stringResource(if (partial) R.string.permission_manage_btn else R.string.permission_grant_btn))
            }
        }
    }
}

@Composable
private fun Photo(uri: Uri, description: String?, modifier: Modifier, contentScale: ContentScale = ContentScale.Crop) {
    AsyncImage(
        model = uri,
        contentDescription = description,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = painterResource(R.drawable.ic_image),
        error = painterResource(R.drawable.ic_image)
    )
}

@Composable
private fun ImageDetailDialog(image: MediaImage, onDismiss: () -> Unit) {
    GalleryDialog(onDismiss) {
        Text(stringResource(R.string.dialog_details_title), style = MaterialTheme.typography.titleLarge)
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Photo(image.uri, image.displayName, Modifier.fillMaxWidth().height(240.dp), ContentScale.Fit) }
            item { Metadata(stringResource(R.string.label_file_name), image.displayName) }
            item { Metadata(stringResource(R.string.label_mime_type), image.mimeType) }
            item { Metadata(stringResource(R.string.label_file_size), "${image.formattedSize} (${image.sizeBytes} bytes)") }
            item { Metadata(stringResource(R.string.label_date_added), image.formattedDate) }
            item { Metadata(stringResource(R.string.label_content_uri), image.uri.toString()) }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.btn_close))
        }
    }
}

@Composable
private fun Metadata(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        SelectionContainer { Text(value, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun GalleryDialog(
    onDismiss: () -> Unit,
    dismissible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible)
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 640.dp).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Preview(showBackground = true, locale = "vi")
@Composable
private fun MiniGalleryPreview() {
    MiniGalleryTheme {
        MiniGalleryScreen(MiniGalleryUiState(), {}, {}, {}, {})
    }
}
