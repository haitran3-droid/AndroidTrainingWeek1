package com.example.coroutines.exercise4.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class UserDto(val id: Long, val name: String, val email: String)
data class User(val id: Long, val name: String, val email: String)

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: Long,
  @ColumnInfo(name = "full_name") val name: String,
  val email: String,
)

fun UserDto.toEntity() = UserEntity(id, name, email)
fun UserEntity.toUser() = User(id, name, email)
