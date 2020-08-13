package com.fh.payday

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.view.WindowManager
import com.fh.payday.views2.shared.custom.AlertDialogFragment


open class BaseFragment : Fragment() {
    var activity: BaseActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        context ?: return
        activity = context as BaseActivity
    }

    override fun onResume() {
        activity?.hideNavigationBar()
        if (!BuildConfig.DEBUG)
            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onResume()
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }
}