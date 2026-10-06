package com.example.coroutines.exercise7.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("StateFlow / SharedFlow", style = MaterialTheme.typography.headlineSmall)
        Text("Stop → phát vài giá trị → Start: StateFlow chỉ trả state mới nhất; replay=0 và buffer không giữ event; replay=1 phát lại event cuối.")
        Button(onClick = viewModel::start) { Text("Start collectors") }
        OutlinedButton(onClick = viewModel::stop) { Text("Stop collectors") }
        Button(onClick = viewModel::emit) { Text("Phát state/event mới") }
        OutlinedButton(onClick = viewModel::clearLogs) { Text("Xóa log") }
        if (state.running) LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(state.status, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        state.logs.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

