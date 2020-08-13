package com.fh.payday.datasource.models.shared

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListModel(
        @SerializedName("key") @Expose val key: String,
        @SerializedName("value") @Expose val value: String
) : Parcelable