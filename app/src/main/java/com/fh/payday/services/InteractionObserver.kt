package com.fh.payday.services

import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.customer.CustomerService

// val interactionObservable: Subject<Boolean> = PublishSubject.create()

class InteractionObserver {
    companion object {
        var status = false

        fun start(
            user: User,
            sessionTimeout: Long,
            callback: ApiCallback<String>
        ) {
            with(user) {
                CustomerService.getInstance(token, sessionId, refreshToken)
                    .resetSession(customerId.toLong(), sessionTimeout, callback)
            }
        }
    }
}
