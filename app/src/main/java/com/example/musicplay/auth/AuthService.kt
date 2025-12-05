package com.example.musicplay.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    @POST("auth/login")
    suspend fun login(@Body loginDto: LoginDto): AuthResponseDto

    @POST("auth/register")
    suspend fun register(@Body registerDto: RegisterDto): AuthResponseDto

    @GET("auth/me")
    suspend fun me(
        @Header("Authorization") bearerToken: String
    ): PublicUserDto
}
