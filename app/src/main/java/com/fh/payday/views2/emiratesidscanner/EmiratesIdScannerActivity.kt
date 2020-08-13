package com.fh.payday.views2.emiratesidscanner

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.models.customer.EmiratesIdWrapper
import com.fh.payday.viewmodels.EmiratesIdScannerViewModel
import com.fh.payday.views2.emiratesidscanner.autoscan.CameraSource
import com.fh.payday.views2.emiratesidscanner.autoscan.TextRecognitionProcessor
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import com.google.android.cameraview.Constants.*
import kotlinx.android.synthetic.main.activity_emirates_id_scanner.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class EmiratesIdScannerActivity : BaseActivity() {

    private val emiratesFront by lazy {
        File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILE_NAME_EMIRATES_ID_FRONT)
    }
    private val emiratesBack by lazy {
        File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILE_NAME_EMIRATES_ID_BACK)
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(EmiratesIdScannerViewModel::class.java)
    }
    private var cameraSource: CameraSource? = null

    override fun getLayout(): Int = R.layout.activity_emirates_id_scanner

    override fun onBackPressed() {
        when (view_pager.currentItem) {
            0 -> super.onBackPressed()
            else -> view_pager.currentItem = view_pager.currentItem - 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        createTorchView()

        when (intent.getIntExtra(KEY_MODE, 0)) {
            MODE_CAPTURE -> {
                toolbar_title.text = getString(R.string.capture_emirated_id)
                addCaptureEmiratesIdFrontObserver()
                addCaptureEmiratesIdBackObserver()
            }
            else -> {
                toolbar_title.text = getString(R.string.scan_emirates_id)
                addScanEmiratesIdFrontObserver()
                addScanEmiratesIdBackObserver()
            }
        }

        viewModel.startScanTimer().also { addScanWarningObserver() }
    }

    /*private fun createHelpView() {
        ignored_view.text = getString(R.string.help)
        toolbar_help.apply {
            text = getString(R.string.help)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_help, 0, 0, 0)
            } else {
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_help, 0, 0, 0)
            }
        }

        toolbar_help.setOnClickListener(this)
    }*/

    private fun createTorchView() {
        ignored_view.text = ""
        toolbar_help.apply {
            text = ""
            setDrawableTorch(R.drawable.ic_no_flash)
        }

        toolbar_help.setOnClickListener { addFlashClickListener() }
    }

    private fun setDrawableTorch(res: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            toolbar_help.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        } else {
            toolbar_help.setCompoundDrawablesWithIntrinsicBounds(res, 0, 0, 0)
        }
    }

    override fun init() {
        createCameraSource()
        view_pager.adapter = SwipeAdapter(supportFragmentManager, intent.getIntExtra(KEY_MODE, 0))
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                viewModel.setCurrentItem(position)
            }
        })

        when (intent.getIntExtra(KEY_MODE, 0)) {
            MODE_CAPTURE -> initCaptureView()
        }
    }

    private fun initCaptureView() {
        fab_capture.show()
        fab_capture.setOnClickListener { takePicture() }
    }

    private fun takePicture() {
        showProgress()
        camera_source_preview?.takePicture { data ->
            viewModel.onEmiratesIdCaptured(data, emiratesFront, emiratesBack)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraSource()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showCameraPermissionsDialog()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_REQUEST)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        camera_source_preview?.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        camera_source_preview?.release()
    }

    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(this)
        }

        val textRecognizerProcessor = TextRecognitionProcessor.Builder()
            .addOnSuccessListener { bitmap, visionText, metadata ->
                if (intent.getIntExtra(KEY_MODE, 0) == MODE_CAPTURE) return@addOnSuccessListener
                bitmap?.let { viewModel.onEmiratesIdScanned(it, visionText, metadata) }
            }
            .addOnFailureListener {
                if (intent.getIntExtra(KEY_MODE, 0) == MODE_CAPTURE) return@addOnFailureListener
            }
            .build()

        cameraSource?.setMachineLearningFrameProcessor(textRecognizerProcessor)
    }

    private fun startCameraSource() {
        try {
            cameraSource?.let { camera_source_preview.start(it) }
        } catch (e: IOException) {
            cameraSource?.release()
            cameraSource = null
        }
    }

    private fun navigateUp() {
        view_pager.currentItem = view_pager.currentItem + 1
    }

    private fun showCameraPermissionsDialog() {
        PermissionsDialog.Builder()
            .setTitle(getString(R.string.camera_permission_title))
            .setDescription(getString(R.string.camera_permission_description))
            .setNegativeText(getString(R.string.app_settings))
            .setPositiveText(getString(R.string.not_now))
            .addNegativeListener { finish() }
            .build()
            .show(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERM_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
        }
    }

    override fun showProgress(
        message: String,
        cancellable: Boolean,
        dismissListener: ProgressDialogFragment.OnDismissListener
    ) {
        progress_bar.visibility = View.VISIBLE
        fab_capture.hide()
    }

    override fun hideProgress() {
        progress_bar.visibility = View.GONE
        fab_capture.show()
    }

    private fun addScanEmiratesIdFrontObserver() {
        viewModel.scanEmiratesIdFront.observe(this, Observer { triple ->
            triple?.first ?: return@Observer
            vibrate()
            navigateUp()
        })
    }

    private fun addScanEmiratesIdBackObserver() {
        viewModel.scanEmiratesIdBack.observe(this, Observer { triple ->
            camera_source_preview?.stop()

            val bitmapFront = viewModel.scanEmiratesIdFront.value?.first ?: return@Observer
            val bitmapBack = triple?.first ?: return@Observer
            val emiratesId = triple.second

            val fileEmiratesIdFront = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILE_NAME_EMIRATES_ID_FRONT)
            FileOutputStream(fileEmiratesIdFront).use { bitmapFront.compress(Bitmap.CompressFormat.JPEG, 80, it) }

            val fileEmiratesIdBack = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILE_NAME_EMIRATES_ID_BACK)
            FileOutputStream(fileEmiratesIdBack).use { bitmapBack.compress(Bitmap.CompressFormat.JPEG, 80, it) }

            vibrate()
            sendResult(EmiratesIdWrapper(fileEmiratesIdFront, fileEmiratesIdBack, emiratesId))
        })
    }

    private fun addCaptureEmiratesIdFrontObserver() {
        viewModel.captureEmiratesIdFront.observe(this, Observer { file ->
            hideProgress()
            file ?: return@Observer
            navigateUp()
        })
    }

    private fun addCaptureEmiratesIdBackObserver() {
        viewModel.captureEmiratesIdBack.observe(this, Observer { file ->
            hideProgress()
            val fileFront = viewModel.captureEmiratesIdFront.value ?: return@Observer
            val fileBack = file ?: return@Observer

            camera_source_preview?.stop()
            sendResult(EmiratesIdWrapper(fileFront, fileBack, EmiratesID()))
        })
    }

    private fun sendResult(wrapper: EmiratesIdWrapper) {
        val returnIntent = Intent()
        returnIntent.apply {
            putExtra("data", wrapper)
            setResult(Activity.RESULT_OK, returnIntent)
        }
        finish()
    }

    private fun addFlashClickListener() {
        try {
            when (cameraSource?.flashMode) {
                FLASH_AUTO, FLASH_ON, FLASH_OFF, FLASH_RED_EYE -> {
                    setFlashMode(FLASH_TORCH)
                    setDrawableTorch(R.drawable.ic_flash)
                }
                else -> {
                    setFlashMode(FLASH_OFF)
                    setDrawableTorch(R.drawable.ic_no_flash)
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    @Throws(java.lang.IllegalStateException::class)
    fun setFlashMode(flash: Int): Boolean {
        val cameraSource = this.cameraSource ?: return false
        return cameraSource.setFlashMode(flash)
    }

    /*private fun hasFlash(): Boolean {
        val cameraSource = this.cameraSource ?: return false
        return cameraSource.hasFlash()
    }*/

    private fun addScanWarningObserver() {
        viewModel.scanWarningState.observe(this, Observer { warn ->
            warn ?: return@Observer
            tv_message.visibility = if (warn) View.VISIBLE else View.GONE
        })
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator?
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(VibrationEffect.createOneShot(VIBRATE_DUR, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator?.vibrate(VIBRATE_DUR)
        }
    }

    class SwipeAdapter(fm: FragmentManager, private val mode: Int) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> EmiratesIdScannerOverlayFragment.newInstance(EmiratesIdScannerOverlayFragment.TYPE_FRONT, mode)
            1 -> EmiratesIdScannerOverlayFragment.newInstance(EmiratesIdScannerOverlayFragment.TYPE_BACK, mode)
            else -> throw IllegalStateException("Unexpected position $position")
        }

        override fun getCount(): Int = 2
    }

    companion object {
        private const val VIBRATE_DUR = 200L

        private const val CAMERA_PERM_REQUEST = 12
        private const val FILE_NAME_EMIRATES_ID_FRONT = "emirates_id_front.jpeg"
        private const val FILE_NAME_EMIRATES_ID_BACK = "emirates_id_back.jpeg"
        const val MODE_CAPTURE = 87
        const val KEY_MODE = "mode"
    }

}
