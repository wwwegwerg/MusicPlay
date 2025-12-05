package com.example.musicplay.auth

import com.google.gson.annotations.SerializedName

data class LoginDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class PublicUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class AuthResponseDto(
    @SerializedName("user") val user: PublicUserDto,
    @SerializedName("token") val token: String
)
