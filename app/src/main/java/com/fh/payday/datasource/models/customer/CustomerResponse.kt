package com.fh.payday.datasource.models.customer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CustomerResponse (
    @SerializedName("id")
    @Expose
    val id: Long,

    @SerializedName("mobile")
    @Expose
    val mobileNo: String
)
