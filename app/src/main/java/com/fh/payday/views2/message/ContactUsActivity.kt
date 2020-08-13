package com.fh.payday.views2.message

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.webkit.URLUtil
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.utilities.FH_URL
import com.fh.payday.utilities.ItemOffsetDecoration
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.adapter.OperatorAdapter
import com.fh.payday.views2.shared.custom.ConfirmDialogFragment
import com.fh.payday.views2.shared.custom.PermissionsDialog
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class ContactUsActivity : BaseActivity(), OnItemClickListener {


    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
    }

    override fun getLayout(): Int {
        return R.layout.activity_contact_us
    }

    override fun init() {
        recyclerView = findViewById(R.id.recycler_view)
        toolbar_title.setText(R.string.contact_us)

        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)

        toolbar_help.setOnClickListener {
            startHelpActivity("applicationFooterHelp")
        }

        val optionList = DataGenerator.getContactUsOptions(this)
        recyclerView.addItemDecoration(ItemOffsetDecoration(3))
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter = OperatorAdapter(this, optionList, 3)
    }

    override fun onItemClick(index: Int) {
        when (index) {
            0 -> {
                ConfirmDialogFragment.Builder()
                        .setTitle("+971 26 194 440")
                        .setConfirmText(getString(R.string.call))
                        .setIcon(R.drawable.ic_call_center_number_large)
                        .setCancelable(true)
                        .setConfirmListener(object : ConfirmDialogFragment.OnConfirmListener {
                            override fun onConfirm(dialog: DialogInterface) {
                                dialog.dismiss()
                                if (checkPermission(Manifest.permission.CALL_PHONE)) {
                                    val dial = "tel:+97126194440"
                                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
                                } else {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@ContactUsActivity,
                                                    Manifest.permission.CALL_PHONE)) {
                                        PermissionsDialog.Builder()
                                                .setTitle(getString(R.string.phone_permission_title))
                                                .setDescription(getString(R.string.phone_permission_description))
                                                .setNegativeText(getString(R.string.app_settings))
                                                .setPositiveText(getString(R.string.not_now))
                                                .addNegativeListener {
                                                    Dialog::dismiss
                                                }
                                                .build()
                                                .show(this@ContactUsActivity)
                                    } else {
                                        ActivityCompat.requestPermissions(this@ContactUsActivity,
                                                arrayOf(Manifest.permission.CALL_PHONE), MAKE_CALL_PERMISSION_REQUEST_CODE)
                                    }
                                }

                            }
                        })
                        .build()
                        .show(supportFragmentManager, "")
            }
            1 -> {
                if (URLUtil.isHttpUrl(FH_URL) || URLUtil.isHttpsUrl(FH_URL)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(FH_URL))
                    startActivity(intent)
                } else {
                    return
                }
            }
            2 -> {
                startActivity(Intent(this, SuggestionComplaintActivity::class.java))
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            MAKE_CALL_PERMISSION_REQUEST_CODE -> {
                if (!(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    val dial = "tel:+97140080021"
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
                }
                return
            }
        }
    }
}