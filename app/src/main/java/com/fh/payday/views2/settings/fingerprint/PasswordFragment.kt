package com.fh.payday.views2.settings.fingerprint

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.AppPreferences
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.utilities.setErrorMessage
import com.fh.payday.utilities.token
import com.fh.payday.viewmodels.LoginViewModel
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_password.*

@RequiresApi(Build.VERSION_CODES.M)
class PasswordFragment : BaseFragment() {

    private val viewModel:  LoginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_confirm.setOnClickListener {
            activity?.hideKeyboard()
            val password = et_password.text?.toString()
            val userId = UserPreferences.instance.getUser(
                    activity ?: return@setOnClickListener
            )?.username ?: return@setOnClickListener

            if (password.isNullOrEmpty() || password.length < 8) {
                textInputLayout.setErrorMessage(getString(R.string.invalid_password))
                return@setOnClickListener
            }

            if (activity != null) {
                if (!activity!!.isNetworkConnected()) {
                    activity!!.onFailure(activity!!.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
            }

            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@addOnCompleteListener
                        }

                        // Get new Instance ID token
                        task.result?.apply {
                            viewModel.login(userId, password, token)
                        }
                    }
        }

        et_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout.clearErrorMessage()
            }
        })

        attachObservers()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun attachObservers() {
        viewModel.loginState.observe(this, Observer {
            it ?: return@Observer
            val activity = activity ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading<*>) {
                btn_confirm.isEnabled = false
                return@Observer activity.showProgress(getString(R.string.processing))
            }

            btn_confirm.isEnabled = true
            activity.hideProgress()

            if (state is NetworkState2.Success<*>) {
                val user = (state as NetworkState2.Success<User>).data
                user?.let {
                    ApiClient.clearInstance()
                    token = user.token
                    UserPreferences.instance.saveUser(activity, user)
                    AppPreferences.instance.cacheUserId(activity, user.username)
                }

                if (activity is FingerprintSettings) {
                    val userId = user?.username ?: return@Observer
                    val password = et_password.text?.toString() ?: return@Observer
                    et_password.setText(null)
                    (activity as FingerprintSettings?)?.onAuthenticated(userId, password)
                }
            } else if (state is NetworkState2.Error<*>) {
                val error = state as NetworkState2.Error<*>
                if (error.errorCode == "201"){
                    activity.onError(getErrorMessage(error.message).replace("user ID or",""))
                    return@Observer
                }
                activity.onError(error.message.replace("user ID or ",""))
            } else if (state is NetworkState2.Failure<*>) {
                activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun getErrorMessage(message: String?) : String {
        val messageArray = message?.split(".") ?: return ""
        return if (messageArray.isNotEmpty())
            String.format(getString(R.string.invalid_credentials), messageArray[1])
        else
            message
    }
}
