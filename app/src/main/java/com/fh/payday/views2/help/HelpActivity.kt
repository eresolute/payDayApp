package com.fh.payday.views2.help

import android.annotation.SuppressLint
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
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.shared.utils.OkHttpWebViewClient

class HelpActivity : BaseActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var linearLayout: LinearLayout
    override fun getLayout(): Int = R.layout.activity_help

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleBottomBar()
        val anchor = intent.getStringExtra("anchor") ?: ""
        loadHelpPage(anchor)
    }

    private fun loadHelpPage(anchor: String) {

        hideNoInternetView()
        if (!isNetworkConnected()) {
            progressBar.visibility = View.GONE
            linearLayout.visibility = View.GONE
            return showNoInternetView { loadHelpPage(anchor) }
        }
        loadWeb(anchor)
    }

    override fun init() {
        findViewById<TextView>(R.id.toolbar_title).apply {
            text = getString(R.string.help)
        }
        findViewById<View>(R.id.toolbar_help).visibility = View.INVISIBLE
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }

        progressBar = findViewById(R.id.progress_bar)
        webView = findViewById(R.id.web_view)
        linearLayout = findViewById(R.id.linear_layout)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWeb(anchor: String) {
        val locale = LocalePreferences.instance.getLocale(this)
        progressBar.visibility = View.VISIBLE
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("$BASE_URL/help?searchParam=$anchor&lang=$locale")

        webView.webViewClient = object : OkHttpWebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                linearLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                showNoInternetView { loadHelpPage(anchor) }
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

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { v ->
            val i = Intent(v.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener {
            val i = Intent(it.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener {
            val i = Intent(it.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }
    }
}
