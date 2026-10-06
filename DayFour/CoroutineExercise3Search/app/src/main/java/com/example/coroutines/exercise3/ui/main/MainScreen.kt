package com.example.coroutines.exercise3.ui.main

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.coroutines.exercise3.data.FakeSearchRepository
import com.example.coroutines.exercise3.data.SearchItem
import com.example.coroutines.exercise3.theme.CoroutineExercise3SearchTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

fun EditText.textChanges(): Flow<String> = callbackFlow {
  val watcher = object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun afterTextChanged(s: Editable?) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
      trySend(s?.toString().orEmpty())
    }
  }
  addTextChangedListener(watcher)
  trySend(text.toString())
  awaitClose { removeTextChangedListener(watcher) }
}.buffer(Channel.CONFLATED)

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(FakeSearchRepository()) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  var editText by remember { mutableStateOf<EditText?>(null) }

  LaunchedEffect(editText) {
    editText?.textChanges()?.collect(viewModel::onQueryChanged)
  }

  SearchContent(
    state = state,
    modifier = modifier,
    searchBox = {
      val context = LocalContext.current
      AndroidView(
        factory = {
          EditText(context).apply {
            hint = "Nhập Kotlin, Flow hoặc error"
            isSingleLine = true
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            editText = this
          }
        },
        modifier = Modifier.fillMaxWidth(),
      )
    },
  )
}

@Composable
private fun SearchContent(
  state: SearchState,
  modifier: Modifier = Modifier,
  searchBox: @Composable () -> Unit,
) {
  Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text("Search với callbackFlow", style = MaterialTheme.typography.headlineSmall)
    Text("TextWatcher → debounce → distinctUntilChanged → flatMapLatest")
    searchBox()
    when (state) {
      SearchState.Idle -> Text("Nhập từ khóa để tìm kiếm")
      SearchState.Loading -> CircularProgressIndicator()
      is SearchState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
      is SearchState.Success -> {
        if (state.items.isEmpty()) Text("Không có kết quả")
        LazyColumn {
          items(state.items, key = SearchItem::id) { item ->
            Text(item.title, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            HorizontalDivider()
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
  CoroutineExercise3SearchTheme {
    SearchContent(
      SearchState.Success(listOf(SearchItem("1", "Kotlin Flow"))),
      Modifier.padding(16.dp),
    ) { Text("[EditText]") }
  }
}
