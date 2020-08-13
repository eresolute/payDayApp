package com.fh.payday.views2.intlRemittance.transfer.adapter

data class SummaryUI(
    val label: String,
    val title: String,
    val heading1: String? = null,
    val heading2: String? = null,
    val isEditable: Boolean = false,
    val status: String? = null,
    val exchange: String? = null,
    val exchangeLogo: Int = 0
    )