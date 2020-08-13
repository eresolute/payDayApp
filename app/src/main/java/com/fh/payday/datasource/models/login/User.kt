package com.fh.payday.datasource.models.login

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        @SerializedName("username")
        @Expose
        val username: String,

        @SerializedName("role")
        @Expose
        val role: String,

        @SerializedName("token")
        @Expose
        val token: String,

        @SerializedName("refreshToken")
        @Expose
        val refreshToken: String,

        @SerializedName("customerId")
        @Expose
        val customerId: Int,

        @SerializedName("lastLogin")
        @Expose
        val lastLogin: String,

        @SerializedName("sessionID")
        @Expose
        val sessionId : String,

        val customerName: String? = null,

        var profilePic: String? = "",

        @SerializedName("sessionTimeout")
        @Expose
        val sessionTimeout: Long
): Parcelable