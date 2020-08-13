package com.fh.payday

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric


/**
 * PayDayFH
 * Created by EResolute on 9/11/2018.
 */
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}