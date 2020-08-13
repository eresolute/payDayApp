package com.fh.payday.datasource.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiResult<T>(
        @SerializedName("code")
        @Expose
        val code: Int,

        @SerializedName("status")
        @Expose
        val status: String,

        @SerializedName("message")
        @Expose
        val message: String?,

        @SerializedName("data")
        @Expose
        val data: T?,

        @SerializedName("errors")
        @Expose
        val errors : List<String>?,

        @Expose val token: String? = null,

        @Expose val refreshToken: String? = null
)

/*
sealed class ApiResult {
    data class Success<T>(
            @SerializedName("code")
            @Expose
            val code: Int,

            @SerializedName("status")
            @Expose
            val status: String,

            @SerializedName("message")
            @Expose
            val message: String?,

            @SerializedName("data")
            @Expose
            val data: T?,

            @SerializedName("errors")
            @Expose
            val errors : List<String>?

    ) : ApiResult()

    data class Error(
            @SerializedName("code")
            @Expose
            val code: Int,

            @SerializedName("status")
            @Expose
            val status: String,

            @SerializedName("errors")
            @Expose
            val errors : List<String>
    ) : ApiResult()
}*/
