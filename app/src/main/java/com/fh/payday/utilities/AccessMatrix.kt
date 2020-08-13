package com.fh.payday.utilities


const val INACTIVE = "inactive"
const val ACTIVE = "active"
const val STOP_LIST = "stoplist"
const val SUSPENDED = "suspended"
const val BLOCKED = "blocked"
const val REPLACED = "replaced"
const val CANCELLED = "cancelled"

class AccessMatrix {
    companion object {

        val CARD_DETAILS = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to true,
                SUSPENDED to true,
                BLOCKED to true,
                REPLACED to true,
                CANCELLED to true
        )

        val LOAN_DETAILS = CARD_DETAILS

        val MOBILE_NUMBER_UPDATE = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val EMAIL_UPDATE = MOBILE_NUMBER_UPDATE

        val EID_UPDATE = MOBILE_NUMBER_UPDATE

        val PP_UPDATE = MOBILE_NUMBER_UPDATE

        val APPLY_LOAN = mapOf(
                INACTIVE to false,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val APPLY_TOPUP_LOAN = APPLY_LOAN

        val RESCHEDULE = APPLY_LOAN

        val EARLY_SETTLEMENT = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val LIABILITY_LETTER = EARLY_SETTLEMENT

        val CLEARANCE_LETTER = EARLY_SETTLEMENT

        val PARTIAL_SETTLEMENT = APPLY_LOAN

        val PAYMENT_HOLIDAY = APPLY_LOAN

        val TRANSACTION_HISTORY = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val OFFERS = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val UTILITY_PAYMENT_LOCAL = mapOf(
                INACTIVE to false,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val UTILITY_PAYMENT_INTL = UTILITY_PAYMENT_LOCAL

        val P2P_SENDER = mapOf(
                INACTIVE to false,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val P2P_RECEIVER = P2P_SENDER

        val INTERNATIOM_REMITTANCE = P2P_SENDER

        val LOCAL_MONEY_TRANSFER_TO_ACCOUNT = P2P_SENDER

        val LOCAL_MONEY_TRANSFER_TO_CREDIT_CARD = P2P_SENDER

        val CARD_STATEMENT = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val TRANSACTIONS = CARD_STATEMENT

        val SALARY_CREDITED = CARD_STATEMENT

        val CHANGE_PIN = mapOf(
                INACTIVE to false,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val BLOCK_CARD = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val ACTIVATE_CARD = mapOf(
                INACTIVE to true,
                ACTIVE to false,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )

        val CARD_TRANSACTION_DISPUTE = mapOf(
                INACTIVE to true,
                ACTIVE to true,
                STOP_LIST to false,
                SUSPENDED to false,
                BLOCKED to false,
                REPLACED to false,
                CANCELLED to false
        )


    }
}

fun hasAccess(cardStatus: String?, map: Map<String, Boolean>): Boolean {
    return map[cardStatus?.toLowerCase() ?: INACTIVE] ?: map[INACTIVE]!!
}







