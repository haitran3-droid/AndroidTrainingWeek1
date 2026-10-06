package com.example.androidtrainingexample.minigallery.model

sealed interface PermissionState {
    data object Denied : PermissionState

    data class GrantedPartial(
        val selectedCount: Int = 0
    ) : PermissionState

    data object GrantedFull : PermissionState
}
