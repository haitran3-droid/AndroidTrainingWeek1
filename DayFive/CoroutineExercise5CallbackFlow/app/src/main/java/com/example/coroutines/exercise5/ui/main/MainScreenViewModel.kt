package com.example.coroutines.exercise5.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.exercise5.data.LocationPoint
import com.example.coroutines.exercise5.data.locationUpdates
import com.example.coroutines.exercise5.data.messagesFlow
import com.example.coroutines.exercise5.data.networkStatusFlow
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CallbackFlowUiState(
  val hasValidatedNetwork: Boolean? = null,
  val location: LocationPoint? = null,
  val locationStatus: String = "Chưa theo dõi vị trí",
  val firebaseStatus: String = "Chưa collect Firebase",
)

class MainScreenViewModel(
  private val context: Context,
  private val locationClient: FusedLocationProviderClient,
) : ViewModel() {
  private val _uiState = MutableStateFlow(CallbackFlowUiState())
  val uiState: StateFlow<CallbackFlowUiState> = _uiState.asStateFlow()
  private var locationJob: Job? = null
  private var firebaseJob: Job? = null

  init {
    viewModelScope.launch {
      context.networkStatusFlow()
        .catch { _uiState.update { state -> state.copy(hasValidatedNetwork = false) } }
        .collect { connected -> _uiState.update { it.copy(hasValidatedNetwork = connected) } }
    }
  }

  fun startLocation() {
    if (locationJob?.isActive == true) return
    locationJob = viewModelScope.launch {
      _uiState.update { it.copy(locationStatus = "Đang chờ location callback") }
      locationClient.locationUpdates()
        .catch { error ->
          _uiState.update { it.copy(locationStatus = error.message ?: "Location lỗi") }
        }
        .collect { point ->
          _uiState.update { it.copy(location = point, locationStatus = "Đang theo dõi") }
        }
    }
  }

  fun stopLocation() {
    locationJob?.cancel()
    _uiState.update { it.copy(locationStatus = "Đã hủy và gỡ LocationCallback") }
  }

  fun startFirebase() {
    if (firebaseJob?.isActive == true) return
    if (FirebaseApp.getApps(context).isEmpty()) {
      _uiState.update {
        it.copy(firebaseStatus = "Chưa cấu hình google-services.json; adapter đã sẵn sàng trong CallbackFlows.kt")
      }
      return
    }
    firebaseJob = viewModelScope.launch {
      FirebaseDatabase.getInstance().getReference("messages").messagesFlow()
        .catch { error -> _uiState.update { it.copy(firebaseStatus = error.message ?: "Firebase lỗi") } }
        .collect { messages ->
          _uiState.update { it.copy(firebaseStatus = "Nhận ${messages.size} messages") }
        }
    }
  }
}
