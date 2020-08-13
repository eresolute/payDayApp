package com.fh.payday

import android.app.NotificationManager
import android.content.*
import android.content.res.Resources
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.auth.AuthService
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.preferences.AppPreferences
import com.fh.payday.preferences.BiometricPreferences
import com.fh.payday.preferences.LocalePreferences
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.services.AppUpdater.AppUpdateListener
import com.fh.payday.services.AppUpdater.Companion.with
import com.fh.payday.services.InteractionObserver
import com.fh.payday.services.OfferReceiver
import com.fh.payday.utilities.*
import com.fh.payday.utilities.ContextWrapper
import com.fh.payday.views2.auth.LoginActivity
import com.fh.payday.views2.bottombar.FaqActivity
import com.fh.payday.views2.bottombar.location.BranchATMActivity
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.intro.IntroActivity
import com.fh.payday.views2.intro.SplashActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.registration.AppUpdateAvailableActivity
import com.fh.payday.views2.shared.activity.WebViewActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.no_internet_connectivity.*
import java.net.SocketTimeoutException
import java.util.*

/**
 * PayDayFH
 * Created by EResolute on 8/28/2018.
 */
abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    private var progress: ProgressDialogFragment? = null

    private object ProgressDismissListener : ProgressDialogFragment.OnDismissListener {
        override fun onDismiss(dialog: DialogInterface?) {
            dialog?.dismiss()
        }
    }

    val alertDismissListener = AlertDialogFragment.OnDismissListener { it.dismiss() }
    val alertCancelListener = AlertDialogFragment.OnCancelListener { it.dismiss() }
    private val alertConfirmListener = AlertDialogFragment.OnConfirmListener { it.dismiss() }

    @DrawableRes
    val errorIcon = R.drawable.ic_error
    @DrawableRes
    val successIcon = R.drawable.ic_success_checked

    @ColorRes
    val tintColorError = R.color.color_red
    @ColorRes
    val tintColorSuccess = R.color.blue

    private val sessionErrorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val message = intent.getStringExtra("message") ?: return
            val isSessionExpired = intent.getBooleanExtra("isSessionExpired", false)

            if (isSessionExpired) {
                return this@BaseActivity.onSessionExpired(message)
            }

            this@BaseActivity.onError(message)
        }
    }
    private val offerReceiver = OfferReceiver()

    override fun attachBaseContext(newBase: Context?) {
        val context = when {
            newBase != null -> {
                val newLocale = Locale(LocalePreferences.instance.getLocale(newBase))
                ContextWrapper.wrap(newBase, newLocale)
            }
            else -> newBase
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG)
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(getLayout())
        setupFocusOutside(findViewById(android.R.id.content))
        init()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(offerReceiver, IntentFilter(OfferReceiver.OFFER_NOTIFICATION))
    }

    override fun onResume() {
        hideNavigationBar()
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(sessionErrorReceiver, IntentFilter(Action.ACTION_SESSION_ERROR))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sessionErrorReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(offerReceiver)
        super.onDestroy()
    }

    fun hideNavigationBar() {
        if (hasSoftNavbar(resources) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.apply {
                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
    }

    open fun setOnFocusListener(etMobileNumber: TextInputEditText, constraintLayout: ConstraintLayout) {

        etMobileNumber.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                constraintLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_blue_rounded_border, null)
            else
                constraintLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.edit_text_outline_color, null)
        }
    }

    @LayoutRes
    abstract fun getLayout(): Int

    abstract fun init()

    @JvmOverloads
    open fun showProgress(message: String = "", cancellable: Boolean = false,
                          dismissListener: ProgressDialogFragment.OnDismissListener = ProgressDismissListener) {
        if (progress != null && progress?.isVisible!!) return

        progress = ProgressDialogFragment.Builder(dismissListener)
            .setTitle(message)
            .setCancelable(cancellable)
            .build()

        progress?.show(supportFragmentManager, "progress-dialog")
    }

    open fun hideProgress() {
        progress?.dismiss()
    }

    open fun onFailure(view: View, t: Throwable?) {
        t?.message ?: return

        val message = when (t) {
            is SocketTimeoutException -> LIMITED_CONNECTIVITY
            else -> getString(R.string.request_error)
        }
        onFailure(view, message)
    }

    open fun onFailure(view: View, message: CharSequence) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorError))
        val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    open fun onFailure(message: CharSequence) {
        val view = findViewById<ViewGroup>(R.id.root_view)
        if (view != null) {
            onFailure(view, message)
        }
    }

    @JvmOverloads
    open fun onError(
        message: String,
        cancellable: Boolean = false,
        @DrawableRes icon: Int = errorIcon,
        @ColorRes tintColor: Int = tintColorError,
        dismissListener: AlertDialogFragment.OnDismissListener = alertDismissListener,
        cancelListener: AlertDialogFragment.OnCancelListener = alertCancelListener,
        confirmListener: AlertDialogFragment.OnConfirmListener = alertConfirmListener,
        @StringRes btnTextRes: Int = R.string.ok
    ) {
        AlertDialogFragment.Builder()
            .setMessage(message)
            .setIcon(icon)
            .setTintColor(tintColor)
            .setCancelable(cancellable)
            .setCancelListener(cancelListener)
            .setConfirmListener(confirmListener)
            .setConfirmListener(confirmListener)
            .setButtonText(btnTextRes)
            .build()
            .show(supportFragmentManager, "")
    }

    @JvmOverloads
    open fun showMessage(
        message: String,
        @DrawableRes icon: Int = successIcon,
        @ColorRes tintColor: Int = tintColorSuccess,
        confirmListener: AlertDialogFragment.OnConfirmListener = alertConfirmListener,
        dismissListener: AlertDialogFragment.OnDismissListener = alertDismissListener,
        cancelListener: AlertDialogFragment.OnCancelListener = alertCancelListener,
        cancellable: Boolean = false
    ) {
        AlertDialogFragment.Builder()
            .setMessage(message)
            .setIcon(icon)
            .setTintColor(tintColor)
            .setCancelListener(cancelListener)
            .setConfirmListener(confirmListener)
            .setConfirmListener(confirmListener)
            .setCancelable(cancellable)
            .build()
            .show(supportFragmentManager, "")
    }

    @JvmOverloads
    open fun showWarning(
        message: String,
        @DrawableRes icon: Int = R.drawable.ic_warning,
        @ColorRes tintColor: Int = tintColorSuccess,
        confirmListener: AlertDialogFragment.OnConfirmListener = alertConfirmListener,
        dismissListener: AlertDialogFragment.OnDismissListener = alertDismissListener,
        cancelListener: AlertDialogFragment.OnCancelListener = alertCancelListener,
        cancellable: Boolean = false
    ) {
        AlertDialogFragment.Builder()
            .setMessage(message)
            .setIcon(icon)
            .setBackground(R.drawable.bottom_orange_rounded)
            .setCancelListener(cancelListener)
            .setConfirmListener(confirmListener)
            .setConfirmListener(confirmListener)
            .setCancelable(cancellable)
            .build()
            .show(supportFragmentManager, "")
    }

    open fun showMessage(view: View, message: String) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    fun logout() {
        with(UserPreferences.instance) {
            getUser(this@BaseActivity)?.let {
                AuthService.instance.logout(
                    it.token, it.sessionId, it.refreshToken, it.customerId.toLong()
                )
                CustomerService.getInstance(it.token, it.sessionId, it.refreshToken).clearDisposable()
            }

            clearPreferences(this@BaseActivity)
        }
        ApiClient.clearInstance()
        startLoginActivity()
    }

    open fun startHelpActivity(anchor: String) {
        startActivity(Intent(this, HelpActivity::class.java).putExtra("anchor", anchor))
    }

    open fun startLoginActivity() {
        val intent = Intent(Intent(this, LoginActivity::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    fun logoutToLoginActivity() {
        with(UserPreferences.instance) {
            getUser(this@BaseActivity)?.let {
                AuthService.instance.logout(
                    it.token, it.sessionId, it.refreshToken, it.customerId.toLong()
                )
                CustomerService.getInstance(it.token, it.sessionId, it.refreshToken).clearDisposable()
            }
            clearPreferences(this@BaseActivity)
        }
        AppPreferences.instance.clearPreferences(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricPreferences.instance.clearPreferences(this)
        }
        ApiClient.clearInstance()
        startLoginActivity()
    }

    open fun startIntroActivity() {
        val intent = Intent(Intent(this, IntroActivity::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    open fun onSessionExpired(message: String? = null) {
        if (message == null) {
            return logout()
        }

        val dismissListener = AlertDialogFragment.OnDismissListener { it.dismiss(); logout() }
        val cancelListener = AlertDialogFragment.OnCancelListener { it.dismiss(); logout() }
        val confirmListener = AlertDialogFragment.OnConfirmListener { it.dismiss(); logout() }

        showWarning(message, confirmListener = confirmListener, cancelListener = cancelListener,
            dismissListener = dismissListener)
    }

    private fun hasSoftNavbar(resources: Resources): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    fun hideKeyboard() {
        val currentView = this.currentFocus
        if (currentView != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentView.windowToken, 0)
        }
    }

    fun showKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }
    }

    fun setupFocusOutside(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                false
            }
        }

        if (view is ViewGroup) {
            view.filterTouchesWhenObscured = true
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupFocusOutside(innerView)
            }
        }
    }

    override fun onClick(v: View?) {
        val view = v ?: return
        handleToolbar(view)
        handlePostLoginBottomBar(view)
        handlePreLoginBottomBar(view)
    }

    private fun handlePreLoginBottomBar(view: View) {
        val locale = LocalePreferences.instance.getLocale(this)
        when (view.id) {
            R.id.btm_menu_cash_out -> {
                val i = Intent(this, BranchATMActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                i.putExtra("issue", "branch")
                startActivity(i)
            }
            R.id.btm_menu_currency_conv -> {
                val exploreIntent = Intent(view.context, WebViewActivity :: class.java).apply {
                    putExtra("title", view.context.getString(R.string.explore))
                    when (locale) {
                        "en" -> putExtra("url", EXPLORE_URL_EN)
                        "hi" -> putExtra("url", EXPLORE_URL_HI)
                        else -> putExtra("url", EXPLORE_URL_EN)
                    }
                }
                startActivity(exploreIntent)
            }
            R.id.btm_menu_faq -> {
                val i = Intent(this, FaqActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
            R.id.btm_menu_how_to_reg -> {
                val faqIntent = Intent(view.context, WebViewActivity :: class.java).apply {
                    putExtra("title", view.context.getString(R.string.how_to_register))
                    when (locale) {
                        "en" -> putExtra("url", FAQ_URL_EN)
                        "hi" -> putExtra("url", FAQ_URL_HI)
                        else -> putExtra("url", FAQ_URL_EN)
                    }
                }
                startActivity(faqIntent)
            }
        }
    }


    private fun handlePostLoginBottomBar(view: View) {
        when (view.id) {
            R.id.btm_home -> {
                val i = Intent(view.context, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
            R.id.btm_menu_branch -> {
                val i = Intent(view.context, LocatorActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
            R.id.btm_menu_support -> {
                val i = Intent(view.context, ContactUsActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
            R.id.btm_menu_loan_calc -> {
                val i = Intent(view.context, LoanCalculatorActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (!isOnBoardingActivity(this)) {
            with(UserPreferences.instance.getUser(this)) {
                this ?: return@with
                InteractionObserver.status = true
            }
        }
    }

    private fun handleToolbar(view: View) {
        when (view.id) {
            R.id.toolbar_back -> {
                onBackPressed()
            }
            R.id.toolbar_help -> {
                startActivity(Intent(view.context, HelpActivity::class.java))
            }
        }
    }

    open fun isNetworkConnected(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return manager?.activeNetworkInfo?.isConnected ?: false
    }

    fun clearNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @JvmOverloads
    open fun showNoInternetView(
        message: String = getString(R.string.no_internet_connectivity),
        retryAction: () -> Unit
    ) {
        error_msg?.text = message
        btn_retry?.setOnClickListener { hideNoInternetView(); retryAction() }
        no_internet?.visibility = View.VISIBLE
    }

    open fun hideNoInternetView() {
        no_internet?.visibility = View.GONE
    }

    fun showCardStatusError(cardStatus: String?) {
        val status = if (cardStatus?.toLowerCase(Locale.ENGLISH) == STOP_LIST) {
            String.format(getString(R.string.card_status_stoplist), cardStatus.toLowerCase(Locale.ENGLISH))
        } else
            String.format(getString(R.string.card_status), cardStatus?.toLowerCase(Locale.ENGLISH)
                ?: "inactive")
        showMessage(
            status,
            R.drawable.ic_insufficient_fund_44,
            R.color.colorError,
            AlertDialogFragment.OnConfirmListener { it.dismiss() }
        )
    }

    fun handleErrorCode(errorCode: Int, message: String) {
        showMessage(message,
            R.drawable.ic_error,
            R.color.colorError,
            AlertDialogFragment.OnConfirmListener {
                it.dismiss()
                when (errorCode) {
                    850, 851 -> return@OnConfirmListener
                    else -> finish()
                }
            }
        )
        return
    }

    fun startUserInteractionObserver(user: User?, sessionTimeout: Long) {
        if (user == null) return

        InteractionObserver.start(user, sessionTimeout, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {}

            override fun onError(message: String) {
                sendBroadcast(message, false)
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                sendBroadcast(message, isSessionExpired)
            }

            override fun onSuccess(data: String) {}

            private fun sendBroadcast(message: String, isSessionExpired: Boolean) {
                val intent = Intent(Action.ACTION_SESSION_ERROR)
                intent.putExtra("message", message)
                intent.putExtra("isSessionExpired", isSessionExpired)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        })
    }

    fun isGpsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
            }

            return false
        }

        return true
    }

    fun setLocale(locale: String) {
        LocalePreferences.instance.setLocale(this, locale)
        recreate()
    }

    fun checkAppUpdate() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1)
            .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task: Task<Boolean?>? ->
            if (task?.isSuccessful == true) {
                val updateListener = object : AppUpdateListener {
                    override fun onUpdateAvailable(storeLink: String, versionName: String, isForce: Boolean) {
                        onAppUpdateAvailable(storeLink, versionName, isForce)
                    }
                }
                with(updateListener).check()
            }
        }
    }

    private fun onAppUpdateAvailable(storeLink: String, versionName: String, isForceUpdate: Boolean) {
        if (AppPreferences.instance.lastAppUpdateFlag(this) && !isForceUpdate) {
            return
        }

        startActivity(Intent(this, AppUpdateAvailableActivity::class.java)
            .putExtra(AppUpdateAvailableActivity.KEY_VERSION_NAME, versionName)
            .putExtra(AppUpdateAvailableActivity.KEY_PLAY_STORE_LINK, storeLink)
            .putExtra(AppUpdateAvailableActivity.KEY_IS_FORCE_UPDATE, isForceUpdate))
    }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 876
    }
}