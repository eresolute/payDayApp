package com.fh.payday.datasource.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Item(
        @SerializedName("name") @Expose val name: String,
        @SerializedName("res") @Expose val res: Int
) : Parcelable

data class StepperItem(
        @SerializedName("name") @Expose val name: String,
        @SerializedName("icon") @Expose val icon: Int,
        @SerializedName("selectedIcon") @Expose val selectedIcon: Int
)