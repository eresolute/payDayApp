package com.fh.payday.datasource.models.login

import com.google.gson.annotations.Expose

data class RefreshToken(
    @Expose val token: String,
    @Expose val refreshToken: String
)
