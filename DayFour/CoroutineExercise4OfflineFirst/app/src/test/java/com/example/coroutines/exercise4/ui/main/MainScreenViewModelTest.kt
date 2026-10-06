package com.example.coroutines.exercise4.ui.main

import com.example.coroutines.exercise4.data.User
import com.example.coroutines.exercise4.data.UserRepositoryContract
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
  private val mainDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(mainDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun cachedUsersRemainVisibleWhenRefreshFails() = runTest {
    val cached = listOf(User(1, "Cached user", "cached@example.com"))
    val refreshStarted = CompletableDeferred<Unit>()
    val repository = object : UserRepositoryContract {
      override fun observeUsers(): Flow<List<User>> = MutableStateFlow(cached)
      override suspend fun refresh(): Result<Unit> {
        refreshStarted.complete(Unit)
        return Result.failure(IllegalStateException("HTTP 500"))
      }
      override suspend fun deleteUser(userId: Long): Result<Unit> = Result.success(Unit)
    }

    val viewModel = MainScreenViewModel(repository)
    val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
    runCurrent()
    refreshStarted.await()
    runCurrent()

    assertEquals(cached, viewModel.uiState.value.users)
    assertEquals("HTTP 500", viewModel.uiState.value.error)
    assertFalse(viewModel.uiState.value.isRefreshing)
    collectJob.cancelAndJoin()
    runCurrent()
  }
}
