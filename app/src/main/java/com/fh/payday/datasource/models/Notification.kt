package com.fh.payday.datasource.models

data class Notification(
    val Id: Long,
    val CIF: String?,
    val notificationDate: String,
    val offerTitle: String?,
    val notificationBody: String?,
    val category: String?,
    val type: String?,
    val flag: Int,
    val createdAt: String,
    val updatedAt: String
)
