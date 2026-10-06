package com.example.coroutines.exercise5.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class ChatMessage(val id: String = "", val text: String = "")
data class LocationPoint(val latitude: Double, val longitude: Double)

fun DatabaseReference.messagesFlow(): Flow<List<ChatMessage>> = callbackFlow {
  val listener = object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
      try {
        val messages = snapshot.children.mapNotNull { child ->
          child.getValue(ChatMessage::class.java)?.copy(id = child.key.orEmpty())
        }
        trySend(messages)
      } catch (error: Exception) {
        close(error)
      }
    }

    override fun onCancelled(error: DatabaseError) {
      close(error.toException())
    }
  }
  addValueEventListener(listener)
  awaitClose { removeEventListener(listener) }
}.buffer(Channel.CONFLATED)

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationUpdates(): Flow<LocationPoint> = callbackFlow {
  val client = this@locationUpdates
  val closed = AtomicBoolean(false)
  val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5_000L).build()
  val callback = object : LocationCallback() {
    override fun onLocationResult(result: LocationResult) {
      if (closed.get()) return
      val location = result.lastLocation ?: return
      trySend(LocationPoint(location.latitude, location.longitude))
    }
  }

  client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    .addOnSuccessListener { if (closed.get()) client.removeLocationUpdates(callback) }
    .addOnFailureListener { close(it) }

  awaitClose {
    closed.set(true)
    client.removeLocationUpdates(callback)
  }
}.buffer(Channel.CONFLATED)

fun Context.networkStatusFlow(): Flow<Boolean> = callbackFlow {
  val manager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  fun validated(capabilities: NetworkCapabilities?): Boolean =
    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

  val initialNetwork = manager.activeNetwork
  trySend(validated(initialNetwork?.let(manager::getNetworkCapabilities)))
  val trackedNetwork = AtomicReference<Network?>(null)
  val callback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      trackedNetwork.set(network)
    }

    override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
      if (trackedNetwork.get() == network) trySend(validated(capabilities))
    }

    override fun onLost(network: Network) {
      val current = trackedNetwork.get()
      if (current == network && trackedNetwork.compareAndSet(current, null)) trySend(false)
    }
  }
  manager.registerDefaultNetworkCallback(callback)
  awaitClose { manager.unregisterNetworkCallback(callback) }
}.buffer(Channel.CONFLATED).distinctUntilChanged()
