package com.fh.payday.views2.kyc.update

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.viewmodels.kyc.KycViewModel
import com.fh.payday.views2.registration.ScanEmiratesFragment
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class KycOptionActivity : BaseActivity() {

    val REQUEST_PHOTO_PASSPORT_CARD = 1
    val REQUEST_SIGNATURE_PASSPORT_CARD = 2
    val REQUEST_ADDRESS_PASSPORT_CARD = 3
    val REQUEST_SCAN_EMIRATES = 4
    val REQUEST_CAPTURE_EMIRATES_FRONT = 5
    val REQUEST_CAPTURE_EMIRATES_BACK = 6

    lateinit var kycViewModel: KycViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kycViewModel = ViewModelProviders.of(this).get(KycViewModel::class.java)

        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        val index = intent.getIntExtra("index",0)
        changeView(index)
    }

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }
    }

    override fun getLayout(): Int {
        return R.layout.activity_kyc_option
    }

    fun setToolbarText (title: String) {
        toolbar_title.text = title
    }

    fun getViewModel() : KycViewModel {
        return kycViewModel
    }


    fun onHandleCapture() {
        val fragment = ScanEmiratesFragment()
        val bundle = Bundle()
        bundle.putSerializable("screen", ScanEmiratesFragment.Screen.CAPTURE_BACK)
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    private fun changeView (id: Int){
        when (id) {
            0 -> {
                setToolbarText(getString(R.string.update_email_id))
                val fragment = UpdateEmailFragment()
                val bundle = Bundle()
                bundle.putSerializable("title", UpdateEmailFragment.Type.DEFAULT)
                fragment.arguments = bundle
                showFragment(fragment)
            }

            1 -> {
                setToolbarText(getString(R.string.update_mobile_no))
                val fragment = UpdateMobileNumberFragment()
                val bundle = Bundle()
                bundle.putSerializable("title", UpdateMobileNumberFragment.Type.DEFAULT)
                fragment.arguments = bundle
                showFragment(fragment)
            }

            2 -> {
                setToolbarText(getString(R.string.update_passport))
                showFragment(UpdatePassportFragment())
            }

            3 -> {
                setToolbarText(getString(R.string.update_emirates))
                val fragment = ScanEmiratesFragment()
                val bundle = Bundle()
                bundle.putSerializable("screen", ScanEmiratesFragment.Screen.CAPTURE_FRONT)
                fragment.arguments = bundle
                showFragment(fragment)
            }
        }
    }


    fun showFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(fragment.javaClass.name)
                .commit()
    }
}
