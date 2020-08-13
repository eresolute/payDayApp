package com.fh.payday.utilities

import com.fh.payday.BaseActivity
import com.fh.payday.views2.auth.LoginActivity
import com.fh.payday.views2.auth.forgotcredentials.ForgotCredentailsActivity
import com.fh.payday.views2.auth.forgotcredentials.ForgotCredentialsMainActivity
import com.fh.payday.views2.auth.forgotcredentials.loancustomer.LoanCustomerActivity
import com.fh.payday.views2.bottombar.FaqActivity
import com.fh.payday.views2.bottombar.HowToRegisterActivity
import com.fh.payday.views2.bottombar.location.BranchATMActivity
import com.fh.payday.views2.bottombar.location.LocationActivity
import com.fh.payday.views2.intro.IntroActivity
import com.fh.payday.views2.intro.SplashActivity
import com.fh.payday.views2.registration.AppUpdateAvailableActivity
import com.fh.payday.views2.registration.RegisterActivity
import com.fh.payday.views2.shared.activity.WebViewActivity

fun isOnBoardingActivity(a: BaseActivity): Boolean {
    return a is SplashActivity || a is IntroActivity
        || a is LoginActivity || a is RegisterActivity || a is AppUpdateAvailableActivity
        || a is HowToRegisterActivity || a is LocationActivity || a is BranchATMActivity
        || a is ForgotCredentialsMainActivity || a is ForgotCredentailsActivity
        || a is LoanCustomerActivity || a is FaqActivity ||  a is WebViewActivity
}