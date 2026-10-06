package com.example.notificationpractice

import androidx.annotation.StringRes

data class DemoMessage(
    val id: String,
    val notificationId: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
)

object DemoMessages {
    val a = DemoMessage("A", 101, R.string.message_a_title, R.string.message_a_body)
    val b = DemoMessage("B", 102, R.string.message_b_title, R.string.message_b_body)
    fun find(id: String?): DemoMessage? = when (id) {
        a.id -> a
        b.id -> b
        else -> null
    }
}

