package com.example.musicplay.auth

import android.content.Context
import androidx.core.content.edit

/**
 * Лёгкий помощник поверх SharedPreferences, хранящий токен и сведения о пользователе,
 * чтобы другие части приложения могли быстро проверять состояние сессии.
 */
class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Сохраняет токен и поля пользователя из ответа авторизации. */
    fun saveAuth(response: AuthResponseDto) {
        preferences.edit {
            putString(KEY_TOKEN, response.token)
            putString(KEY_USER_ID, response.user.id)
            putString(KEY_USER_NAME, response.user.username)
        }
    }

    /** Обновляет кэшированные данные пользователя, не трогая токен. */
    fun saveUser(user: PublicUserDto) {
        preferences.edit {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
        }
    }

    fun getToken(): String? = preferences.getString(KEY_TOKEN, null)

    fun getUsername(): String? = preferences.getString(KEY_USER_NAME, null)

    /** Возвращает `true`, если в хранилище есть непустой токен. */
    fun hasSession(): Boolean = !getToken().isNullOrBlank()

    /** Удаляет все сохранённые данные авторизации. */
    fun clearSession() {
        preferences.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "auth_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
    }
}
