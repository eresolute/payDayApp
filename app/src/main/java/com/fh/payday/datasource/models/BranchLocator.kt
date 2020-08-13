package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BranchLocator(
        @SerializedName("ID") @Expose val ID: String,
        @SerializedName("branchName") @Expose val branchName: String,
        @SerializedName("address") @Expose val address: String,
        @SerializedName("fromTime") @Expose val fromTime: String?,
        @SerializedName("toTime") @Expose val toTime: String?,
        @SerializedName("tollFree") @Expose val tollFree: String,
        @SerializedName("latLng") @Expose val latLng: String,
        @SerializedName("telephone") @Expose val telephone: String,
        @SerializedName("emailID") @Expose val emailID: String,
        @SerializedName("fax") @Expose val fax: String,
        @SerializedName("locatorType") @Expose val locatorType: String,
        @SerializedName("latitude") @Expose val latitude: Double,
        @SerializedName("longitude") @Expose val longitude: Double,
        @SerializedName("workingDays") @Expose val workingDays: String,
        @SerializedName("country") @Expose val country: String,
        @SerializedName("city") @Expose val city: String)

data class IntlBranchLocator(
        @SerializedName("latitude")
        @Expose
        val latitude: Double,
        @SerializedName("longitude")
        @Expose
        val longitude: Double,
        @SerializedName("branchName")
        @Expose
        val branchName: String,
        @SerializedName("address")
        @Expose
        val address: String,
        @SerializedName("city")
        @Expose
        val city: String,
        @SerializedName("country")
        @Expose
        val country: String,
        @SerializedName("workingDays")
        @Expose
        val workingDays: String,
        @SerializedName("tollFree")
        @Expose
        val tollFree: String,
        @SerializedName("telephone")
        @Expose
        val telephone: String,
        @SerializedName("emailID")
        @Expose
        val emailID: String,
        @SerializedName("fax")
        @Expose
        val fax: String,
        @SerializedName("Type")
        @Expose
        val Type: String,
        @SerializedName("fromTime")
        @Expose
        val fromTime: String,
        @SerializedName("toTime")
        @Expose
        val toTime: String,
        @SerializedName("sundayTiming")
        @Expose
        val sundayTiming: String?,
        @SerializedName("mondayTiming")
        @Expose
        val mondayTiming: String?,
        @SerializedName("tuesdayTiming")
        @Expose
        val tuesdayTiming: String?,
        @SerializedName("wednesdayTiming")
        @Expose
        val wednesdayTiming: String?,
        @SerializedName("thursdayTiming")
        @Expose
        val thursdayTiming: String?,
        @SerializedName("fridayTiming")
        @Expose
        val fridayTiming: String?,
        @SerializedName("saturdayTiming")
        @Expose
        val saturdayTiming: String?
)

data class BranchLocatorDetails(val addressDetails: String, val tollFree: String, val telephone: String, val fax: String, val email: String)