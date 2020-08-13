package com.fh.payday.views2.payments.ibp

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.IndianState
import com.fh.payday.datasource.models.payments.CountryCode
import com.fh.payday.datasource.models.payments.PlanType
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnStateSelectListener
import com.fh.payday.views2.shared.custom.showIndianStates
import kotlinx.android.synthetic.main.fragment_select_state.*

class SelectStateFragment : Fragment() {

    private var activity: BaseActivity? = null
    private var indianStates: List<IndianState>? = null
    private var mStateAlias: String? = null
    private var mAccountNo: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as BaseActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    private fun getViewModel() = when (activity) {
        is IndianBillPaymentActivity -> (activity as IndianBillPaymentActivity?)?.viewModel
        else -> null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            getIndianStates().apply { attachIndStateObserver() }
        }
        val viewModel = getViewModel() ?: return
        if (!isVisibleToUser) {
            clearStateView()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachObserver()
        btn_next.setOnClickListener {

            val viewModel = getViewModel() ?: return@setOnClickListener
            val user = viewModel.user ?: return@setOnClickListener
            val (_, accessKey, _, isFlexAvailable) = viewModel.selectedOperator
                    ?: return@setOnClickListener
            val planType = if (isFlexAvailable == 1) PlanType.FLEXI else PlanType.FIXED

            viewModel.data.observe(this, Observer {
                mAccountNo = viewModel.data.value?.get("account_no") ?: return@Observer
            })

            if (validateState()) {

                if (activity?.isNetworkConnected() == false) {
                    activity?.onFailure(activity?.findViewById(R.id.card_view)!!, getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                viewModel.setStateName(tv_state.text.toString())
                getViewModel()?.dataClear = true
                val stateAlias = if (!mStateAlias.isNullOrEmpty()) mStateAlias else return@setOnClickListener
                viewModel.operatorDetails(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                        mAccountNo ?: return@setOnClickListener, accessKey, viewModel.typeId
                        ?: return@setOnClickListener,
                        planType, CountryCode.INDIA, stateAlias ?: return@setOnClickListener)
            }
        }

        val clickListener = View.OnClickListener {
            loadIndianStates()
        }

        ll_state.setOnClickListener(clickListener)
        view.findViewById<View>(R.id.iv_state).setOnClickListener(clickListener)
    }

    private fun setStateView(text: String, stateAlias: String) {
        ll_state.background = ContextCompat.getDrawable(this.context ?: return,
                R.drawable.bg_grey_blue_border)
        tv_state.text = text
        mStateAlias = stateAlias


    }

    private fun setStateError(message: String) {
        ll_state.background = ContextCompat.getDrawable(this.context ?: return,
                R.drawable.bg_grey_red_border)
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }

    private fun clearStateView() {
        ll_state.background = ContextCompat.getDrawable(this.context ?: return,
                R.drawable.bg_grey_dark_grey_border)
        tv_state.text = ""
        mStateAlias = null
    }

    private fun getIndianStates() {

        val viewModel = getViewModel() ?: return
        val user = viewModel.user ?: return
        val accessKey = viewModel.selectedOperator?.accessKey ?: return
        viewModel.getIndianStates(user.token, user.sessionId,
                user.refreshToken, user.customerId.toLong(), "indianStates", accessKey)
    }

    private fun loadIndianStates() {

        if (!indianStates.isNullOrEmpty()) {
            showIndianStates(activity ?: return, indianStates
                    ?: return, object : OnStateSelectListener {
                override fun onStateSelect(stateName: IndianState) {
                    setStateView(stateName.stateName, stateName.stateAlias)
                    //getViewModel()?.dataClear = true
                }
            })
        }
    }

    private fun onSuccess(activity: FragmentActivity) {
        when (activity) {
            is IndianBillPaymentActivity -> activity.navigateUp()
        }
    }

    private fun attachIndStateObserver() {
        getViewModel()?.indianStates?.observe(this, Observer {
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> {
                    indianStates = state.data

                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun attachObserver() {

        getViewModel()?.operatorDetailFixedState?.observe(this, Observer {
            it ?: return@Observer
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(activity)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun validateState(): Boolean {
        return if (tv_state.text.isNullOrEmpty() && mStateAlias.isNullOrEmpty() && activity is IndianBillPaymentActivity) {
            setStateError(getString(R.string.choose_state))
            false
        } else {
            true
        }
    }
}