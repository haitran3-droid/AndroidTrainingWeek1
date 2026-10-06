package com.example.coroutines.exercise6.data

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*

data class CallbackUser(val id: String, val name: String)
interface UserCallback { fun onSuccess(user: CallbackUser); fun onError(error: Throwable) }
fun interface CancelableCall { fun cancel() }
class FakeUserApi(
  private val workerScope: CoroutineScope,
  private val deliverAfterCancel: Boolean = false,
  private val log: (String) -> Unit = {},
) {
  val cancelCalls = AtomicInteger()
  fun fetchUser(callback: UserCallback): CancelableCall {
    val job = workerScope.launch {
      delay(500)
      log("API phát callback success rồi error lặp")
      callback.onSuccess(CallbackUser("U-01", "An"))
      callback.onError(IllegalStateException("Callback bị gọi lần hai"))
    }
    return CancelableCall {
      cancelCalls.incrementAndGet()
      log("API.cancel()")
      if (!deliverAfterCancel) job.cancel()
    }
  }
}
suspend fun FakeUserApi.awaitUser(): CallbackUser = suspendCoroutine { continuation ->
  val completed = AtomicBoolean()
  fetchUser(object : UserCallback {
    override fun onSuccess(user: CallbackUser) { if (completed.compareAndSet(false, true)) continuation.resume(user) }
    override fun onError(error: Throwable) { if (completed.compareAndSet(false, true)) continuation.resumeWithException(error) }
  })
}
suspend fun FakeUserApi.awaitUserCancellable(): CallbackUser = suspendCancellableCoroutine { continuation ->
  val completed = AtomicBoolean()
  val call = fetchUser(object : UserCallback {
    override fun onSuccess(user: CallbackUser) { if (completed.compareAndSet(false, true)) continuation.resume(user) }
    override fun onError(error: Throwable) { if (completed.compareAndSet(false, true)) continuation.resumeWithException(error) }
  })
  continuation.invokeOnCancellation { completed.set(true); call.cancel() }
}
data class Comparison(val cancelCalls: Int, val uiUpdates: Int)
suspend fun compareCancellation(
  cancellable: Boolean,
  deliverAfterCancel: Boolean,
  log: (String) -> Unit = {},
  cancelCaller: Boolean = true,
): Comparison = coroutineScope {
  // External SDK scope is independent of the caller.
  val workerScope = CoroutineScope(SupervisorJob() + currentCoroutineContext().minusKey(Job))
  val api = FakeUserApi(workerScope, deliverAfterCancel, log)
  var uiUpdates = 0
  try {
    val caller = launch {
      val user = if (cancellable) api.awaitUserCancellable() else api.awaitUser()
      log("Hàm trả về; caller active = " + currentCoroutineContext().isActive)
      currentCoroutineContext().ensureActive()
      uiUpdates++
      log("UI nhận " + user.name)
    }
    if (cancelCaller) {
      delay(100)
      val start = System.nanoTime()
      caller.cancelAndJoin()
      log("cancelAndJoin: " + (System.nanoTime() - start) / 1_000_000 + " ms")
      delay(600)
    } else caller.join()
    log("API.cancel = " + api.cancelCalls.get() + "; UI updates = $uiUpdates")
    Comparison(api.cancelCalls.get(), uiUpdates)
  } finally {
    withContext(NonCancellable) { workerScope.coroutineContext[Job]?.cancelAndJoin() }
  }
}

