package com.example.coroutines.exercise4.data

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
  @GET("users")
  suspend fun getUsers(): Response<List<UserDto>>

  @DELETE("users/{id}")
  suspend fun deleteUser(@Path("id") userId: Long): Response<Unit>
}
