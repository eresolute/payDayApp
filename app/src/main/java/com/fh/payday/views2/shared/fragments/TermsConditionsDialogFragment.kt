package com.fh.payday.views2.shared.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BuildConfig
import com.fh.payday.R
import com.fh.payday.views2.shared.utils.OkHttpWebViewClient


class TermsConditionsDialogFragment : DialogFragment() {

    private lateinit var inflater: LayoutInflater
    private lateinit var builder: AlertDialog.Builder
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    companion object {
        private lateinit var mBaseUrl: String
        private lateinit var mTitle: String

        fun newInstance(url: String): TermsConditionsDialogFragment {
            mBaseUrl = url
            return TermsConditionsDialogFragment()
        }

        fun newInstance(url: String, title: String): TermsConditionsDialogFragment {
            mBaseUrl = url
            mTitle = title
            return TermsConditionsDialogFragment()
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        inflater = activity!!.layoutInflater

        val view = inflater.inflate(R.layout.dialog_layout_terms_conditions, null)
        initView(view)

        tvError.text = getString(R.string.no_internet)
        webView.webViewClient = MyWebViewClient()

        if (isNetworkConnected()) {
            webView.loadUrl(mBaseUrl)
        } else {
            progressBar.visibility = View.GONE
            tvError.visibility = View.VISIBLE
        }

        builder = AlertDialog.Builder(activity!!)
        builder.setView(view).setCancelable(true)
        val dialog = builder.create()

        view.findViewById<TextView>(R.id.text_view).apply {
            text = mTitle
            setOnClickListener {
                dialog.dismiss()
            }
        }

        if (!BuildConfig.DEBUG)
            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun initView(view: View) {
        progressBar = view.findViewById(R.id.progress_bar)
        webView = view.findViewById(R.id.web_view)
        tvError = view.findViewById(R.id.tv_error)
    }


    private fun isNetworkConnected(): Boolean {
        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return cm.activeNetworkInfo != null
    }

    inner class MyWebViewClient : OkHttpWebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            showProgress()
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            hideProgress()
            super.onPageFinished(view, url)
        }

        fun showProgress() {
            webView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }

        fun hideProgress() {
            webView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}