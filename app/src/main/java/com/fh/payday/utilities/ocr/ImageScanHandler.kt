package com.fh.payday.utilities.ocr

import android.graphics.Bitmap
import android.util.Log
import com.fh.payday.datasource.models.PaydayCard
import com.fh.payday.datasource.models.customer.EmiratesID
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class ImageScanHandler private constructor(){

    companion object {
        val instance: ImageScanHandler by lazy { ImageScanHandler() }
    }

    fun scanEmiratesId(bitmap: Bitmap, listener: OnEmiratesIdScannedListener/*, context: Context? = null*/) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        recognizer.processImage(image)
                .addOnSuccessListener {
                    /*logEvent(context, Bundle().apply {
                        putString("scanned_text", it.text)
                    })*/
                    Log.i("ImageScanHandler", it.text)

                    val blocks = it.textBlocks
                    if (blocks.size == 0) {
                        return@addOnSuccessListener listener.onScanned(null)
                    }

                    listener.onScanned(filterEmiratesId(blocks))
                }
                .addOnFailureListener {
                    listener.onScanned(null)
                    /*logEvent(context, Bundle().apply {
                            putString("ex_message", it.message)
                        })*/
                }
    }

    fun scanEmiratesId(bitmap: Bitmap, block: (EmiratesID?) -> Unit) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        recognizer.processImage(image)
            .addOnSuccessListener {
                Log.i("ImageScanHandler", it.text)

                val blocks = it.textBlocks
                if (blocks.size == 0) {
                    return@addOnSuccessListener block(null)
                }

                block(filterEmiratesId(blocks))
            }
            .addOnFailureListener {
                block(null)
            }
    }

    /*private fun logEvent(context: Context?, bundle: Bundle) {
        FirebaseAnalytics.getInstance(context ?: return)
            .logEvent("event_scan_emirates", bundle)
    }*/

    fun scanPaydayCard(bitmap: Bitmap, listener: OnCardScannedListener) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        recognizer.processImage(image)
                .addOnSuccessListener {
                    Log.i("ImageScanHandler", it.text)

                    val blocks = it.textBlocks
                    if (blocks.size == 0) {
                        return@addOnSuccessListener listener.onScanned(null)
                    }

                    listener.onScanned(filterPaydayCard(blocks))
                }
                .addOnFailureListener {
                    listener.onScanned(null)
                }
    }

    interface OnEmiratesIdScannedListener {
        fun onScanned(emiratesId: EmiratesID?)
    }

    interface OnCardScannedListener {
        fun onScanned(card: PaydayCard?)
    }
}