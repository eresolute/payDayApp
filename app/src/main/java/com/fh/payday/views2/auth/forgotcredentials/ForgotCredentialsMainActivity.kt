package com.fh.payday.views2.auth.forgotcredentials

import android.content.Intent
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.utilities.setTextWithUnderLine
import com.fh.payday.views2.auth.forgotcredentials.loancustomer.LoanCustomerActivity
import kotlinx.android.synthetic.main.activity_forgot_login_credential.*
import kotlinx.android.synthetic.main.bottombar.*
import kotlinx.android.synthetic.main.toolbar.*

class ForgotCredentialsMainActivity : BaseActivity() {
    private var userIdStateChange: Int = 0
    private var passwordStateChange: Int = 0

    override fun getLayout(): Int {
        return R.layout.activity_forgot_login_credential
    }

    override fun init() {

        handleBottomBarListeners()
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.setText(R.string.forgot_user_id_password)
        toolbar_help.visibility = View.INVISIBLE

        linear_layout_forgot_userId.setOnClickListener {
            if (!isNetworkConnected()) {
                onFailure(findViewById<View>(R.id.root_view), getString(R.string.no_internet_connectivity))
            } else {
                if (userIdStateChange == 0) {
                    ll_forgot_userId_child.visibility = View.VISIBLE
                    //ll_forgot_password_child.visibility = View.GONE
                    userIdStateChange = 1
                } else {
                    userIdStateChange = 0
                    ll_forgot_userId_child.visibility = View.GONE
                }
            }
        }
        linear_layout_forgot_password.setOnClickListener {
            if (!isNetworkConnected()) {
                onFailure(findViewById<View>(R.id.root_view), getString(R.string.no_internet_connectivity))
            } else {
                if (passwordStateChange == 0) {
                    //ll_forgot_userId_child.visibility = View.GONE
                    ll_forgot_password_child.visibility = View.VISIBLE
                    passwordStateChange = 1
                } else {
                    passwordStateChange = 0
                    ll_forgot_password_child.visibility = View.GONE
                }
            }
        }
        handleClick()
    }

    private fun handleClick() {

        tv_forgotUserId_paydayCustomer.setTextWithUnderLine(getString(R.string.payday_customer))
        tv_forgotPassword_paydayCustomer.setTextWithUnderLine(getString(R.string.payday_customer))

        tv_forgotUserId_loanCustomer.setTextWithUnderLine(getString(R.string.loan_customer))
        tv_forgotPassword_loanCustomer.setTextWithUnderLine(getString(R.string.loan_customer))

        tv_forgotUserId_paydayCustomer.setOnClickListener {
            if (!isNetworkConnected()) {
                onFailure(findViewById<View>(R.id.root_view), getString(R.string.no_internet_connectivity))
                return@setOnClickListener
            }
            val intent = Intent(this, ForgotCredentailsActivity::class.java)
            intent.putExtra("forgotType", "forgotUserID")
            startActivity(intent)
        }

        tv_forgotPassword_paydayCustomer.setOnClickListener {
            if (!isNetworkConnected()) {
                onFailure(findViewById<View>(R.id.root_view), getString(R.string.no_internet_connectivity))
                return@setOnClickListener
            }
            val intent = Intent(this, ForgotCredentailsActivity::class.java)
            intent.putExtra("forgotType", "forgotPassword")
            startActivity(intent)
        }

        tv_forgotUserId_loanCustomer.setOnClickListener {
            if (!isNetworkConnected()) {
                onFailure(findViewById<View>(R.id.root_view), getString(R.string.no_internet_connectivity))
                return@setOnClickListener
            }
            val intent = Intent(this, LoanCustomerActivity::class.java)
            intent.putExtra("forgotType", "forgotUserID")
            startActivity(intent)
        }
        tv_forgotPassword_loanCustomer.setOnClickListener {
            if (!isNetworkConnected()) {
                onFailure(findViewById<View>(R.id.root_view), getString(R.string.no_internet_connectivity))
                return@setOnClickListener
            }
            val intent = Intent(this, LoanCustomerActivity::class.java)
            intent.putExtra("forgotType", "forgotPassword")
            startActivity(intent)
        }
    }

    private fun handleBottomBarListeners() {

        btm_menu_cash_out.setOnClickListener(this)
        btm_menu_currency_conv.setOnClickListener(this)
        btm_menu_faq.setOnClickListener(this)
        btm_menu_how_to_reg.setOnClickListener(this)
    }
}
