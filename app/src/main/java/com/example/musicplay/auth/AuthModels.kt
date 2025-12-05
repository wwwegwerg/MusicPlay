package com.example.musicplay.auth

import com.google.gson.annotations.SerializedName

/** Тело запроса при логине. */
data class LoginDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

/** Тело запроса при регистрации нового аккаунта. */
data class RegisterDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

/** Публичная часть профиля пользователя, возвращаемая бэкендом. */
data class PublicUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

/** Универсальный ответ, приходящий как после логина, так и после регистрации. */
data class AuthResponseDto(
    @SerializedName("user") val user: PublicUserDto,
    @SerializedName("token") val token: String
)
