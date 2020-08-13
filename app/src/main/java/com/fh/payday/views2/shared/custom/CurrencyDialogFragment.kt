package com.fh.payday.views2.shared.custom


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.azoft.carousellayoutmanager.CarouselLayoutManager
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.azoft.carousellayoutmanager.CenterScrollListener
import com.fh.payday.BaseActivity
import com.fh.payday.BuildConfig
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.CountryCurrency
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views2.intlRemittance.rateCalculator.SelectCurrencyAdapter2
import java.util.*

/**
 * A simple [Fragment] subclass.
 */

@SuppressLint("InflateParams")
fun showCurrencies(activity: BaseActivity, list: ArrayList<CountryCurrency>, listener: OnItemClickListener): Dialog {
    val view = LayoutInflater.from(activity).inflate(R.layout.fragment_currency_dialog, null, false)
    val rvCountryList = view?.findViewById<RecyclerView>(R.id.rvCountries)

    rvCountryList?.adapter = SelectCurrencyAdapter2(list, listener)
    val layoutManager = CarouselLayoutManager(CarouselLayoutManager.VERTICAL)
    layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())
    rvCountryList?.setHasFixedSize(true)
    rvCountryList?.addOnScrollListener(CenterScrollListener())
    rvCountryList?.layoutManager = layoutManager

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(view)
    dialog.setCanceledOnTouchOutside(false)

    val lp = WindowManager.LayoutParams()
    val window = dialog.window
    if (window != null) {
        lp.copyFrom(window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    if (!BuildConfig.DEBUG)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

    dialog.show()
    return dialog
}

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val inflater = activity?.layoutInflater
//
//        val builder = AlertDialog.Builder(activity!!)
//
//        builder.setView(view).setCancelable(false)
//        val dialog = builder.create()
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        return dialog
//    }

