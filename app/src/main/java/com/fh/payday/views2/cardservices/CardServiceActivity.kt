package com.fh.payday.views2.cardservices

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.models.Loan
import com.fh.payday.utilities.AccessMatrix.Companion.CARD_STATEMENT
import com.fh.payday.utilities.AccessMatrix.Companion.CHANGE_PIN
import com.fh.payday.utilities.AccessMatrix.Companion.SALARY_CREDITED
import com.fh.payday.utilities.AccessMatrix.Companion.TRANSACTIONS
import com.fh.payday.utilities.ItemOffsetDecoration
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.utilities.hasAccess
import com.fh.payday.utilities.maskCardNumber
import com.fh.payday.views.adapter.CardServiceAdapter
import com.fh.payday.views2.cardservices.statements.CardStatementActivity
import com.fh.payday.views2.settings.SettingOptionActivity
import com.fh.payday.views2.transactionhistoryv2.TransactionHistoryV2Activity
import kotlinx.android.synthetic.main.activity_card_service.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

@Suppress("NAME_SHADOWING")
class CardServiceActivity : BaseActivity(), OnItemClickListener {

    private val items: List<Item>
        get() {
            val items = ArrayList<Item>()
            val array = resources.obtainTypedArray(R.array.card_service_icons)
            val titles = resources.getStringArray(R.array.card_service_items)

            for (i in titles.indices) {
                items.add(Item(titles[i], array.getResourceId(i, 0)))
            }

            array.recycle()
            return items
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btm_home.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)
        findViewById<View>(R.id.toolbar_help).setOnClickListener {
            startHelpActivity("cardServicesHelp")
        }
    }

    override fun getLayout() = R.layout.activity_card_service

    override fun init() {
        toolbar_title.text = getString(R.string.card_services)
        setCardInfo(intent.getParcelableExtra("card") ?: return finish())

        rv_card_services.addItemDecoration(ItemOffsetDecoration(2))
        rv_card_services.layoutManager = GridLayoutManager(this, 2)
        rv_card_services.adapter = CardServiceAdapter(items, this)
    }

    private fun setCardInfo(card: Card) {
        tv_card_name.text = card.cardName
        tv_card_number.text = maskCardNumber(card.cardNumber, "XXXX XXXX XXXX ####")
    }

    override fun onItemClick(index: Int) {

        val card = intent?.getParcelableExtra<Card>("card")
        val loan = intent?.getParcelableExtra<Loan>("loan")
        val summary = intent?.getParcelableExtra<CustomerSummary>("summary")
        val cardStatus = card?.cardStatus?.toLowerCase()
        when (index) {
          /*  0 -> {
                if (hasAccess(cardStatus, CARD_STATEMENT))
                    startActivity(Intent(
                            this@CardServiceActivity, CardStatementActivity::class.java)
                            .apply {
                                card?.let {
                                    putExtra("availableBalance", it.availableBalance)
                                    putExtra("availableOverdraft", it.overdraftLimit)
                                }

                                loan?.let { putExtra("loanNumber", it.loanNumber) }
                            }
                    )
                else showCardStatusError(cardStatus)
            }*/
            0 -> {
                if (hasAccess(cardStatus, TRANSACTIONS)) {
                    val intent = Intent(this@CardServiceActivity, TransactionHistoryV2Activity::class.java)
                            .apply {
                                card?.let {
                                    putExtra("availableBalance", it.availableBalance)
                                    putExtra("availableOverdraft", it.overdraftLimit)
                                }

                                loan?.let { putExtra("loanNumber", it.loanNumber) }
                            }

                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            1 -> {
                if (hasAccess(cardStatus, SALARY_CREDITED)) {
                    val intent = Intent(this@CardServiceActivity, TransactionHistoryV2Activity::class.java)
                            .apply {
                                putExtra("transaction_type", TransactionHistoryV2Activity.SALARIES_CREDITED)
                                card?.let {
                                    putExtra("availableBalance", it.availableBalance)
                                    putExtra("availableOverdraft", it.overdraftLimit)
                                }

                                loan?.let { putExtra("loanNumber", it.loanNumber) }
                            }
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            2 -> {
                if (summary?.emiratesUpperLimitWarning != null && summary.emiratesUpperLimitWarning!!.isNotEmpty()) {
                    showWarning(summary.emiratesUpperLimitWarning!!)
                    return
                }
                if (hasAccess(cardStatus, CHANGE_PIN)) {
                    val intent = Intent(this@CardServiceActivity, SettingOptionActivity::class.java)
                    intent.putExtra("index", 1)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
        }
    }

}
