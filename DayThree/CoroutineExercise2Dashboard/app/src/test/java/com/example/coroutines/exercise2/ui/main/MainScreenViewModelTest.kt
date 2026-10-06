package com.example.coroutines.exercise2.ui.main
import com.example.coroutines.exercise2.data.*
import kotlinx.coroutines.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
  @Test fun parallelRequestsTakeMaximumDuration() = runTest {
    val result = DashboardRepository(FakeDashboardApi()).load()
    assertEquals(3_000L, currentTime)
    assertEquals("An", result.profile.name)
  }
  @Test fun failureCancelsSiblingsButSupervisorPreservesResults() = runTest {
    val logs = mutableListOf<String>()
    try { DashboardRepository(FakeDashboardApi(true, logs::add)).load(); fail("Expected error") }
    catch (e: IllegalStateException) { assertTrue(logs.contains("profile bị cancel")); assertTrue(logs.contains("notifications bị cancel")) }
    val parts = DashboardRepository(FakeDashboardApi(true)).loadParts()
    assertTrue(parts.profile.isSuccess)
    assertTrue(parts.orders.isFailure)
    assertTrue(parts.notifications.isSuccess)
  }
}
