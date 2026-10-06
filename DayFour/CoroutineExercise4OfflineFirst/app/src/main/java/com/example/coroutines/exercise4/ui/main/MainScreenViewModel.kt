package com.example.coroutines.exercise4.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.exercise4.data.User
import com.example.coroutines.exercise4.data.UserRepositoryContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserListUiState(
  val users: List<User> = emptyList(),
  val isRefreshing: Boolean = false,
  val error: String? = null,
)

class MainScreenViewModel(private val repository: UserRepositoryContract) : ViewModel() {
  private val operationState = MutableStateFlow(UserListUiState())
  private var refreshJob: Job? = null

  val uiState: StateFlow<UserListUiState> = combine(
    repository.observeUsers(),
    operationState,
  ) { users, operation -> operation.copy(users = users) }
    .catch { error -> emit(UserListUiState(error = error.message ?: "Không đọc được database")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserListUiState())

  init {
    refresh()
  }

  fun refresh() {
    if (refreshJob?.isActive == true) return
    refreshJob = viewModelScope.launch {
      operationState.update { it.copy(isRefreshing = true, error = null) }
      try {
        repository.refresh().onFailure { error ->
          operationState.update { it.copy(error = error.message ?: "Refresh thất bại") }
        }
      } finally {
        operationState.update { it.copy(isRefreshing = false) }
      }
    }
  }

  fun deleteUser(userId: Long) {
    viewModelScope.launch {
      repository.deleteUser(userId).onFailure { error ->
        operationState.update { it.copy(error = error.message ?: "Xóa thất bại") }
      }
    }
  }

  fun dismissError() {
    operationState.update { it.copy(error = null) }
  }
}
