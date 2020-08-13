package com.fh.payday.datasource.models.moneytransfer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocalTransferResponse (
    @SerializedName("FTReferenceNo") @Expose val FTReferenceNo: String,
    @SerializedName("CMSAuthCode") @Expose val CMSAuthCode: String,
    @SerializedName("ResponseCode") @Expose val ResponseCode: String,
    @SerializedName("ResponseDesc") @Expose val ResponseDesc: String,
    @SerializedName("Balance") @Expose val Balance: Double
)