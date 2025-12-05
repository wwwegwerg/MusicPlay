package com.example.musicplay.auth

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuth(response: AuthResponseDto) {
        preferences.edit {
            putString(KEY_TOKEN, response.token)
            putString(KEY_USER_ID, response.user.id)
            putString(KEY_USER_NAME, response.user.username)
        }
    }

    fun saveUser(user: PublicUserDto) {
        preferences.edit {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
        }
    }

    fun getToken(): String? = preferences.getString(KEY_TOKEN, null)

    fun getUsername(): String? = preferences.getString(KEY_USER_NAME, null)

    fun hasSession(): Boolean = !getToken().isNullOrBlank()

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
