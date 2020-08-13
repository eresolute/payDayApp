package com.fh.payday.datasource.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IndianState(
        @SerializedName("state_alias")
        @Expose
        val stateAlias: String,
        @SerializedName("state")
        @Expose
        val stateName: String
) : Parcelable