package com.example.coroutines.exercise6.ui.main
import com.example.coroutines.exercise6.data.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class MainScreenViewModelTest {
  @Test fun cancellationIsConnectedOnlyForCancellableAdapter() = runTest {
    assertEquals(Comparison(0, 0), compareCancellation(false, false))
    assertEquals(Comparison(1, 0), compareCancellation(true, false))
    assertEquals(Comparison(1, 0), compareCancellation(true, true))
  }
  @Test fun duplicateCallbacksCompleteOnce() = runTest {
    assertEquals(Comparison(0, 1), compareCancellation(true, false, cancelCaller = false))
    assertEquals(Comparison(0, 1), compareCancellation(false, false, cancelCaller = false))
  }
}
