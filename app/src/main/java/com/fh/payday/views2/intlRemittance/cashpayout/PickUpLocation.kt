package com.fh.payday.views2.intlRemittance.cashpayout

data class PickUpLocation (val locationName:String,val locationDetails: String?, val  payOutBranchId: Long)
data class PickUpBankName(val bankName :String, val bankDetails :String?, val payOutAgentId : Int)

interface OnPickUpLocationListener {
    fun onLocationSelect(pickUpLocation: PickUpLocation)
}

interface OnPickBankNameListener {
    fun onBankSelect(pickUpBankName: PickUpBankName)
}