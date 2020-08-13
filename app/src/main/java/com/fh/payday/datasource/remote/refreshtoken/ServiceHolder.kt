package com.fh.payday.datasource.remote.refreshtoken

class ServiceHolder(
    val customerId: Long,
    val refreshToken: String,
    val service: RefreshTokenService?
)