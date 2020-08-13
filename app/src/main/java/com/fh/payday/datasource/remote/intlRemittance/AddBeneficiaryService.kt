package com.fh.payday.datasource.remote.intlRemittance

import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.*

class AddBeneficiaryService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        private val INSTANCE: AddBeneficiaryService by lazy { AddBeneficiaryService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): AddBeneficiaryService {
            return INSTANCE.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }


    fun getPayoutCountries(customerId: Long, accessKey: String,  deliveryMode: String, callback: ApiCallbackImpl<List<PayoutCountries>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(AddBeneficiaryApiInterface::class.java)
        val call = service.getPayoutCountries(customerId, accessKey, deliveryMode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getRelationShips(customerId: Long, payoutAgentId: String,deliveryMode: String,  relation: String, callback: ApiCallbackImpl<List<RelationsItem>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(AddBeneficiaryApiInterface::class.java)
        val call = service.getRelationShips(customerId,payoutAgentId, deliveryMode, relation)
        call.enqueue(CallbackImpl(callback))
    }

    fun createBeneficiaries(customerId: Long, accessKey: String, alFardan : AlFardanIntlAddBeneficiary,
                            payoutAgentId: Long, payoutBranchId: Long, otp: String, callback: ApiCallback<CreateBeneficiaries>){

        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(AddBeneficiaryApiInterface::class.java)
        val call = service.createBeneficiaries(customerId, accessKey, alFardan.countryName ?: return,
            alFardan.currency ?: return, alFardan.firstName ?: return,alFardan.lastName ?: return,
            alFardan.contactNo ?: return, alFardan.nationality ?: return,
            alFardan.addressLine ?: return,alFardan.relationShip ?: return,
            alFardan.bankName ?: return,alFardan.city ?: " ", alFardan.state ?: " ",
            alFardan.branchName ?: return,alFardan.branchAddress ?: " ",alFardan.accountNo ?: " ",
            alFardan.ifscCode ?: " ", alFardan.swiftCode ?: " ", alFardan.iBan ?: " ",
            alFardan.deliveryMode ?: return, alFardan.flagPath, payoutAgentId, payoutBranchId, alFardan.IdNo!!, otp)
        call.enqueue(CallbackImpl(callback))

    }

    fun getPayoutAgents(customerId: Long, accessKey : String, countryCode: String, deliveryMode: String, callback: ApiCallback<List<PayoutAgent>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(AddBeneficiaryApiInterface::class.java)
        val call = service.payoutAgents(customerId, accessKey, countryCode, deliveryMode)
        call.enqueue(CallbackImpl(callback))
    }
    fun getPayoutAgentBranches(customerId: Long, accessKey : String, agentId: Long,
                               deliveryMode: String, optional1: String, optional2: String,
                               callback: ApiCallback<List<PayoutAgentBranches>>
    ) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(AddBeneficiaryApiInterface::class.java)
        val call = service.payoutAgentBranches(customerId, accessKey, agentId, deliveryMode, optional1, optional2)
        call.enqueue(CallbackImpl(callback))
    }

    fun addToFavBeneficiary(customerId: Long,accessKey: String, receiverRefNumber :String, flag : Int, callback: ApiCallback<String>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(AddBeneficiaryApiInterface::class.java)
        val call = service.addToFavBeneficiary(customerId, accessKey, receiverRefNumber ?:return, flag)
        call.enqueue(CallbackStringImpl(callback))
    }
}