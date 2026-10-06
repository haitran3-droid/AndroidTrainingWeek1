package com.example.coroutines.exercise1.ui.main

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CountdownState(
  val seconds: Int = 10,
  val isRunning: Boolean = false,
)

class MainScreenViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(CountdownState())
  val uiState: StateFlow<CountdownState> = _uiState.asStateFlow()
  private var timerJob: Job? = null

  @MainThread
  fun start() {
    val previousJob = timerJob
    if (previousJob?.isActive == true || _uiState.value.seconds <= 0) return
    timerJob = viewModelScope.launch {
      previousJob?.join()
      _uiState.update { it.copy(isRunning = true) }
      try {
        while (_uiState.value.seconds > 0) {
          delay(1_000)
          _uiState.update { it.copy(seconds = (it.seconds - 1).coerceAtLeast(0)) }
        }
      } finally {
        _uiState.update { it.copy(isRunning = false) }
      }
    }
  }

  @MainThread
  fun pause() {
    timerJob?.cancel()
    _uiState.update { it.copy(isRunning = false) }
  }

  @MainThread
  fun reset() {
    pause()
    _uiState.value = CountdownState()
  }
}
