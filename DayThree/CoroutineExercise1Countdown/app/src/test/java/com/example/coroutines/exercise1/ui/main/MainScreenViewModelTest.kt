package com.example.coroutines.exercise1.ui.main

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
  @Test
  fun startPauseAndReset_manageSingleTimerJob() = runTest {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    try {
      val viewModel = MainScreenViewModel()
      viewModel.start()
      viewModel.start()
      runCurrent()
      assertTrue(viewModel.uiState.value.isRunning)

      advanceTimeBy(3_000)
      runCurrent()
      assertEquals(7, viewModel.uiState.value.seconds)

      viewModel.pause()
      runCurrent()
      advanceTimeBy(2_000)
      runCurrent()
      assertEquals(7, viewModel.uiState.value.seconds)
      assertFalse(viewModel.uiState.value.isRunning)

      viewModel.reset()
      assertEquals(CountdownState(), viewModel.uiState.value)
    } finally {
      Dispatchers.resetMain()
    }
  }
}
