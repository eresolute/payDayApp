package com.fh.payday.views2.registration

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.PUBLIC_KEY
import com.fh.payday.utilities.onTextChanged
import com.mukesh.OtpView

class CardPinFragment : Fragment() {

    private var activity: RegisterActivity? = null
    private lateinit var btnConfirm: View
    private lateinit var pinView: OtpView
    private lateinit var tvError: TextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as RegisterActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            tvError.visibility = View.GONE
            pinView.text = null

            val activity = activity ?: return
            activity.viewModel.isPaydayDetailSet = true

            pinView.requestFocus()
            val imgr = getActivity()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_card_pin, container, false)

        btnConfirm = view.findViewById(R.id.btn_confirm)
        pinView = view.findViewById(R.id.pin_view)
        tvError = view.findViewById(R.id.tv_error)

        pinView.setOtpCompletionListener {
            tvError.visibility = View.GONE

            val pin = pinView.text.toString()
            if (pin.isEmpty() || pin.length < 4) {
                tvError.visibility = View.VISIBLE
                tvError.requestFocus()
                return@setOtpCompletionListener
            }

            pinView.text = null
            createCustomer(pin)
        }

        pinView.onTextChanged { _, _, _, _ -> tvError.visibility = View.GONE }


        btnConfirm.setOnClickListener {
            val pin = pinView.text.toString()
            if (pin.isEmpty() || pin.length < 4) {
                tvError.visibility = View.VISIBLE
                tvError.requestFocus()
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            createCustomer(pin)
        }

        attachObservers()

        return view
    }

    private fun createCustomer(pin: String) {
        activity?.let {
            (it as BaseActivity?)?.hideKeyboard()
            val inputWrapper = it.viewModel.inputWrapper
            val (_, _, emiratesId, cardNumber, cardName, cardExpiry) = inputWrapper
            val eId = emiratesId?.id

            if (cardNumber.isNullOrEmpty() || cardName.isNullOrEmpty() || cardExpiry.isNullOrEmpty()
                || eId.isNullOrEmpty() || pin.isEmpty()) return

            val keyBytes = it.assets.open(PUBLIC_KEY).use { stream ->
                stream.readBytes()
            }

            if (!it.isNetworkConnected()) {
                return@let it.onFailure(it.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
            }

            it.viewModel.createCustomer(keyBytes, cardNumber, cardName, cardExpiry, pin, eId)
        }
    }

    private fun attachObservers() {
        activity?.apply {
            viewModel.createCustomerState.observe(this, Observer {
                it ?: return@Observer

                val state = it.getContentIfNotHandled() ?: return@Observer

                if (state is NetworkState2.Loading<*>) {
                    showProgress(getString(R.string.processing))
                    btnConfirm.isEnabled = false
                    return@Observer
                }

                hideProgress()
                btnConfirm.isEnabled = true
                pinView.text = null

                when (state) {
                    is NetworkState2.Success<*> -> navigateUp()
                    is NetworkState2.Error<*> -> handleErrorResponse(state.message, state.errorCode)
                    is NetworkState2.Failure<*> -> onFailure(findViewById(R.id.root_view), state.throwable)
                    else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            })
        }
    }
}
