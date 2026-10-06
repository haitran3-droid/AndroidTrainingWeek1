package com.example.coroutines.exercise4.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface UserRepositoryContract {
  fun observeUsers(): Flow<List<User>>
  suspend fun refresh(): Result<Unit>
  suspend fun deleteUser(userId: Long): Result<Unit>
}

class UserRepository(
  private val api: UserApi,
  private val dao: UserDao,
  private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UserRepositoryContract {
  override fun observeUsers(): Flow<List<User>> = dao.observeUsers()
    .map { entities -> entities.map(UserEntity::toUser) }
    .distinctUntilChanged()
    .flowOn(computeDispatcher)

  override suspend fun refresh(): Result<Unit> = safeApiCall {
    val response = api.getUsers()
    if (!response.isSuccessful) throw HttpException(response)
    val body = response.body() ?: error("HTTP thành công nhưng body rỗng")
    val entities = withContext(computeDispatcher) { body.map(UserDto::toEntity) }
    dao.replaceAll(entities)
  }

  override suspend fun deleteUser(userId: Long): Result<Unit> = safeApiCall {
    val response = api.deleteUser(userId)
    if (!response.isSuccessful) throw HttpException(response)
    dao.deleteById(userId)
  }

  companion object {
    fun create(context: android.content.Context): UserRepository {
      val api = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UserApi::class.java)
      return UserRepository(api, UserDatabase.getInstance(context).userDao())
    }
  }
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
  Result.success(block())
} catch (error: CancellationException) {
  throw error
} catch (error: Exception) {
  Result.failure(error)
}
