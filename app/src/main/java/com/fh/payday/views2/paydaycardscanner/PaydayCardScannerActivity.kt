package com.fh.payday.views2.paydaycardscanner

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.PaydayCard
import com.fh.payday.viewmodels.PaydayCardScannerViewModel
import com.fh.payday.views2.emiratesidscanner.autoscan.CameraSource
import com.fh.payday.views2.emiratesidscanner.autoscan.TextRecognitionProcessor
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.google.android.cameraview.Constants
import kotlinx.android.synthetic.main.activity_payday_card_scanner.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PaydayCardScannerActivity : BaseActivity() {
    private var cameraSource: CameraSource? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(PaydayCardScannerViewModel :: class.java)
    }

    override fun getLayout(): Int  = R.layout.activity_payday_card_scanner

    override fun init() {
        toolbar_title.text = ""
        toolbar_back.setOnClickListener(this)
        createCameraSource()
        createTorchView()

        attachPaydayCardObserver()

        viewModel.startScanTimer().also { addScanWarningObserver() }
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

    private fun addFlashClickListener() {
        when (cameraSource?.flashMode) {
            Constants.FLASH_AUTO, Constants.FLASH_ON, Constants.FLASH_OFF, Constants.FLASH_RED_EYE -> {
                setDrawableTorch(R.drawable.ic_flash)
                setFlashMode(Constants.FLASH_TORCH)
            }
            else -> {
                setDrawableTorch(R.drawable.ic_no_flash)
                setFlashMode(Constants.FLASH_OFF)
            }
        }
    }

    @Throws(java.lang.IllegalStateException::class)
    fun setFlashMode(flash: Int): Boolean {
        val cameraSource = this.cameraSource ?: return false
        return cameraSource.setFlashMode(flash)
    }

    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(this)
        }

        val textRecognizerProcessor = TextRecognitionProcessor.Builder()
            .addOnSuccessListener { bitmap, visionText, metadata ->
                bitmap?.let { viewModel.onPaydayCardScanned(it, visionText, metadata) }
            }
            .addOnFailureListener {

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

    private fun attachPaydayCardObserver() {
        viewModel.paydayCard.observe(this, Observer { triple ->
            val bitmapBack = triple?.first ?: return@Observer
            val paydayCard = triple.second


            val filePaydayCard = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILE_NAME_PAYDAY_CARD)
            FileOutputStream(filePaydayCard).use { bitmapBack.compress(Bitmap.CompressFormat.JPEG, 80, it) }

            camera_source_preview?.stop()
            vibrate()
            sendResult(paydayCard)
        })
    }

    private fun addScanWarningObserver() {
        viewModel.scanWarningState.observe(this, Observer { warn ->
            warn ?: return@Observer
            tv_message.visibility = if(warn) View.VISIBLE else View.GONE
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

    private fun sendResult(paydayCard: PaydayCard) {
        val returnIntent = Intent()
        returnIntent.apply {
            putExtra("data", paydayCard)
            setResult(Activity.RESULT_OK, returnIntent)
        }
        finish()
    }

    companion object {
        private const val VIBRATE_DUR = 200L
        private const val CAMERA_PERM_REQUEST = 12
        private const val FILE_NAME_PAYDAY_CARD = "payday_card.jpeg"
    }
}
