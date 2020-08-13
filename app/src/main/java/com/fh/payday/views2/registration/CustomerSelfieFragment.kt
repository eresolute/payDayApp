package com.fh.payday.views2.registration

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.views2.shared.activity.SelfieActivity

private const val REQUEST_CODE_SELFIE = 9

class CustomerSelfieFragment : Fragment() {

    private lateinit var activity: RegisterActivity
    private lateinit var btnCapture: Button

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity = context as RegisterActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_customer_selfie, container, false)
        btnCapture = view.findViewById(R.id.btn_capture)
        btnCapture.setOnClickListener { startActivityForResult(Intent(activity, SelfieActivity::class.java), REQUEST_CODE_SELFIE) }

        addObserver()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_SELFIE -> {
                    data?.let {
                        /*val capturedData = it.getSerializableExtra("data") as File
                        activity.viewModel.registerWrapper?.apply {
                            activity.viewModel.uploadSelfie(capturedData)
                        }*/
                    }
                }
            }
        }
    }

    private fun addObserver() {
        activity.viewModel.uploadSelfieState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btnCapture.isEnabled = false
                activity.showProgress()
                return@Observer
            }

            btnCapture.isEnabled = true
            activity.hideProgress()

            when(state) {
                is NetworkState2.Success<*> -> {
                    //activity.onSelfieCapture()
                }
                is NetworkState2.Error<*> -> {
                    val error = state as NetworkState2.Error<*>
                    activity.onError(error.message)
                }
                is NetworkState2.Failure<*> -> {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error))
                }
                else -> {
                    activity.onFailure(activity.findViewById(R.id.root_view)!!, CONNECTION_ERROR)
                }
            }
        })
    }
}
