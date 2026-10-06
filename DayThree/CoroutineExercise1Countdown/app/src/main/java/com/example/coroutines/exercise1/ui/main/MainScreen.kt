package com.example.coroutines.exercise1.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.coroutines.exercise1.theme.CoroutineExercise1CountdownTheme

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel(),
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  CountdownContent(state, viewModel::start, viewModel::pause, viewModel::reset, modifier)
}

@Composable
private fun CountdownContent(
  state: CountdownState,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onReset: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text("Countdown timer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(
      text = state.seconds.toString(),
      modifier = Modifier.padding(vertical = 32.dp),
      fontSize = 80.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
    )
    Text(if (state.isRunning) "Đang chạy" else "Đã dừng", style = MaterialTheme.typography.bodyLarge)
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
      Button(onClick = onStart, enabled = !state.isRunning && state.seconds > 0) { Text("Start") }
      OutlinedButton(onClick = onPause, enabled = state.isRunning) { Text("Pause") }
      OutlinedButton(onClick = onReset) { Text("Reset") }
    }
    Text(
      "Start nhiều lần không tạo thêm timer. Pause và Reset hủy Job hiện tại.",
      modifier = Modifier.padding(top = 32.dp),
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun CountdownPreview() {
  CoroutineExercise1CountdownTheme {
    CountdownContent(CountdownState(7, true), {}, {}, {}, Modifier.padding(16.dp))
  }
}
