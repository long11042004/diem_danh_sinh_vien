package com.example.diemdanhsinhvien.data.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("teacherId")
    val teacherId: String
)