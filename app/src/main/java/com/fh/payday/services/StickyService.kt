package com.fh.payday.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.fh.payday.datasource.remote.auth.AuthService
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.preferences.UserPreferences


class StickyService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        CustomerService.clearDisposable()

        with (UserPreferences.instance) {
            val user = getUser(this@StickyService) ?: return@with clearPreferences(this@StickyService)
            AuthService.instance.logout(
                user.token, user.sessionId, user.refreshToken, user.customerId.toLong()
            )
            clearPreferences(this@StickyService)
        }
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}