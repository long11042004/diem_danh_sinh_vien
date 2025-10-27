package com.example.diemdanhsinhvien.data.response

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)