package com.fh.payday.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.fh.payday.R
import com.fh.payday.utilities.Action
import com.fh.payday.utilities.isAppInBackground
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        remoteMessage?.data?.let {
            showNotification(it)
            handleNotificationData(it)
        }
    }


    private fun showNotification(data: Map<String, String>) {

        /*val postLoginAction = if (data["productType"] == OfferActivity.BANNER) {
            Intent(this, OfferActivity::class.java)
        } else {
            Intent(this, CampaignActivity::class.java)
        }

        val intent = when (MainActivity.isActive) {
            true -> when (UserPreferences.instance.getUser(this)) {
                null -> Intent(data["click_action"])
                else -> postLoginAction
            }
            else -> Intent(data["click_action"])
        }

        intent.apply {
            putExtra("title", data["title"])
            putExtra("body", data["body"])
            putExtra("expiryDays", data["expiryDays"])
            putExtra("notificationId", data["notificationId"])
            putExtra("productType", data["productType"])
        }*/

        val intent = Intent(this, OfferReceiver::class.java).apply {
            putExtra("title", data["title"])
            putExtra("body", data["body"])
            putExtra("click_action", data["click_action"])
            putExtra("expiryDays", data["expiryDays"])
            putExtra("notificationId", data["notificationId"])
            putExtra("productType", data["productType"])
        }


        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setAutoCancel(true)
            .setContentTitle(data["title"])
            .setContentText(data["body"])
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data["body"]))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        manager?.notify(0, builder.build())
    }

    private fun handleNotificationData(data: Map<String, String>) {
        if (!isAppInBackground(this)) {
            val title = data["title"]
            val body = data["body"]
            val expiryDays = data["expiryDays"]
            val productType = data["productType"]

            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(Action.ACTION_OFFER).apply {
                    putExtra("title", title)
                    putExtra("body", body)
                    putExtra("expiryDays", expiryDays)
                    putExtra("productType", productType)
                })
        }
    }

}