package com.fh.payday.utilities

import com.fh.payday.datasource.models.Card

interface OnCardSelectedListener {
    fun onCardSelected(index: Int, card: Card)
}