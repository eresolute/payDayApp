package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class QuestionBank(
        @SerializedName("name") @Expose val name: String,
        @SerializedName("detail") @Expose val detail: String
)