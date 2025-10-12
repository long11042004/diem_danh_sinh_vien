package com.example.diemdanhsinhvien.manager

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        val editor = prefs.edit()
        editor.putString(ACCESS_TOKEN, accessToken)
        editor.putString(REFRESH_TOKEN, refreshToken)
        editor.apply()
    }

    fun fetchAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)

    fun fetchRefreshToken(): String? = prefs.getString(REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}