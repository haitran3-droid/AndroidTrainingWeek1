package com.example.coroutines.exercise4.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.coroutines.exercise4.data.User
import com.example.coroutines.exercise4.data.UserRepository

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  val repository = remember(context) { UserRepository.create(context) }
  val viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(repository) }
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text("Offline-first Users", style = MaterialTheme.typography.headlineSmall)
        Text("Room là nguồn dữ liệu duy nhất")
      }
      Button(onClick = viewModel::refresh, enabled = !state.isRefreshing) { Text("Refresh") }
    }

    if (state.isRefreshing) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator()
        Text("Đang tải network; cache vẫn hiển thị")
      }
    }
    state.error?.let { message ->
      Column {
        Text(message, color = MaterialTheme.colorScheme.error)
        OutlinedButton(onClick = viewModel::dismissError) { Text("Ẩn lỗi") }
      }
    }
    if (state.users.isEmpty() && !state.isRefreshing) Text("Chưa có dữ liệu cache")
    LazyColumn {
      items(state.users, key = User::id) { user ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(Modifier.weight(1f)) {
            Text(user.name, style = MaterialTheme.typography.titleMedium)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
          }
          OutlinedButton(onClick = { viewModel.deleteUser(user.id) }) { Text("Delete") }
        }
        HorizontalDivider()
      }
    }
  }
}
