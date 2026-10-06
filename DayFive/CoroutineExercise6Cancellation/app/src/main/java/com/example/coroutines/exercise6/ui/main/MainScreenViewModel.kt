package com.example.coroutines.exercise6.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.exercise6.data.compareCancellation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class DemoState(val running: Boolean = false, val status: String = "Chọn một adapter", val logs: List<String> = emptyList())
class MainScreenViewModel : ViewModel() {
  private val state = MutableStateFlow(DemoState())
  val uiState = state.asStateFlow()
  private var job: Job? = null
  fun run(cancellable: Boolean, late: Boolean, cancelCaller: Boolean = true) {
    if (job?.isActive == true) return
    state.value = DemoState(true, "Đang chạy thí nghiệm...")
    job = viewModelScope.launch {
      try {
        compareCancellation(cancellable, late, { line -> state.update { it.copy(logs = it.logs + line) } }, cancelCaller)
        state.update { it.copy(status = "Hoàn tất") }
      } catch (e: CancellationException) { throw e }
      catch (e: Exception) { state.update { it.copy(status = e.message.orEmpty()) } }
      finally { state.update { it.copy(running = false) } }
    }
  }
}

