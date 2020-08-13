package com.fh.payday.views.shared

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.*
import android.support.media.ExifInterface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.google.android.cameraview.CameraView
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.content_camera.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.Serializable

private const val REQUEST_CODE: Int = 100

class CameraActivity : BaseActivity() {

    private var fileName = "picture.jpeg"
    private var mBackgroundHandler: Handler? = null

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {}

        override fun onCameraClosed(cameraView: CameraView) {}

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            getBackgroundHandler()?.post {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.RGB_565
                val bitmap = BitmapFactory.decodeStream(data.inputStream(), null, options) ?: return@post
                val rotatedBitmap = rotateIfRequired(bitmap, data.inputStream())
                val cropped = cropToCenter(rotatedBitmap)
                /*val compressed = compressImageSize(cropped, 1280)*/

                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
                FileOutputStream(file).use {
                    cropped.compress(Bitmap.CompressFormat.JPEG, 80, it)
                }

                sendResult(file)
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

    override fun getLayout() = R.layout.activity_camera

    override fun init() {
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }
        fileName = intent.getStringExtra("file_name") ?: fileName
        val title = intent.getStringExtra("title") ?: getString(R.string.scan_card)
        val instructions = intent.getStringExtra("instruction") ?: getString(R.string.position_card_instruction)
        val buttonText = intent.getStringExtra("button_text") ?: getString(R.string.capture)

        tv_title.text = title
        tv_instructions.text = instructions
        btn_capture.text = buttonText

        btn_capture.setOnClickListener {
            if (camera_view.isCameraOpened) {
                it.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                camera_view.takePicture()
            }
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
        }
    }

    private fun <T> sendResult(data: T) where T : Serializable {
        runOnUiThread {
            //btn_capture.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            val returnIntent = Intent()
            returnIntent.apply {
                putExtra("data", data)
                setResult(Activity.RESULT_OK, returnIntent)
            }
            finish()
        }
    }

    private fun cropToCenter(bitmap: Bitmap) =
            if (bitmap.width >= bitmap.height){
                val x = (bitmap.width / 4 - bitmap.height / 4)
                Bitmap.createBitmap(bitmap, x, 0, bitmap.height, bitmap.height)
            } else {
                val y = (bitmap.height / 4 - bitmap.width / 4)
                Bitmap.createBitmap(bitmap, 0, y, bitmap.width, bitmap.width)
            }

    private fun rotateIfRequired(bitmap: Bitmap, inputStream: InputStream): Bitmap {
        val ei = ExifInterface(inputStream)

        return when (ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270F)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
        )
    }

    /*private fun compressImageSize(bitmap: Bitmap, maxSize: Int): Bitmap {
        Log.e("MainActivity", "Initial Size: ${(sizeOf(bitmap) / 1000)}")

        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        val compressedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        Log.e("MainActivity", "Final Size: ${(sizeOf(bitmap) / 1000)}")

        return compressedBitmap
    }

    private fun sizeOf(bitmap: Bitmap) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> bitmap.allocationByteCount
        else -> bitmap.byteCount
    }*/

}
