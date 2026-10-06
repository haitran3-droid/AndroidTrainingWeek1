package com.example.coroutines.exercise3.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.exercise3.data.SearchItem
import com.example.coroutines.exercise3.data.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SearchState {
  data object Idle : SearchState
  data object Loading : SearchState
  data class Success(val items: List<SearchItem>) : SearchState
  data class Error(val message: String) : SearchState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
fun searchStates(queries: Flow<String>, repository: SearchRepository): Flow<SearchState> =
  queries
    .map(String::trim)
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { query ->
      if (query.isBlank()) {
        flowOf<SearchState>(SearchState.Idle)
      } else {
        flow<SearchState> {
          emit(SearchState.Loading)
          emit(SearchState.Success(repository.search(query)))
        }.catch { error ->
          emit(SearchState.Error(error.message ?: "Tìm kiếm thất bại"))
        }
      }
    }

class MainScreenViewModel(repository: SearchRepository) : ViewModel() {
  private val queries = MutableStateFlow("")

  val uiState: StateFlow<SearchState> = searchStates(queries, repository)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchState.Idle)

  fun onQueryChanged(query: String) {
    queries.value = query
  }
}
