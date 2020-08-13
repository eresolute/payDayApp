package com.fh.payday.views2.shared.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fh.payday.BaseActivity
import com.fh.payday.R
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.bottombar.*
import kotlinx.android.synthetic.main.toolbar.*


class WebViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_title.text = intent.getStringExtra("title") ?: ""
        toolbar_back.setOnClickListener(this)
        toolbar_help.visibility = View.INVISIBLE

    }

    override fun getLayout() = R.layout.activity_web_view

    override fun init() {
        intent ?: finish()
        val title = intent.getStringExtra("title") ?: ""
        btm_menu_cash_out.setOnClickListener(this)
        btm_menu_faq.setOnClickListener(this)
        if (title == getString(R.string.explore))
            btm_menu_how_to_reg.setOnClickListener(this)
        if (title == getString(R.string.how_to_register))
            btm_menu_currency_conv.setOnClickListener(this)


        hideNoInternetView()
        if (!isNetworkConnected()) {
            progress_bar.visibility = View.GONE
            web_view.visibility = View.GONE
            return showNoInternetView { init() }
        }


        loadWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        val url = intent.getStringExtra("url")
        url ?: finish()
        web_view.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Log.e("WEB View", description)
            }
        }
        web_view.settings.javaScriptEnabled = true
        web_view.loadUrl(url)
    }


    /*abstract class OkHttpWebViewClient : WebViewClient() {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val default = super.shouldInterceptRequest(view, request)

            val okHttpClient = getOkHttpBuilder().build()
            val okHttpRequest = Request.Builder().url(request?.url.toString()).build()
            return try {
                val response = okHttpClient.newCall(okHttpRequest).execute()
                WebResourceResponse("","", response.body()?.byteStream())
            } catch (e: IOException) {
                default
            }
        }

        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            val default = super.shouldInterceptRequest(view, url)

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                val okHttpClient = getOkHttpBuilder().build()
                val okHttpRequest = Request.Builder().url(url ?: return default).build()
                return try {
                    val response = okHttpClient.newCall(okHttpRequest).execute()
                    WebResourceResponse("", "", response.body()?.byteStream())
                } catch (e: IOException) {
                    default
                }
            }

            return default
        }

        private fun buildCertPinner(): CertificatePinner {
            val pattern = "www.financehouse.ae"
            val pin1 = "sha256//bhL4VjycbKcxe0T8pUmGyDwtK66vpi5GzFEOh6cZDY="
            val pin2 = "sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY="
            return CertificatePinner.Builder()
                .add(pattern, pin1)
                .add(pattern, pin2)
                .build()
        }

        fun getOkHttpBuilder(): OkHttpClient.Builder  {
            val builder = OkHttpClient.Builder()
            builder.certificatePinner(buildCertPinner())
            return builder
        }
    }*/
}
