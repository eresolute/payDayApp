package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.fh.payday.BuildConfig
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.utilities.cropToCenter
import com.fh.payday.utilities.ocr.filterEmiratesId
import com.fh.payday.utilities.ocr.isValidEmiratesIdBack
import com.fh.payday.utilities.ocr.isValidEmiratesIdFront
import com.fh.payday.utilities.rotateIfRequired
import com.fh.payday.views2.emiratesidscanner.autoscan.FrameMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class EmiratesIdScannerViewModel : ViewModel() {

    private val _scanEmiratesIdFront = MutableLiveData<Triple<Bitmap, EmiratesID, FrameMetadata>>()
    val scanEmiratesIdFront: LiveData<Triple<Bitmap, EmiratesID, FrameMetadata>> get() = _scanEmiratesIdFront

    private val _scanEmiratesIdBack = MutableLiveData<Triple<Bitmap, EmiratesID, FrameMetadata>>()
    val scanEmiratesIdBack: LiveData<Triple<Bitmap, EmiratesID, FrameMetadata>> get() = _scanEmiratesIdBack

    private val _captureEmiratesIdFront = MutableLiveData<File>()
    val captureEmiratesIdFront: LiveData<File> get() = _captureEmiratesIdFront

    private val _captureEmiratesIdBack = MutableLiveData<File>()
    val captureEmiratesIdBack: LiveData<File> get() = _captureEmiratesIdBack

    private val _scanWarningState = MutableLiveData<Boolean>()
    val scanWarningState: LiveData<Boolean> get() = _scanWarningState

    private var warningJob: Job? = null

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private var currentItem: Int = 0

    private var pauseMode: Boolean = false

    init {
        startPauseMode(FRONT_SCAN_DELAY)
    }

    override fun onCleared() {
        job.cancel()
        warningJob?.cancel()
        super.onCleared()
    }

    fun setCurrentItem(position: Int) {
        currentItem = position
        _scanWarningState.value = false
        startScanTimer()
    }

    fun onEmiratesIdScanned(bitmap: Bitmap, visionText: FirebaseVisionText, metadata: FrameMetadata) {

        if (BuildConfig.DEBUG) {
            Log.d("Emirates Scanner", visionText.text)
        }

        when (currentItem) {
            0 -> onEmiratesIdFrontScanned(bitmap, visionText, metadata)
            1 -> onEmiratesIdBackScanned(bitmap, visionText, metadata)
        }
    }

    private fun onEmiratesIdFrontScanned(bitmap: Bitmap, visionText: FirebaseVisionText, metadata: FrameMetadata) {
        if (pauseMode || !isValidEmiratesIdFront(visionText.textBlocks)) return

        val emiratesId = filterEmiratesId(visionText.textBlocks)
        _scanEmiratesIdFront.value = Triple(bitmap, emiratesId, metadata)
        startPauseMode(BACK_SCAN_DELAY)
    }

    private fun onEmiratesIdBackScanned(bitmap: Bitmap, visionText: FirebaseVisionText, metadata: FrameMetadata) {
        if (pauseMode || !isValidEmiratesIdBack(visionText.textBlocks)) return

        startPauseMode(BACK_SCAN_DELAY)
        val emiratesId = filterEmiratesId(visionText.textBlocks)
        _scanEmiratesIdBack.value = Triple(bitmap, emiratesId, metadata)
    }

    fun onEmiratesIdCaptured(byteArray: ByteArray, fileFront: File, fileBack: File) {
        when (currentItem) {
            0 -> onEmiratesIdFrontCaptured(byteArray, fileFront)
            1 -> onEmiratesIdBackCaptured(byteArray, fileBack)
        }
    }

    private fun onEmiratesIdFrontCaptured(byteArray: ByteArray, file: File) {
        coroutineScope.launch {
            val convertedFile = convertToFile(byteArray, file)
            _captureEmiratesIdFront.value = convertedFile
        }
    }

    private fun onEmiratesIdBackCaptured(byteArray: ByteArray, file: File) {
        coroutineScope.launch {
            val convertedFile = convertToFile(byteArray, file)
            _captureEmiratesIdBack.value = convertedFile
        }
    }

    private suspend fun convertToFile(byteArray: ByteArray, file: File) = withContext(Dispatchers.IO) {
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.use { it.write(byteArray) }

        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val rotatedBitmap = rotateIfRequired(bitmap, file.path)
        val cropped = cropToCenter(rotatedBitmap)

        FileOutputStream(file).use { cropped.compress(Bitmap.CompressFormat.JPEG, 80, it) }
        file
    }

    private fun startPauseMode(delay: Long) {
        coroutineScope.launch {
            pauseMode = true
            delay(delay)
            pauseMode = false
        }
    }

    fun startScanTimer() {
        warningJob?.cancel()
        warningJob = coroutineScope.launch(Dispatchers.Main) {
            delay(SCAN_WARNING_DELAY)
            if (_scanEmiratesIdFront.value == null || _scanEmiratesIdBack.value == null) {
                _scanWarningState.value = true
            }
        }
    }

    companion object {
        private const val FRONT_SCAN_DELAY = 5000L
        private const val BACK_SCAN_DELAY = 3000L
        private const val SCAN_WARNING_DELAY = 15L * 1000L
    }

}