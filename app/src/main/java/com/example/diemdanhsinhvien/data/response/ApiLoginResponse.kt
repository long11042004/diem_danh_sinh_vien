package com.example.diemdanhsinhvien.data.response

import com.google.gson.annotations.SerializedName

data class ApiLoginResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("account")
    val account: LoginResponse
)