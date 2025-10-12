package com.example.diemdanhsinhvien.data.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("loginName")
    val loginName: String,

    @SerializedName("fullName")
    val fullName: String
)