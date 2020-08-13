package com.fh.payday.views2.intlRemittance.termsandconditions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.UAEX_TERMS_CONDITIONS
import com.fh.payday.utilities.setTextWithUnderLine
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog

class UAEXchangeFragment : BaseFragment() {
    private lateinit var tvUAEXchange: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_uaexchange_terms_conditions, container, false)
        initView(view)

        return view
    }

    private fun initView(view: View) {
//        tvUAEXchange = view.findViewById(R.id.tv_uae_exchange)
//        tvUAEXchange.setTextWithUnderLine(tvUAEXchange.text.toString())
//        tvUAEXchange.setOnClickListener {
//            val activity = activity ?: return@setOnClickListener
//            if (!activity.isNetworkConnected()) {
//                activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
//                return@setOnClickListener
//            }
//            val termsAndCondDialog = TermsAndConditionsDialog.Builder()
//                    .setMessage(getString(R.string.uae_exchange_terms_conditions_link))
//                    .setMessageGravity(Gravity.CENTER)
//                    .setPositiveText(getString(R.string.yes))
//                    .setNegativeText(getString(R.string.cancel))
//                    .attachPositiveListener {
//                        if (URLUtil.isHttpUrl(UAEX_TERMS_CONDITIONS) || URLUtil.isHttpsUrl(UAEX_TERMS_CONDITIONS)) {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UAEX_TERMS_CONDITIONS))
//                            startActivity(intent)
//                        } else {
//                            return@attachPositiveListener
//                        }
//                    }
//                    .build()
//            val fm = activity.supportFragmentManager ?: return@setOnClickListener
//            termsAndCondDialog.apply {
//                isCancelable = false
//                show(fm, termsAndCondDialog.tag)
//            }
//        }
    }
}