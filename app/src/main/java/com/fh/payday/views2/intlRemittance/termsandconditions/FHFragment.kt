package com.fh.payday.views2.intlRemittance.termsandconditions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.views2.shared.utils.OkHttpWebViewClient

class FHFragment : BaseFragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fh_terms_conditions, container, false)
        initView(view)

        val url = arguments?.getString("url") ?: ""
        loadWebPage(url)
        return view
    }

    private fun loadWebPage(url: String) {

        val activity = activity ?: return
        activity.hideNoInternetView()
        if (!activity.isNetworkConnected()) {
            constraintLayout.visibility = View.GONE
            return activity.showNoInternetView { loadWebPage(url) }
        }
        loadWeb(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWeb(url: String) {
        constraintLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)

        webView.webViewClient = object : OkHttpWebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                constraintLayout.visibility = View.GONE
                activity?.showNoInternetView { loadWebPage(url) }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                webView.visibility = View.GONE
                constraintLayout.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                webView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                constraintLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun initView(view: View) {
        webView = view.findViewById(R.id.web_view)
        progressBar = view.findViewById(R.id.progress_bar)
        constraintLayout = view.findViewById(R.id.parent_view)
    }
}