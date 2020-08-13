package com.fh.payday.views2.shared.fragments

import android.content.Context
import android.support.v4.app.DialogFragment
import android.view.WindowManager
import com.fh.payday.BaseActivity
import com.fh.payday.BuildConfig


abstract class BaseDialog : DialogFragment() {

    private var mActivity: BaseActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BaseActivity) {
            mActivity = context
        }
    }

    override fun onResume() {
        mActivity?.hideNavigationBar()
        if (!BuildConfig.DEBUG)
            mActivity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onResume()
    }

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }

}