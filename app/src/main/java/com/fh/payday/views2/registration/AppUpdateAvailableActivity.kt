package com.fh.payday.views2.registration

import android.content.Intent
import android.net.Uri
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import kotlinx.android.synthetic.main.activity_app_update_available.*


class AppUpdateAvailableActivity : BaseActivity() {
    override fun onBackPressed() {
        when (intent?.getBooleanExtra(KEY_IS_FORCE_UPDATE, false)) {
            true -> exit()
            else -> super.onBackPressed()
        }
    }

    override fun getLayout(): Int = R.layout.activity_app_update_available

    override fun init() {
        val versionName = requireNotNull(intent?.getStringExtra(KEY_VERSION_NAME)) { "$KEY_VERSION_NAME not found" }
        val link = requireNotNull(intent?.getStringExtra(KEY_PLAY_STORE_LINK)) { "$KEY_PLAY_STORE_LINK not found" }

        text_view.text = getString(R.string.app_update_available)
            .format(versionName, getString(R.string.app_name))
        btn_update.setOnClickListener { redirectStore(link) }

        val isForceUpdate = intent?.getBooleanExtra(KEY_IS_FORCE_UPDATE, false) ?: false
        if (isForceUpdate) { tv_not_now.visibility = View.GONE }

        tv_not_now.setOnClickListener { onBackPressed() }
    }

    private fun redirectStore(storeLink: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                setPackage("com.android.vending")
            }
            startActivity(intent)
            //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeLink))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun exit() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }

    companion object {
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_PLAY_STORE_LINK = "play_store_link"
        const val KEY_IS_FORCE_UPDATE = "is_force_update"
    }
}