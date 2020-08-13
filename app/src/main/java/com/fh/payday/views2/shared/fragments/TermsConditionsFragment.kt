package com.fh.payday.views2.shared.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.BuildConfig
import com.fh.payday.R
import com.fh.payday.utilities.BASE_URL

class TermsConditionsFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var webView: WebView
    private lateinit var cbTerms: CheckBox
    private lateinit var btnAccept: View
    private var activity: BaseActivity? = null
    private var acceptanceListener: OnAcceptanceListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as BaseActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terms_conditions, container, false)
        init(view)
        if (!BuildConfig.DEBUG)
            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val title = arguments?.getString("title")
        val instructions = arguments?.getString("instructions")

        tvTitle.text = title ?: getString(R.string.terms_and_Conditions)

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl("$BASE_URL/termsConditions")

        btnAccept.setOnClickListener {
            if (!cbTerms.isChecked) return@setOnClickListener setAcceptanceError()
            acceptanceListener?.onAccepted()
        }

        return view
    }

    private fun init(view: View) {
        tvTitle = view.findViewById(R.id.tv_title)
        cbTerms = view.findViewById(R.id.cb_terms_conditions)
        btnAccept = view.findViewById(R.id.btn_accept)
        webView = view.findViewById(R.id.web_view)
    }

    private fun setAcceptanceError() {
        if (!cbTerms.isChecked) {
            val message = getString(R.string.terms_conditions_error)
            activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
        }
    }

    class Builder {
        private var mTitle: String? = null
        private var mTerms: String? = null
        private var mListener: OnAcceptanceListener? = null

        fun setTitle(text: String): Builder {
            mTitle = text
            return this
        }

        fun setInstructions(text: String): Builder {
            mTerms = text
            return this
        }

        fun attachListener(listener: OnAcceptanceListener): Builder {
            mListener = listener
            return this
        }

        fun build(): TermsConditionsFragment {
            val bundle = Bundle()
            val title = mTitle
            val instructions = mTerms
            if (title != null) bundle.putString("title", title)
            if (instructions != null) bundle.putString("instructions", instructions)

            val fragment = TermsConditionsFragment()
            fragment.arguments = bundle
            fragment.acceptanceListener = mListener

            return fragment
        }
    }

    interface OnAcceptanceListener {
        fun onAccepted()
    }
}
