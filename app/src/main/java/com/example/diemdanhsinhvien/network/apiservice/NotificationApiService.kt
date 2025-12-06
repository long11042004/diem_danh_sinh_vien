package com.example.diemdanhsinhvien.network.apiservice

import com.example.diemdanhsinhvien.data.model.Notification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationApiService {
    @GET("notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @PUT("notifications/{notificationId}/read")
    suspend fun markAsRead(@Path("notificationId") notificationId: String): Response<Unit>

    @POST("notifications")
    suspend fun createNotification(@Body notification: Notification): Response<Notification>
}