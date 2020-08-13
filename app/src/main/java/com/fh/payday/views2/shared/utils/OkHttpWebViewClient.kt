package com.fh.payday.views2.shared.utils

import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fh.payday.datasource.remote.getOkHttpBuilder
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.PRODUCTION_URL
import okhttp3.Request
import java.io.IOException

abstract class OkHttpWebViewClient : WebViewClient() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val default = super.shouldInterceptRequest(view, request)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && BASE_URL in PRODUCTION_URL) {
            return default
        }

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
}