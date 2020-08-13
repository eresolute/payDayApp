package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import com.fh.payday.datasource.models.PaydayCard
import com.fh.payday.utilities.ocr.filterPaydayCardRegister
import com.fh.payday.utilities.ocr.isValidPaydayCard
import com.fh.payday.views2.emiratesidscanner.autoscan.FrameMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.coroutines.*

class PaydayCardScannerViewModel : ViewModel() {

    private val _paydayCard = MutableLiveData<Triple<Bitmap, PaydayCard, FrameMetadata>>()
    val paydayCard: LiveData<Triple<Bitmap, PaydayCard, FrameMetadata>> get() = _paydayCard

    private val _scanWarningState = MutableLiveData<Boolean>()
    val scanWarningState: LiveData<Boolean> get() = _scanWarningState

    private var warningJob: Job? = null

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private var pauseMode: Boolean = false

    init { startPauseMode() }

    override fun onCleared() {
        job.cancel()
        warningJob?.cancel()
        super.onCleared()
    }

    fun onPaydayCardScanned(bitmap: Bitmap, visionText: FirebaseVisionText, metadata: FrameMetadata) {
        if (pauseMode || !isValidPaydayCard(visionText.textBlocks)) return
        coroutineScope.launch {
            val paydayCard = filterPaydayCardRegister(visionText.textBlocks)
            setPaydayCard(bitmap, paydayCard, metadata)
        }
    }

    private fun setPaydayCard(bitmap: Bitmap, paydayCard: PaydayCard, metadata: FrameMetadata) {
        _paydayCard.value = Triple(bitmap, paydayCard, metadata)
    }

    fun startScanTimer() {
        warningJob?.cancel()
        warningJob = coroutineScope.launch(Dispatchers.Main) {
            delay(SCAN_WARNING_DELAY)
            if (_paydayCard.value == null) {
                _scanWarningState.value = true
            }
        }
    }

    private fun startPauseMode() {
        coroutineScope.launch {
            pauseMode = true
            delay(SCAN_DELAY)
            pauseMode = false
        }
    }

    companion object {
        private const val SCAN_WARNING_DELAY = 15L * 1000L
        private const val SCAN_DELAY = 5000L
    }
}