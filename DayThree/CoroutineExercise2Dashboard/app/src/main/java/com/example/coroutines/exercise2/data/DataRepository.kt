package com.example.coroutines.exercise2.data

import kotlinx.coroutines.*

data class Profile(val name: String)
data class Order(val id: String)
data class NotificationItem(val message: String)
data class Dashboard(val profile: Profile, val orders: List<Order>, val notifications: List<NotificationItem>)
data class DashboardParts(val profile: Result<Profile>, val orders: Result<List<Order>>, val notifications: Result<List<NotificationItem>>)

class FakeDashboardApi(private val failOrders: Boolean = false, private val log: (String) -> Unit = {}) {
  private suspend fun <T> request(name: String, duration: Long, result: () -> T): T {
    log("$name bắt đầu")
    try { delay(duration); return result() }
    catch (e: CancellationException) { log("$name bị cancel"); throw e }
    finally { log("$name cleanup") }
  }
  suspend fun getProfile() = request("profile", 2_000) { Profile("An") }
  suspend fun getOrders() = request("orders", if (failOrders) 300 else 3_000) {
    if (failOrders) error("Không tải được orders")
    listOf(Order("O-01"))
  }
  suspend fun getNotifications() = request("notifications", 1_000) { listOf(NotificationItem("Có đơn hàng mới")) }
}

class DashboardRepository(private val api: FakeDashboardApi) {
  suspend fun load(): Dashboard = coroutineScope {
    val profile = async { api.getProfile() }
    val orders = async { api.getOrders() }
    val notifications = async { api.getNotifications() }
    Dashboard(profile.await(), orders.await(), notifications.await())
  }
  suspend fun loadParts(): DashboardParts = supervisorScope {
    val profile = async { api.getProfile() }
    val orders = async { api.getOrders() }
    val notifications = async { api.getNotifications() }
    DashboardParts(awaitResult(profile), awaitResult(orders), awaitResult(notifications))
  }
}
private suspend fun <T> awaitResult(value: Deferred<T>): Result<T> = try {
  Result.success(value.await())
} catch (e: CancellationException) { throw e }
catch (e: Exception) { Result.failure(e) }

