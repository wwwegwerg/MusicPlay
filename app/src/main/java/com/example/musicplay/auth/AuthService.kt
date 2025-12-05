package com.example.musicplay.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/** Retrofit-описание эндпоинтов авторизации на бэкенде. */
interface AuthService {

    /** Выполняет вход по логину/паролю и возвращает auth-пayload. */
    @POST("auth/login")
    suspend fun login(@Body loginDto: LoginDto): AuthResponseDto

    /** Создаёт новый аккаунт и возвращает auth-пayload в том же формате, что и логин. */
    @POST("auth/register")
    suspend fun register(@Body registerDto: RegisterDto): AuthResponseDto

    /** Загружает информацию о владельце переданного токена. */
    @GET("auth/me")
    suspend fun me(
        @Header("Authorization") bearerToken: String
    ): PublicUserDto
}
