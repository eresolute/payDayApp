package com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.moneytransfer.Beneficiary
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.utilities.TYPE
import com.fh.payday.viewmodels.beneficiaries.PaydayBeneficiaryViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class PaydayBeneficiaryActivity : BaseActivity() {
    private lateinit var type: TYPE
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: SliderPagerAdapter
    private lateinit var viewModelBeneficiary: PaydayBeneficiaryViewModel
    private lateinit var beneficiary: Beneficiary
    private var enable: Int = -1
    override fun getLayout(): Int = R.layout.activity_payday_beneficiary

    private val addOtpListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            val user = UserPreferences.instance.getUser(this@PaydayBeneficiaryActivity) ?: return
            val id = user.customerId.toLong()
            val token = user.token
            val sessionId = user.sessionId
            val refreshToken = user.refreshToken

            if (!isNetworkConnected()) {
                onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
            }

            val viewModel = viewModelBeneficiary
            val beneficiary = viewModel.selectedBeneficiary ?: return

            when(type) {
                TYPE.ADD -> {
                }
                TYPE.EDIT -> {
                    val beneficiaryName = viewModelBeneficiary.beneficiaryName ?: return
                    viewModelBeneficiary.editP2PBeneficiary(token, sessionId, refreshToken, id, beneficiary.beneficiaryId, beneficiaryName, otp)
                }
                TYPE.DELETE -> {
                    viewModelBeneficiary.deleteBeneficiary(token, sessionId, refreshToken, id, beneficiary.beneficiaryId.toLong(), otp)
                }
                TYPE.ENABLE -> {
                    if (enable == -1) return
                    val isEnabled = when (enable) {
                        0 -> false
                        1 -> true
                        else -> return
                    }
                    viewModelBeneficiary.changeBeneficiaryStatus(token, sessionId, refreshToken, id, beneficiary.beneficiaryId.toLong(), isEnabled, otp)
                }

            }
        }
    }

    override fun init() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        viewPager = findViewById(R.id.pagerPaydayBeneficiary)

        viewModelBeneficiary = ViewModelProviders.of(this).get(PaydayBeneficiaryViewModel::class.java)

        type = intent.getSerializableExtra("index") as TYPE
        beneficiary = intent.getParcelableExtra("beneficiary") ?: return
        enable = intent.getIntExtra("enable", -1)

        viewModelBeneficiary.selectedBeneficiary = beneficiary

        changeToolbarTitle(type)
        setupViewPager()
        handleBottomBar()
        setFragments(type)
        addObserver()
    }



    private fun handleBottomBar() {
        toolbar_help.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_back.setOnClickListener{
            onBackPressed()
        }
    }

    fun getViewModel() : PaydayBeneficiaryViewModel {
        return viewModelBeneficiary
    }

    private fun setupViewPager() {
        adapter = SliderPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }
            override fun onPageSelected(p0: Int) {
                when(p0) {

                    1 -> {
                        findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.verify_otp)
                    }
                    else -> {
                        changeToolbarTitle(type)
                    }
                }
            }
        })
    }

    private fun changeToolbarTitle(type: TYPE) {
        when(type) {
            TYPE.ADD -> {
                findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.add_beneficiary)
            }
            TYPE.EDIT -> {
                findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.edit_beneficiary)
            }
            TYPE.DELETE -> {
                findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.verify_otp)
            }
            TYPE.ENABLE -> {
                findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.verify_otp)
            }
        }
    }

    private fun setFragments(type: TYPE) {
        changeToolbarTitle(type)
        when(type) {
            TYPE.ADD -> {
                adapter.addFragments(addBeneficiaryFragments)
            }
            TYPE.EDIT -> {
                adapter.addFragments(addBeneficiaryFragments)
            }
            TYPE.DELETE -> {
                adapter.addFragments(deleteBeneficiaryFragments)
            }
            TYPE.ENABLE -> {
                adapter.addFragments(deleteBeneficiaryFragments)
            }
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun navigateUp() {
        val currentPage = viewPager.currentItem
        val nextPage = currentPage + 1
        if (nextPage < viewPager.adapter?.count ?: 0) {
            viewPager.currentItem = nextPage
        }
    }

    private fun addObserver() {
        viewModelBeneficiary.deleteBeneficiaryState.observe(this, Observer {
            it ?: return@Observer

            val response = it.getContentIfNotHandled() ?: return@Observer

            if (response is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()
            when(response) {
                is NetworkState2.Success -> {
                    showMessage(response.data!!,
                            R.drawable.ic_success_checked_blue,
                            R.color.blue,
                            AlertDialogFragment.OnConfirmListener {
                                finish()
                            })
                }
                is NetworkState2.Error -> {
                    val message = response.message
                    handleErrorCode(Integer.parseInt(response.errorCode), message)

//                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById<View>(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById<View>(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })

        viewModelBeneficiary.changeBeneficiaryState.observe(this, Observer {
            it ?: return@Observer
            val response = it.getContentIfNotHandled() ?: return@Observer
            if (response is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()
            when(response) {
                is NetworkState2.Success -> {
                    showMessage(response.data!!,
                            R.drawable.ic_success_checked_blue,
                            R.color.blue,
                            AlertDialogFragment.OnConfirmListener {
                                finish()
                            })
                }
                is NetworkState2.Error -> {
                    val message = response.message
                    handleErrorCode(Integer.parseInt(response.errorCode), message)

//                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById<View>(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById<View>(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })

        viewModelBeneficiary.editP2PBeneficiaryState.observe(this, Observer {
            it ?: return@Observer
            val response = it.getContentIfNotHandled() ?: return@Observer
            if (response is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()
            when(response) {
                is NetworkState2.Success -> {
                    showMessage(response.data!!,
                            R.drawable.ic_success_checked_blue,
                            R.color.blue,
                            AlertDialogFragment.OnConfirmListener {
                                finish()
                            })
                }
                is NetworkState2.Error -> {
                    val message = response.message

                    handleErrorCode(Integer.parseInt(response.errorCode), message)

//                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById<View>(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById<View>(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })
    }

    class SliderPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

        private val fragmentList = ArrayList<Fragment>()
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int = fragmentList.size

        fun addFragments(fragmentsList: List<Fragment>) {
            fragmentList.addAll(fragmentsList)
            notifyDataSetChanged()
        }
    }

    private val addBeneficiaryFragments by lazy {
        listOf(
            EditBeneficiaryNameFragment(),
            OTPFragment.Builder(addOtpListener)
                    .setPinLength(6)
                    .setTitle(getString(R.string.enter_otp))
                    .setHasCardView(false)
                    .setButtonTitle(getString(R.string.submit))
                    .build())
    }

    private val deleteBeneficiaryFragments by lazy {
        listOf(
            OTPFragment.Builder(addOtpListener)
                    .setPinLength(6)
                    .setTitle(getString(R.string.enter_otp))
                    .setHasCardView(false)
                    .setButtonTitle(getString(R.string.submit))
                    .build())
    }
}
