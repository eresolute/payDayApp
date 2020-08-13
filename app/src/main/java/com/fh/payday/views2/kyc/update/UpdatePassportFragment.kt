package com.fh.payday.views2.kyc.update

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.views.shared.CameraActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import java.io.File

class UpdatePassportFragment: Fragment() {

    private lateinit var activity: KycOptionActivity
    private lateinit var btnStart: MaterialButton

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as KycOptionActivity
    }

    override fun onResume() {
        super.onResume()
        activity.setToolbarText(getString(R.string.update_passport))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update_passport, container, false)

        btnStart = view.findViewById(R.id.btn_start)

        handlePhotoPage()

        return view
    }

    private fun handlePhotoPage() {
        addObserver()
        btnStart.setOnClickListener {
            val intent = Intent(getActivity(), CameraActivity::class.java)
            intent.putExtra("title", getString(R.string.capture_photo_page_passport))
            intent.putExtra("instruction", "")
            startActivityForResult(intent, KycOptionActivity().REQUEST_PHOTO_PASSPORT_CARD)
        }
    }

    private fun handleSignaturePage() {
        val intent = Intent(getActivity(), CameraActivity::class.java)
        intent.putExtra("title", getString(R.string.capture_signature_page_passport))
        intent.putExtra("instruction", "")
        startActivityForResult(intent, KycOptionActivity().REQUEST_SIGNATURE_PASSPORT_CARD)
    }

    private fun handleAddressPage() {
        val intent = Intent(getActivity(), CameraActivity::class.java)
        intent.putExtra("title", getString(R.string.capture_address_page_passport))
        intent.putExtra("instruction", "")
        startActivityForResult(intent, KycOptionActivity().REQUEST_ADDRESS_PASSPORT_CARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == KycOptionActivity().REQUEST_PHOTO_PASSPORT_CARD) {
            /*byte[] capturedData = data.getByteArrayExtra("data");
            activity.getViewModel().onScanEmirates(capturedData);*/

            activity.getViewModel().passportPhoto = data?.getSerializableExtra("data") as File

            AlertDialogFragment.Builder()
                    .setMessage(getString(R.string.passport_capture_successful))
                    .setIcon(R.drawable.ic_success_checked_blue)
                    .setCancelable(false)
                    .setConfirmListener { dialog ->
                        dialog.dismiss()
                        handleSignaturePage()
                    }
                    .build()
                    .show(fragmentManager, "")

        } else if (resultCode == Activity.RESULT_OK && requestCode == KycOptionActivity().REQUEST_SIGNATURE_PASSPORT_CARD) {

            activity.getViewModel().passportSignature = data?.getSerializableExtra("data") as File

            AlertDialogFragment.Builder()
                    .setMessage(getString(R.string.passport_capture_successful))
                    .setIcon(R.drawable.ic_success_checked_blue)
                    .setCancelable(false)
                    .setConfirmListener { dialog ->
                        dialog.dismiss()
                        handleAddressPage()
                    }
                    .build()
                    .show(fragmentManager, "")
        } else if (resultCode == Activity.RESULT_OK && requestCode == KycOptionActivity().REQUEST_ADDRESS_PASSPORT_CARD) {

//            activity.getViewModel().passportAddress = data?.getSerializableExtra("data") as File

            val user = UserPreferences.instance.getUser(activity)?: return

            activity.getViewModel().uploadPassport(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                    activity.getViewModel().passportPhoto,
                    activity.getViewModel().passportSignature,
                    data?.getSerializableExtra("data") as File
            )
        }
    }

    private fun onCapturePassport () {
        AlertDialogFragment.Builder()
                .setMessage(getString(R.string.passport_update_succesful))
                .setIcon(R.drawable.ic_success_checked_blue)
                .setCancelable(false)
                .setConfirmListener {dialog ->
                    dialog.dismiss()
                    activity?.finish()
                }
                .build()
                .show(fragmentManager, "")
    }

    private fun addObserver() {
        activity.getViewModel().uploadPassport.observe(this, Observer {
            if (it == null) return@Observer

            val passportState = it.getContentIfNotHandled() ?: return@Observer

            if (passportState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                btnStart.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnStart.isEnabled = true

            when (passportState) {
                is NetworkState2.Success -> AlertDialogFragment.Builder()
                        .setMessage(getString(R.string.passport_update_succesful))
                        .setIcon(R.drawable.ic_success_checked_blue)
                        .setCancelable(false)
                        .setConfirmListener {dialog ->
                            dialog.dismiss()
                            activity.finish()
                        }
                        .build()
                        .show(fragmentManager, "")
                is NetworkState2.Error -> {
                    if (passportState.isSessionExpired)
                        return@Observer activity.onSessionExpired(passportState.message)
                    activity.onError(passportState.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

}