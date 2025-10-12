package com.example.diemdanhsinhvien.data.response

import com.google.gson.annotations.SerializedName

data class ApiLoginResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("account")
    val account: LoginResponse
)