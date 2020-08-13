package com.fh.payday.datasource.models.intlRemittance

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RelationsItem(
    @SerializedName("code") @Expose val code: String,
    @SerializedName("name") @Expose val name: String
) {
    override fun toString(): String {
        return name
    }
}