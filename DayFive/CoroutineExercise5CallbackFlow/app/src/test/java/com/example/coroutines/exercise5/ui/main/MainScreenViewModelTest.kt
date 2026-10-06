package com.example.coroutines.exercise5.ui.main

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
  @Test
  fun callbackFlowUnregistersWhenCollectorIsCancelled() = runTest {
    val source = FakeCallbackSource()
    val received = mutableListOf<Int>()
    val job = backgroundScope.launch { source.values().collect(received::add) }
    runCurrent()

    source.emit(7)
    runCurrent()
    assertEquals(listOf(7), received)

    job.cancel()
    runCurrent()
    assertNull(source.listener)
    assertEquals(1, source.removeCalls)
  }
}

private class FakeCallbackSource {
  var listener: ((Int) -> Unit)? = null
  var removeCalls = 0

  fun values(): Flow<Int> = callbackFlow {
    listener = { trySend(it) }
    awaitClose {
      listener = null
      removeCalls++
    }
  }

  fun emit(value: Int) = listener?.invoke(value)
}
