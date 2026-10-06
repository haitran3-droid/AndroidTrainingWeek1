package com.example.coroutines.exercise4.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY full_name ASC")
  fun observeUsers(): Flow<List<UserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  @Query("DELETE FROM users")
  suspend fun deleteAll()

  @Query("DELETE FROM users WHERE id = :userId")
  suspend fun deleteById(userId: Long)

  @Transaction
  suspend fun replaceAll(users: List<UserEntity>) {
    deleteAll()
    insertUsers(users)
  }
}
