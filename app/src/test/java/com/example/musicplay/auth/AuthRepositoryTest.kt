package com.example.musicplay.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun loginReturnsSuccessWhenServiceResponds() = runTest {
        val fakeService = FakeAuthService()
        val repository = AuthRepository(fakeService)

        val result = repository.login("mike", "secret")

        assertTrue(result.isSuccess)
        val payload = result.getOrNull()
        assertEquals("mike", payload?.user?.username)
        assertEquals("token_login", payload?.token)
        assertEquals(LoginDto("mike", "secret"), fakeService.lastLogin)
    }

    @Test
    fun fetchProfileAddsBearerPrefix() = runTest {
        val fakeService = FakeAuthService()
        val repository = AuthRepository(fakeService)

        val result = repository.fetchProfile("abc123")

        assertTrue(result.isSuccess)
        assertEquals("Bearer abc123", fakeService.lastBearerToken)
    }

    @Test
    fun loginFailsWhenApiIsMissing() = runTest {
        val repository = AuthRepository(service = null)

        val result = repository.login("ghost", "password")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
    }

    private class FakeAuthService : AuthService {

        var lastLogin: LoginDto? = null
        var lastRegister: RegisterDto? = null
        var lastBearerToken: String? = null

        override suspend fun login(loginDto: LoginDto): AuthResponseDto {
            lastLogin = loginDto
            return AuthResponseDto(
                user = PublicUserDto(
                    id = "id_login",
                    username = loginDto.username,
                    createdAt = "",
                    updatedAt = ""
                ),
                token = "token_login"
            )
        }

        override suspend fun register(registerDto: RegisterDto): AuthResponseDto {
            lastRegister = registerDto
            return AuthResponseDto(
                user = PublicUserDto(
                    id = "id_register",
                    username = registerDto.username,
                    createdAt = "",
                    updatedAt = ""
                ),
                token = "token_register"
            )
        }

        override suspend fun me(bearerToken: String): PublicUserDto {
            lastBearerToken = bearerToken
            return PublicUserDto(
                id = "id_me",
                username = "verified",
                createdAt = "",
                updatedAt = ""
            )
        }
    }
}
