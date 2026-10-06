package com.example.coroutines.exercise3.ui.main

import com.example.coroutines.exercise3.data.SearchItem
import com.example.coroutines.exercise3.data.SearchRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainScreenViewModelTest {
  @Test
  fun debounceKeepsLatestQueryAndPipelineRecoversAfterError() = runTest {
    val queries = MutableSharedFlow<String>(extraBufferCapacity = 2)
    val requested = mutableListOf<String>()
    val states = mutableListOf<SearchState>()
    val repository = object : SearchRepository {
      override suspend fun search(query: String): List<SearchItem> {
        requested += query
        if (query == "error") error("boom")
        return listOf(SearchItem(query, query))
      }
    }
    backgroundScope.launch { searchStates(queries, repository).collect(states::add) }
    runCurrent()

    queries.emit("k")
    advanceTimeBy(100)
    queries.emit("ko")
    advanceTimeBy(300)
    runCurrent()
    assertEquals(listOf("ko"), requested)

    queries.emit("error")
    advanceTimeBy(300)
    runCurrent()
    assertTrue(states.last() is SearchState.Error)

    queries.emit("flow")
    advanceTimeBy(300)
    runCurrent()
    assertEquals("flow", (states.last() as SearchState.Success).items.single().id)
  }
}
