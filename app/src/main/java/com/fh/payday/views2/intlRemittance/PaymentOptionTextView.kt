package com.fh.payday.views2.intlRemittance

import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.views.custombindings.setDrawableLeft


fun setBackGround(textView1 :TextView, textView2 :TextView, context :Context){
    textView1.background = ContextCompat.getDrawable(context, R.drawable.bg_rounded_blue_border_16dp)
    textView2.background = ContextCompat.getDrawable(context, R.drawable.bg_blue_gradient)
}

fun setTextColor(textView1: TextView,textView2: TextView, context: Context){
    textView1.setTextColor(ContextCompat.getColor(context, R.color.black))

    textView2.setTextColor(ContextCompat.getColor(context, R.color.white))
}

fun setDrawableBT(blackIcon :TextView, whiteIcon :TextView){
  //  setDrawableLeft(blackIcon, R.id.)
    setDrawableLeft(whiteIcon, R.drawable.ic_bank_white)
}

fun setDrawableCP(blackIcon :TextView, whiteIcon :TextView){
    setDrawableLeft(blackIcon, R.drawable.ic_bank_xs)
}