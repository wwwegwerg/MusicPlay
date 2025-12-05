package com.example.musicplay.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val service: AuthService?
) {

    val isConfigured: Boolean
        get() = service != null

    suspend fun login(username: String, password: String): Result<AuthResponseDto> =
        execute { it.login(LoginDto(username, password)) }

    suspend fun register(username: String, password: String): Result<AuthResponseDto> =
        execute { it.register(RegisterDto(username, password)) }

    suspend fun fetchProfile(token: String): Result<PublicUserDto> =
        execute { it.me(token.asBearerToken()) }

    private suspend fun <T> execute(block: suspend (AuthService) -> T): Result<T> =
        withContext(Dispatchers.IO) {
            val svc = service ?: return@withContext Result.failure<T>(
                IllegalStateException("Auth API base URL is not configured")
            )
            return@withContext try {
                Result.success(block(svc))
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }

    private fun String.asBearerToken(): String = "Bearer $this"
}
