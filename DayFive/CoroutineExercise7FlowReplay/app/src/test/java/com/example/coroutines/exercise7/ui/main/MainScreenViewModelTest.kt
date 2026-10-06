package com.example.coroutines.exercise7.ui.main
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
  @Test fun onlyStateAndReplaySurviveWithoutSubscribers() = runTest {
    val state = MutableStateFlow(0)
    val events = MutableSharedFlow<String>()
    val buffer = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val replay = MutableSharedFlow<String>(replay = 1)
    state.value = 2; state.value = 3
    events.emit("lost"); buffer.emit("lost"); replay.emit("kept")
    val seenEvents = mutableListOf<String>()
    val seenBuffer = mutableListOf<String>()
    val seenReplay = mutableListOf<String>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { events.collect { seenEvents.add(it) } }
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { buffer.collect { seenBuffer.add(it) } }
    val first = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { replay.collect { seenReplay.add(it) } }
    assertEquals(3, state.first())
    assertTrue(seenEvents.isEmpty()); assertTrue(seenBuffer.isEmpty())
    first.cancelAndJoin()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { replay.collect { seenReplay.add(it) } }
    assertEquals(listOf("kept", "kept"), seenReplay)
    events.emit("new"); buffer.emit("new")
    assertEquals(listOf("new"), seenEvents); assertEquals(listOf("new"), seenBuffer)
  }
}
