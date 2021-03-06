package com.fh.payday.views2.moneytransfer.beneificaries.local

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
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.utilities.STATUS_ACCOUNT_NOT_ACTIVE
import com.fh.payday.utilities.TYPE
import com.fh.payday.viewmodels.beneficiaries.LocalBeneficiaryViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.activity_local_beneficiary.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.Serializable

class LocalBeneficiaryActivity : BaseActivity() {

    private lateinit var type: Serializable
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: SliderPagerAdapter
    private lateinit var viewModelBeneficiary: LocalBeneficiaryViewModel
    private var enable: Int = -1

    override fun getLayout(): Int = R.layout.activity_local_beneficiary

    private val addOtpListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            val user = UserPreferences.instance.getUser(this@LocalBeneficiaryActivity) ?: return
            val id = user.customerId.toLong()

            if (!isNetworkConnected()) {
                onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
            }

            when(type) {
                TYPE.ADD -> {
                    val name = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.beneficiary_name)] ?: return
                    val accountNumber = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.account_number)] ?: return
                    val iban = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.iban_number)] ?: return
                    val bank = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.bank)] ?: return
                    val mobileNo = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.mobile_number)] ?: return

                    viewModelBeneficiary.addBeneficiary(user.token, user.sessionId, user.refreshToken, id, mobileNo, name, accountNumber, iban, bank, otp)
                }
                TYPE.EDIT -> {
                    val name = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.beneficiary_name)] ?: return
                    val accountNumber = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.account_number)] ?: return
                    val iban = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.iban_number)] ?: return
                    val bank = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.bank)] ?: return
                    val mobileNo = viewModelBeneficiary.localBeneficiaryMap[getString(R.string.mobile_number)] ?: return
                    val beneficiary = viewModelBeneficiary.selectedBeneficiary ?: return

                    viewModelBeneficiary.editBeneficiary(user.token, user.sessionId, user.refreshToken, id, beneficiary.beneficiaryId, mobileNo, name,
                            accountNumber, iban, bank, otp)

                }
                TYPE.DELETE -> {
                    val beneficiary = viewModelBeneficiary.selectedBeneficiary ?: return
                    viewModelBeneficiary.deleteBeneficiary(user.token, user.sessionId, user.refreshToken, id, beneficiary.beneficiaryId, otp)
                }
                TYPE.ENABLE -> {
                    val beneficiary = viewModelBeneficiary.selectedBeneficiary ?: return
                    if (enable == -1) return
                    viewModelBeneficiary.enableBeneficiary(user.token, user.sessionId, user.refreshToken, id, beneficiary.beneficiaryId, enable, otp)
                }

            }
        }
    }

    override fun init() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        viewModelBeneficiary = ViewModelProviders.of(this).get(LocalBeneficiaryViewModel::class.java)
        setupViewPager()
        type = intent.getSerializableExtra("index")
        enable = intent.getIntExtra("enable", -1)
        viewModelBeneficiary.selectedBeneficiary = intent.getParcelableExtra("beneficiary")  ?: null
        setFragments(type)
        handleBottomBar()


        addObserver()
    }

    override fun showNoInternetView(message: String, retryAction: () -> Unit) {
        pagerLocalBeneficiary.visibility = View.GONE
        super.showNoInternetView(message, retryAction)
    }

    override fun hideNoInternetView() {
        super.hideNoInternetView()
        pagerLocalBeneficiary.visibility = View.VISIBLE
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

    fun navigateUp() {
        val currentPage = viewPager.currentItem
        val nextPage = currentPage + 1
        if (nextPage < viewPager.adapter?.count ?: 0) {
            viewPager.currentItem = nextPage
        }
    }

    fun getViewModel() : LocalBeneficiaryViewModel {
        return viewModelBeneficiary
    }

    private fun setFragments(type: Serializable?) {
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

    private fun changeToolbarTitle(type: Serializable?) {
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

    private fun setupViewPager() {
        viewPager = findViewById(R.id.pagerLocalBeneficiary)
        adapter = SliderPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }
            override fun onPageSelected(p0: Int) {
                when(p0) {
                    2 -> {
                        findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.verify_otp)
                    }
                    else -> {
                        changeToolbarTitle(type)
                    }
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

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun addObserver(){
        viewModelBeneficiary.addBeneficiaryState.observe(this, Observer {
            it ?: return@Observer

            val response = it.getContentIfNotHandled() ?: return@Observer

            if (response is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()
            when(response) {
                is NetworkState2.Success -> {
                    showMessage(getString(R.string.add_beneficiary_success),
                            R.drawable.ic_success_checked_blue,
                            R.color.blue,
                            AlertDialogFragment.OnConfirmListener {
                                finish()
                            })
                    viewModelBeneficiary.selectedBeneficiary = null

                }
                is NetworkState2.Error -> {
                    if(response.isSessionExpired)
                        return@Observer onSessionExpired(response.message)
                    val message = response.message

                    if (response.errorCode.toInt() == STATUS_ACCOUNT_NOT_ACTIVE) {
                        showMessage(message,
                                R.drawable.ic_error,
                                R.color.colorError,
                                AlertDialogFragment.OnConfirmListener {
                                    finish()
                                })
                        return@Observer
                    }

                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })

        viewModelBeneficiary.editBeneficiaryState.observe(this, Observer {
            it ?: return@Observer

            val response = it.getContentIfNotHandled() ?: return@Observer

            if (response is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()
            when(response) {
                is NetworkState2.Success -> {
                    showMessage(getString(R.string.edit_beneficiary_success),
                            R.drawable.ic_success_checked_blue,
                            R.color.blue,
                            AlertDialogFragment.OnConfirmListener {
                                finish()
                            })
                    viewModelBeneficiary.selectedBeneficiary = null
                }
                is NetworkState2.Error -> {

                    if(response.isSessionExpired)
                        return@Observer onSessionExpired(response.message)

                    val message = response.message

                    if (response.errorCode.toInt() == STATUS_ACCOUNT_NOT_ACTIVE) {
                        showMessage(message,
                                R.drawable.ic_error,
                                R.color.colorError,
                                AlertDialogFragment.OnConfirmListener {
                                    finish()
                                })
                        return@Observer
                    }

                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)

                }
            }

        })

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
                    viewModelBeneficiary.selectedBeneficiary = null
                }
                is NetworkState2.Error -> {

                    if(response.isSessionExpired)
                        return@Observer onSessionExpired(response.message)

                    val message = response.message
                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })

        viewModelBeneficiary.enableBeneficiaryState.observe(this, Observer {
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
                    viewModelBeneficiary.selectedBeneficiary = null

                }
                is NetworkState2.Error -> {

                    if(response.isSessionExpired)
                        return@Observer onSessionExpired(response.message)

                    val message = response.message
                    onError(message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), response.throwable)
                }
                else -> {
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })
    }

    private val addBeneficiaryFragments by lazy {
        listOf(
            LocalBeneficiaryDetailFragment(),
            LocalBeneficiarySummaryFragment(),
            OTPFragment.Builder(addOtpListener)
                .setPinLength(6)
                .setTitle(getString(R.string.enter_otp))
                .setHasCardView(false)
                .setButtonTitle(getString(R.string.submit))
                .build()
        )
    }

    private val deleteBeneficiaryFragments by lazy {
        listOf(
            OTPFragment.Builder(addOtpListener)
                .setPinLength(6)
                .setTitle(getString(R.string.enter_otp))
                .setHasCardView(false)
                .setButtonTitle(getString(R.string.submit))
                .build()
        )
    }
}