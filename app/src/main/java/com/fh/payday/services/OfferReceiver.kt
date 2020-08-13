package com.fh.payday.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views2.campaign.CampaignActivity
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.notification.NotificationActivity
import com.fh.payday.views2.offer.OfferActivity

class OfferReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val data = intent?.extras ?: return
        if (!isValidOffer(data)) return

        val postLoginIntent = when (data.getString("productType")) {
            OfferActivity.BANNER -> Intent(context, OfferActivity::class.java)
            LoanViewModel.LOAN -> Intent(context, CampaignActivity::class.java)
            LoanViewModel.TOPUP_LOAN -> Intent(context, CampaignActivity::class.java)
            else -> Intent(context, NotificationActivity::class.java)
        }

        val newIntent = when (MainActivity.isActive) {
            true -> when (UserPreferences.instance.getUser(context)) {
                null -> Intent(data.getString("click_action"))
                else -> postLoginIntent
            }
            else -> Intent(data.getString("click_action"))
        }

        newIntent.putExtras(data)
        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(newIntent)
    }

    private fun isValidOffer(bundle: Bundle?): Boolean {
        bundle ?: return false
        val title: String? = bundle.getString("title")
        val description: String? = bundle.getString("body")

        return when(val productType: String? = bundle.getString("productType")) {
            OfferActivity.BANNER -> !title.isNullOrEmpty() &&  !productType.isNullOrEmpty()
            else -> !title.isNullOrEmpty() && !description.isNullOrEmpty() && !productType.isNullOrEmpty()
        }
    }

    companion object {
        const val OFFER_NOTIFICATION = "new.offer.notification"
    }
}