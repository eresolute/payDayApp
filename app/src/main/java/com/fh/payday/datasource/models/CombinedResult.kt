package com.fh.payday.datasource.models

data class CombinedResult<R1, R2>(
    val response1: R1,
    val response2: R2
)