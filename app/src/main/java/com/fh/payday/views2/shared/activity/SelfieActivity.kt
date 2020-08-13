package com.fh.payday.views2.shared.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import com.google.android.cameraview.CameraView
import kotlinx.android.synthetic.main.activity_selfie.*
import kotlinx.android.synthetic.main.content_selfie.*
import java.io.File
import java.io.FileOutputStream

private const val REQUEST_CODE: Int = 101

class SelfieActivity : BaseActivity() {
    private var fileName = "selfie.jpg"
    private var mBackgroundHandler: Handler? = null
    private lateinit var switchCamera: ImageView

    enum class CameraSide{FRONT, BACK}

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {}

        override fun onCameraClosed(cameraView: CameraView) {}

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            getBackgroundHandler()?.post {
                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.use {
                    it.write(data)
                }

                runOnUiThread {
                    hideProgress()
                    val returnIntent = Intent()
                    returnIntent.apply {
                        putExtra("data", file)
                        setResult(Activity.RESULT_OK, returnIntent)
                    }
                    finish()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camera_view.addCallback(mCallback)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                camera_view.start()
            } catch (e: RuntimeException) {
                Toast.makeText(this, e.message ?: getString(R.string.unable_to_start_camera), Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                PermissionsDialog.Builder()
                    .setTitle(getString(R.string.camera_permission_title))
                    .setDescription(getString(R.string.camera_permission_description))
                    .setNegativeText(getString(R.string.app_settings))
                    .setPositiveText(getString(R.string.not_now))
                    .addNegativeListener {
                        finish()
                    }
                    .build()
                    .show(this)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
            }
        }
    }

    override fun onPause() {
        camera_view.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBackgroundHandler?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                it.looper.quitSafely()
            } else {
                it.looper.quit()
            }
        }
        mBackgroundHandler = null
    }

    override fun getLayout() = R.layout.activity_selfie

    override fun init() {
        var side = CameraSide.FRONT
        switchCamera = findViewById(R.id.switch_camera)
        switchCamera.setOnClickListener {
            if (side == CameraSide.FRONT){
                camera_view.facing = CameraView.FACING_BACK
                side = CameraSide.BACK
            } else {
                camera_view.facing = CameraView.FACING_FRONT
                side = CameraSide.FRONT
            }
        }
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }
        btn_capture.setOnClickListener {
            showProgress()
            camera_view.takePicture()
        }
    }

    private fun getBackgroundHandler(): Handler? {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("camera-handler")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }

        return mBackgroundHandler
    }

    override fun showProgress(message: String, cancellable: Boolean, dismissListener: ProgressDialogFragment.OnDismissListener) {
        btn_capture.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress_bar.visibility = View.GONE
        btn_capture.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    camera_view.addCallback(mCallback)
                }
                else {
                    //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
                    finish()
                }
            }
        }
    }
}
