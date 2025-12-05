package com.example.musicplay.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Небольшая обёртка над [AuthService], позволяющая держать API nullable и
 * корректно сообщать пользователю об отсутствующей конфигурации (например, пустой AUTH_BASE_URL).
 */
class AuthRepository(
    private val service: AuthService?
) {

    val isConfigured: Boolean
        get() = service != null

    /** Выполняет попытку авторизации и возвращает токен/пользователя для указанных данных. */
    suspend fun login(username: String, password: String): Result<AuthResponseDto> =
        execute { it.login(LoginDto(username, password)) }

    /** Регистрирует нового пользователя и сохраняет полученные данные авторизации. */
    suspend fun register(username: String, password: String): Result<AuthResponseDto> =
        execute { it.register(RegisterDto(username, password)) }

    /** Загружает профиль текущего пользователя, используя сохранённый bearer-токен. */
    suspend fun fetchProfile(token: String): Result<PublicUserDto> =
        execute { it.me(token.asBearerToken()) }

    /** Выполняет сетевой вызов на IO-диспетчере и оборачивает исключения в [Result.failure]. */
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
