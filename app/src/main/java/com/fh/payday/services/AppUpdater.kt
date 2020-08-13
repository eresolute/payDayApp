package com.fh.payday.services

import com.fh.payday.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class AppUpdater private constructor(
    private val updateListener: AppUpdateListener?
) {

    interface AppUpdateListener {
        fun onUpdateAvailable(storeLink: String, versionName: String, isForce: Boolean = false)
    }

    fun check() {
        val config = FirebaseRemoteConfig.getInstance()
        val versionCode = config.getLong("update_version_code")
        if (versionCode > BuildConfig.VERSION_CODE) {
            updateListener?.onUpdateAvailable(config.getString("playstore_link"),
                config.getString("update_version_name"), config.getBoolean("force_update_required"))
        }
    }

    class Builder(private var updateListener: AppUpdateListener) {

        fun build(): AppUpdater {
            return AppUpdater(updateListener)
        }

        fun check(): AppUpdater {
            return build().also { it.check() }
        }
    }

    companion object {
        fun with(updateListener: AppUpdateListener): Builder {
            return Builder(updateListener)
        }
    }

}