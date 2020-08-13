package com.fh.payday.views2.auth.forgotcredentials.loancustomer

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.utilities.setErrorMessage

class UserIdFragment : Fragment() {
    private lateinit var textInputLayoutUserId: TextInputLayout
    private lateinit var etUserId: TextInputEditText


    private var activity = LoanCustomerActivity()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as LoanCustomerActivity
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            etUserId.text = null
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_id, container, false)

        textInputLayoutUserId = view.findViewById(R.id.textInputLayout_user_id)
        etUserId = view.findViewById(R.id.et_user_id)
        addjustListiner(etUserId, textInputLayoutUserId)
        addObservers()
        view.findViewById<MaterialButton>(R.id.btn_login).setOnClickListener {
            if (validateEditText(etUserId.text.toString())) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                val customerId = activity.getViewModel().customerId ?: return@setOnClickListener
                activity.getViewModel().getUserName(customerId,etUserId.text.toString())
            }
        }
        return view
    }

    private fun addObservers() {
        activity.getViewModel().userNameState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading<*>) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            activity.hideProgress()

            when (state) {
                is NetworkState2.Success<*> ->  activity.onUserIdExist()
                is NetworkState2.Error<*> -> {
                    val (message) = state
                    activity.onError(message)
                }
                is NetworkState2.Failure<*> -> {
                    //val (throwable) = state
                    activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                }
                else -> activity.onFailure(activity.findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addjustListiner(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInputLayout.clearErrorMessage()
            }
        })
    }

    private fun validateEditText(userId: String): Boolean {

        return if (userId.isBlank() || TextUtils.isEmpty(userId)) {
            textInputLayoutUserId.setErrorMessage(getString(R.string.empty_user_id))
            false
        } else if (userId.trim().length < 8) {
            textInputLayoutUserId.setErrorMessage(getString(R.string.invalid_username))
            false
        } else {
            textInputLayoutUserId.clearErrorMessage()
            true
        }
    }
}