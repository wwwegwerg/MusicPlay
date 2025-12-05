package com.example.musicplay.auth

import com.example.musicplay.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Ленивая фабрика Retrofit-сервиса, настроенного для общения с auth-бэкендом. */
object AuthApi {

    /**
     * Возвращает настроенный [AuthService], если указан базовый URL, иначе `null`,
     * чтобы UI мог показать понятное сообщение вместо падения при старте.
     */
    fun createService(): AuthService? {
        val baseUrl = BuildConfig.AUTH_BASE_URL
        if (baseUrl.isBlank()) return null
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }
}
