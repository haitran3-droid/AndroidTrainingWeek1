package com.example.coroutines.exercise2.ui.main

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
        Text("Dashboard tải đồng thời", style = MaterialTheme.typography.headlineSmall)
        Text("Log bên dưới hiển thị thứ tự chạy và cleanup. Xoay màn hình vẫn giữ thí nghiệm trong ViewModel.")
        Button(
            onClick = { viewModel.run(false, false) },
            enabled = !state.running
        ) { Text("Tải toàn bộ thành công") }
        Button(
            onClick = { viewModel.run(true, false) },
            enabled = !state.running
        ) { Text("Orders lỗi → hủy siblings") }
        Button(
            onClick = { viewModel.run(true, true) },
            enabled = !state.running
        ) { Text("supervisorScope → kết quả từng phần") }
        OutlinedButton(onClick = viewModel::cancel, enabled = state.running) { Text("Hủy request") }
        if (state.running) LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(state.status, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        state.logs.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

