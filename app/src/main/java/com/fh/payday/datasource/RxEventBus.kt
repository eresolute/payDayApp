package com.fh.payday.datasource

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class RxEventBus<T> {

    private val eventBus = PublishSubject.create<T>()

    fun sendEvent(t: T) {
        eventBus.onNext(t)
    }

    fun toObservable(): Observable<T> = eventBus

}

object BalanceEventBus : RxEventBus<Double>()