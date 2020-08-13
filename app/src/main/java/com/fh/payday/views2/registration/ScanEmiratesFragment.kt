package com.fh.payday.views2.registration

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.models.customer.EmiratesIdWrapper
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.ocr.ImageScanHandler
import com.fh.payday.views2.emiratesidscanner.EmiratesIdScannerActivity
import com.fh.payday.views2.kyc.update.KycOptionActivity
import com.fh.payday.views2.shared.custom.PermissionsDialog
import kotlinx.android.synthetic.main.fragment_scan_emirates.view.*
import org.joda.time.DateTime
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ScanEmiratesFragment : BaseFragment() {

    enum class Screen { CAPTURE_FRONT, CAPTURE_BACK }

    private val viewModelReg by lazy {
        when (val activity = activity ?: return@lazy null) {
            is RegisterActivity -> activity.viewModel
            else -> null
        }
    }

    private val viewModelKyc by lazy {
        when (val activity = activity ?: return@lazy null) {
            is KycOptionActivity -> activity.getViewModel()
            else -> null
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val view = view ?: return
        if (isVisibleToUser) {
            view.btn_capture?.isEnabled = true
            view.btn_capture_kyc?.isEnabled = true

            view.btn_skip.visibility = View.GONE
            view.tv_or.visibility = View.GONE

            viewModelReg?.let { viewModel ->
                viewModel.isEmiratesDetailSet = false
                viewModel.clearInputWrapper()
                viewModel.clearWrapper()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan_emirates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tv_title?.visibility = View.GONE

        initViews(view)

        when (activity ?: return) {
            is RegisterActivity -> {
                view.btn_gallery.visibility = View.GONE
                view.btn_capture_kyc?.visibility = View.GONE
            }
            is KycOptionActivity -> initUpdateEmiratesIdView(view)
        }
    }

    private fun initViews(view: View) {
        view.tv_note?.visibility = View.GONE
        view.btn_capture?.text = getString(R.string.scan_emirates_id)
        view.image_view?.setImageResource(R.mipmap.ic_emirates_id_font)
        view.btn_capture?.setOnClickListener { scanEmiratesId() }
        view.btn_capture_kyc?.setOnClickListener { scanEmiratesId() }
        view.tv_or?.visibility = View.GONE
        view.btn_skip?.apply {
            visibility = View.GONE
            setOnClickListener { skipScan() }
        }
    }

    private fun initUpdateEmiratesIdView(view: View) {
        view.btn_capture?.visibility = View.GONE
        view.tv_title?.visibility = View.VISIBLE
        when (arguments?.getSerializable("screen") ?: Screen.CAPTURE_FRONT) {
            Screen.CAPTURE_FRONT -> handleCaptureFront(view)
            Screen.CAPTURE_BACK -> handleCaptureBack(view)
        }
    }

    private fun handleCaptureFront(view: View) {
        view.tv_title?.text = getString(R.string.capture_front_emirates_id)
        view.btn_gallery?.setOnClickListener {
            clearEidFront()
            val activity = activity ?: return@setOnClickListener
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectFromGallery(RegisterActivity.REQUEST_GALLERY_EMIRATES_FRONT)
            } else {
                requestReadStoragePerm(activity)
            }
        }
    }

    private fun handleCaptureBack(view: View) {
        view.image_view?.setImageResource(R.mipmap.ic_emirates_id_back)
        view.btn_capture_kyc?.visibility = View.GONE
        view.tv_title?.text = getString(R.string.capture_back_emirates_id)
        view.btn_gallery?.setOnClickListener {
            clearEmirates()
            val activity = activity ?: return@setOnClickListener
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectFromGallery(RegisterActivity.REQUEST_GALLERY_EMIRATES_BACK)
            } else {
                requestReadStoragePerm(activity)
            }
        }
    }

    private fun requestReadStoragePerm(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionsDialog.Builder()
                .setTitle(getString(R.string.storage_permission_title))
                .setDescription(getString(R.string.storage_permission_description))
                .setNegativeText(getString(R.string.app_settings))
                .setPositiveText(getString(R.string.not_now))
                .build()
                .show(activity)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_GALLERY_PERMISSION)
        }
    }

    private fun selectFromGallery(requestCode: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode)
    }

    private fun scanEmiratesId() {
        clearEmirates()
        if (activity?.isNetworkConnected() ?: return) {
            val intent = Intent(getActivity(), EmiratesIdScannerActivity::class.java)
            startActivityForResult(intent, REQUEST_SCAN_EMIRATES)
        } else {
            activity?.onFailure(CONNECTION_ERROR) ?: return
        }
    }

    /*private fun captureEmiratesId() {
        clearEmirates()
        if (activity?.isNetworkConnected() ?: return) {
            val intent = Intent(getActivity(), EmiratesIdScannerActivity::class.java)
                .putExtra(KEY_MODE, MODE_CAPTURE)
            startActivityForResult(intent, REQUEST_SCAN_EMIRATES)
        } else {
            activity?.onFailure(CONNECTION_ERROR) ?: return
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_SCAN_EMIRATES -> {
                    val wrapper = data?.getParcelableExtra<EmiratesIdWrapper>("data") ?: return
                    setEmiratesId(wrapper)
                }
                RegisterActivity.REQUEST_GALLERY_EMIRATES_FRONT -> {
                    val activity = activity ?: return
                    val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, data?.data)
                    onEmiratesIdFrontSelected(bitmap)
                }
                RegisterActivity.REQUEST_GALLERY_EMIRATES_BACK -> {
                    val activity = activity ?: return
                    val file = MediaStore.Images.Media.getBitmap(activity.contentResolver, data?.data)
                    onEmiratesIdBackSelected(file)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_CODE_GALLERY_PERMISSION -> view?.btn_gallery?.performClick()
            }
        }
    }

    private fun setEmiratesId(wrapper: EmiratesIdWrapper) {

        when (val activity = activity ?: return) {
            is RegisterActivity -> {
                viewModelReg?.inputWrapper?.capturedEmiratesFront = wrapper.fileFront
                viewModelReg?.inputWrapper?.capturedEmiratesBack = wrapper.fileBack
                activity.viewModel.inputWrapper.emiratesId = wrapper.emiratesId
                activity.viewModel.inputWrapper.emiratesIdExpiry = wrapper.emiratesId.expiry
                activity.navigateUp()
            }
            is KycOptionActivity -> {
                viewModelKyc?.inputWrapper?.capturedEmiratesFront = wrapper.fileFront
                viewModelKyc?.inputWrapper?.capturedEmiratesBack = wrapper.fileBack
                viewModelKyc?.inputWrapper?.emiratesId = wrapper.emiratesId
                val fragment: Fragment = EmiratesIdDetailsFragment()
                activity.replaceFragment(fragment)
            }
        }
    }

    private fun clearEmirates() {
        when (val activity = activity ?: return) {
            is RegisterActivity -> activity.viewModel.inputWrapper.emiratesId = null
            is KycOptionActivity -> activity.getViewModel().inputWrapper.emiratesId = null
        }
    }

    private fun clearEidFront() {
        when (val activity = activity ?: return) {
            is RegisterActivity -> activity.viewModel.inputWrapper.eidFront = null
            is KycOptionActivity -> activity.getViewModel().inputWrapper.eidFront = null
        }
    }

    private fun onEmiratesIdFrontSelected(bitmap: Bitmap) {
        val activity = activity ?: return
        val path = DateTime.now().toString()
        val capturedData = getFile(bitmap, "front_$path")

        when (activity) {
            is KycOptionActivity -> {
                activity.getViewModel().inputWrapper.capturedEmiratesFront = capturedData
                activity.onHandleCapture()
            }
        }
    }

    private fun onEmiratesIdBackSelected(file: Bitmap) {
        val activity = activity ?: return
        val path = DateTime.now().toString()
        val capturedData = getFile(file, "back_$path") ?: return

        when (activity) {
            is KycOptionActivity -> {
                activity.getViewModel().inputWrapper.capturedEmiratesBack = capturedData
                scanEmiratesBack(file)
            }
        }
    }

    private fun scanEmiratesBack(bitmap: Bitmap) {
        activity?.showProgress(getString(R.string.processing))

        ImageScanHandler.instance.scanEmiratesId(bitmap, object : ImageScanHandler.OnEmiratesIdScannedListener {
            override fun onScanned(emiratesId: EmiratesID?) {
                val activity = activity ?: return
                activity.hideProgress()
                val eid = emiratesId ?: EmiratesID()
                if (eid.id.isEmpty())
                    eid.id = getEidFromFront()

                if (!eid.isValid()) {
                    view?.btn_skip?.visibility = View.VISIBLE
                    view?.tv_or?.visibility = View.VISIBLE
                    return activity.onError(getString(R.string.scan_emirates_error))
                }
                when (activity) {
                    is KycOptionActivity -> {
                        val emiratesIdWrapper = EmiratesIdWrapper(
                            activity.getViewModel().inputWrapper.capturedEmiratesFront ?: return,
                            activity.getViewModel().inputWrapper.capturedEmiratesBack ?: return,
                            emiratesId ?: return
                        )
                        setEmiratesId(emiratesIdWrapper)
                    }
                }

            }
        })
    }

    private fun getEidFromFront(): String {
        return when (val activity = activity ?: return "") {
            is RegisterActivity -> activity.viewModel.inputWrapper.eidFront ?: ""
            is KycOptionActivity -> activity.getViewModel().inputWrapper.eidFront ?: ""
            else -> ""
        }
    }

    private fun getFile(bitmap: Bitmap, name: String = "image"): File? {
        val filesDir = activity?.filesDir
        val imageFile = File(filesDir, "$name.jpg")
        val os: OutputStream
        return try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os)
            os.flush()
            os.close()
            imageFile
        } catch (e: Exception) {
            null
        }
    }

    private fun skipScan() {
        when (val activity = activity ?: return) {
            is KycOptionActivity -> {
                activity.getViewModel().isEmirateDetailSet = true
                activity.getViewModel().inputWrapper.emiratesId = null
                activity.replaceFragment(EmiratesIdDetailsFragment())
            }
        }
    }

    companion object {
        const val REQUEST_SCAN_EMIRATES = 9
        private const val REQUEST_CODE_GALLERY_PERMISSION = 8
    }
}
