package com.example.coroutines.exercise2.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.exercise2.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class DemoState(
    val running: Boolean = false,
    val status: String = "Chọn một tình huống",
    val logs: List<String> = emptyList()
)

class MainScreenViewModel : ViewModel() {
    private val state = MutableStateFlow(DemoState())
    val uiState = state.asStateFlow()
    private var job: Job? = null
    private fun log(text: String) {
        state.update { it.copy(logs = it.logs + text) }
    }

    fun run(fail: Boolean, partial: Boolean) {
        if (job?.isActive == true) return
        state.value = DemoState(true, "Đang tải...")
        job = viewModelScope.launch {
            val start = System.nanoTime()
            val repository = DashboardRepository(FakeDashboardApi(fail, ::log))
            try {
                val result =
                    if (partial) repository.loadParts().toString() else repository.load().toString()
                state.update { it.copy(status = "Success: $result") }
            } catch (e: CancellationException) {
                state.update { it.copy(status = "Đã hủy") }
                throw e
            } catch (e: Exception) {
                state.update { it.copy(status = "Error: " + e.message) }
            } finally {
                log("Thời gian: " + (System.nanoTime() - start) / 1_000_000 + " ms")
                state.update { it.copy(running = false) }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }
}

