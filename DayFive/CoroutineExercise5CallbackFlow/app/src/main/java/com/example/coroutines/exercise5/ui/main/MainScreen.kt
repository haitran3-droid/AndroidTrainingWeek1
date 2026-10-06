package com.example.coroutines.exercise5.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.google.android.gms.location.LocationServices

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val applicationContext = context.applicationContext
  val locationClient = remember(applicationContext) {
    LocationServices.getFusedLocationProviderClient(applicationContext)
  }
  val viewModel: MainScreenViewModel = viewModel {
    MainScreenViewModel(applicationContext, locationClient)
  }
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
  ) { result ->
    if (result.values.any { it }) viewModel.startLocation()
  }

  fun requestOrStartLocation() {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
      viewModel.startLocation()
    } else {
      permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("callbackFlow adapters", style = MaterialTheme.typography.headlineSmall)
    InfoCard("ConnectivityManager") {
      Text(
        when (state.hasValidatedNetwork) {
          null -> "Đang đăng ký NetworkCallback"
          true -> "Mạng hiện tại đã được hệ thống VALIDATED"
          false -> "Chưa có mạng Internet được VALIDATED"
        },
      )
    }
    InfoCard("FusedLocationProviderClient") {
      Text(state.locationStatus)
      state.location?.let { Text("${it.latitude}, ${it.longitude}") }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = ::requestOrStartLocation) { Text("Start") }
        OutlinedButton(onClick = viewModel::stopLocation) { Text("Stop") }
      }
    }
    InfoCard("Firebase Realtime Database") {
      Text(state.firebaseStatus)
      Button(onClick = viewModel::startFirebase) { Text("Collect messages") }
    }
    Text("Mỗi Flow đăng ký listener khi collect và gỡ listener trong awaitClose khi bị hủy.")
  }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
  Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium)
      content()
    }
  }
}
