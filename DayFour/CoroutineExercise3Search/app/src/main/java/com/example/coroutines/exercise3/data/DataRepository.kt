package com.example.coroutines.exercise3.data

import kotlinx.coroutines.delay

data class SearchItem(val id: String, val title: String)

interface SearchRepository {
  suspend fun search(query: String): List<SearchItem>
}

class FakeSearchRepository : SearchRepository {
  private val catalog = listOf(
    "Kotlin Coroutines",
    "Kotlin Flow",
    "StateFlow",
    "SharedFlow",
    "callbackFlow",
    "Room Database",
    "Retrofit",
    "Jetpack Compose",
  )

  override suspend fun search(query: String): List<SearchItem> {
    delay(800)
    if (query.equals("error", ignoreCase = true)) error("Request tìm kiếm thất bại")
    return catalog
      .filter { it.contains(query, ignoreCase = true) }
      .mapIndexed { index, title -> SearchItem("$query-$index", title) }
  }
}
