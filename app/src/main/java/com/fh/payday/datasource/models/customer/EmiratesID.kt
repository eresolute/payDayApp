package com.fh.payday.datasource.models.customer

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.File

data class EmiratesID(
    @SerializedName("emiratesId") var id: String = "",
    @SerializedName("dob") @Expose var dob: String = "",
    @SerializedName("gender") @Expose var gender: String = "",
    @SerializedName("expiry") @Expose var expiry: String = "",
    @SerializedName("country") @Expose var country: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(dob)
        parcel.writeString(gender)
        parcel.writeString(expiry)
        parcel.writeString(country)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun isValid() = id.isNotEmpty() || dob.isNotEmpty() || gender.isNotEmpty() || expiry.isNotEmpty() || country.isNotEmpty()

    companion object CREATOR : Parcelable.Creator<EmiratesID> {
        override fun createFromParcel(parcel: Parcel): EmiratesID {
            return EmiratesID(parcel)
        }

        override fun newArray(size: Int): Array<EmiratesID?> {
            return arrayOfNulls(size)
        }
    }
}

@Parcelize
data class EmiratesIdWrapper(
    val fileFront: File,
    val fileBack: File,
    val emiratesId: EmiratesID
) : Parcelable
