package com.example.diemdanhsinhvien.network.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.diemdanhsinhvien.activity.LoginActivity 
import com.example.diemdanhsinhvien.data.response.ApiLoginResponse
import com.example.diemdanhsinhvien.manager.SessionManager
import com.example.diemdanhsinhvien.network.apiservice.APIClient
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val context: Context,
    private val sessionManager: SessionManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = sessionManager.fetchRefreshToken() ?: return null

        return synchronized(this) {
            val currentAccessToken = sessionManager.fetchAccessToken()
            val originalRequestAccessToken = response.request.header("Authorization")?.substring(7)

            if (currentAccessToken != null && currentAccessToken != originalRequestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val newTokens: ApiLoginResponse? = runBlocking {
                try {
                    val tokenApi = APIClient.tokenApi(context)
                    val refreshResponse = tokenApi.refreshToken("Bearer $refreshToken")
                    if (refreshResponse.isSuccessful) refreshResponse.body() else null
                } catch (e: Exception) {
                    Log.e("TokenAuthenticator", "Failed to refresh token", e)
                    null
                }
            }

            if (newTokens?.accessToken != null && newTokens.refreshToken != null) {
                sessionManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else { 
                sessionManager.clearTokens()
                context.startActivity(Intent(context, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                null
            }
        }
    }
}