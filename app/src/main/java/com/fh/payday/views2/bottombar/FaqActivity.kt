package com.fh.payday.views2.bottombar

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.preferences.LocalePreferences
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.views2.bottombar.location.BranchATMActivity
import com.fh.payday.views2.shared.utils.OkHttpWebViewClient
import java.util.*

class FaqActivity : BaseActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var linearLayout: LinearLayout
    override fun getLayout(): Int = R.layout.activity_faq

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFaqsPage()
    }

    private fun loadFaqsPage() {
        hideNoInternetView()
        if (!isNetworkConnected()) {
            progressBar.visibility = View.GONE
            linearLayout.visibility = View.GONE
            return showNoInternetView { loadFaqsPage() }
        }
        loadWeb()
    }

    override fun init() {
        findViewById<TextView>(R.id.toolbar_title).apply {
            text = getString(R.string.faq)
        }
        handleBottomBar()
        findViewById<View>(R.id.toolbar_help).visibility = View.INVISIBLE
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }

        progressBar = findViewById(R.id.progress_bar)
        webView = findViewById(R.id.web_view)
        linearLayout = findViewById(R.id.linear_layout)
    }

    private fun loadWeb() {
        progressBar.visibility = View.VISIBLE
        webView.settings.setAppCacheEnabled(true)
        val locale = Locale(LocalePreferences.instance.getLocale(this))
        webView.loadUrl("$BASE_URL/FAQs?lang=$locale")
        webView.webViewClient = object : OkHttpWebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                linearLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                showNoInternetView { loadFaqsPage() }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                linearLayout.visibility = View.GONE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                linearLayout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleBottomBar() {

        val locale = LocalePreferences.instance.getLocale(this)
        findViewById<TextView>(R.id.btm_menu_cash_out).setOnClickListener {
            val mIntent = Intent(this, BranchATMActivity::class.java)
            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mIntent.putExtra("issue", "branch")
            startActivity(mIntent)
        }

        findViewById<TextView>(R.id.btm_menu_currency_conv).setOnClickListener(this)

        findViewById<TextView>(R.id.btm_menu_how_to_reg).setOnClickListener(this)
    }
}