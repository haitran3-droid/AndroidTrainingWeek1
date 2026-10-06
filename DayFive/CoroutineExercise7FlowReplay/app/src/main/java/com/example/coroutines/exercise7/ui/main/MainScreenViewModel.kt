package com.example.coroutines.exercise7.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class DemoState(val running: Boolean = false, val status: String = "Collector đang dừng", val logs: List<String> = emptyList())
class MainScreenViewModel : ViewModel() {
  private val state = MutableStateFlow(DemoState())
  val uiState = state.asStateFlow()
  val counter = MutableStateFlow(0)
  val events = MutableSharedFlow<String>()
  val bufferedEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
  val replayEvents = MutableSharedFlow<String>(replay = 1)
  private var collectors: Job? = null
  private var operation: Job? = null
  private var eventId = 0
  private fun log(text: String) { state.update { it.copy(logs = (it.logs + text).takeLast(100)) } }
  fun start() {
    if (operation?.isActive == true || collectors?.isActive == true) return
    collectors = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
      launch(start = CoroutineStart.UNDISPATCHED) { counter.collect { log("StateFlow nhận $it") } }
      launch(start = CoroutineStart.UNDISPATCHED) { events.collect { log("SharedFlow(0) nhận $it") } }
      launch(start = CoroutineStart.UNDISPATCHED) { bufferedEvents.collect { log("Buffer(64) nhận $it") } }
      launch(start = CoroutineStart.UNDISPATCHED) { replayEvents.collect { log("Replay(1) nhận $it") } }
    }
    state.update { it.copy(status = "Collector đang chạy") }
  }
  fun stop() {
    if (operation?.isActive == true) return
    operation = viewModelScope.launch {
      collectors?.cancelAndJoin()
      collectors = null
      state.update { it.copy(status = "Collector đã dừng; có thể phát event") }
      log("Đã cancelAndJoin collector")
    }
  }
  fun emit() {
    if (operation?.isActive == true) return
    operation = viewModelScope.launch {
      val id = ++eventId
      counter.value = id
      events.emit("E$id")
      bufferedEvents.emit("E$id")
      replayEvents.emit("E$id")
      log("Đã phát $id; subscribers = " + events.subscriptionCount.value)
    }
  }
  fun clearLogs() { state.update { it.copy(logs = emptyList()) } }
}

