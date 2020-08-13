package com.fh.payday.views2.emiratesidscanner.autoscan

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import java.io.IOException

class TextRecognitionProcessor private constructor(
    private var successListener: ((Bitmap?, FirebaseVisionText, FrameMetadata) -> Unit)?,
    private var failureListener: ((Exception) -> Unit)?,
    private val detector: FirebaseVisionTextRecognizer
) : VisionProcessorBase<FirebaseVisionText>() {

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<FirebaseVisionText> {
        return detector.processImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: FirebaseVisionText,
        frameMetadata: FrameMetadata
    ) {
        successListener?.invoke(originalCameraImage, results, frameMetadata)
    }

    override fun onFailure(e: Exception) {
        failureListener?.invoke(e)
    }

    class Builder {
        private var successListener: ((Bitmap?, FirebaseVisionText, FrameMetadata) -> Unit)? = null
        private var failureListener: ((Exception) -> Unit)? = null
        private var recognizer: FirebaseVisionTextRecognizer =
            FirebaseVision.getInstance().onDeviceTextRecognizer

        fun addOnSuccessListener(listener: (Bitmap?, FirebaseVisionText, FrameMetadata) -> Unit): Builder {
            this.successListener = listener
            return this
        }

        fun addOnFailureListener(listener: (Exception) -> Unit): Builder {
            this.failureListener = listener
            return this
        }

        fun addRecognizer(recognizer: FirebaseVisionTextRecognizer): Builder {
            this.recognizer = recognizer
            return this
        }

        fun build(): TextRecognitionProcessor {
            return TextRecognitionProcessor(successListener, failureListener, recognizer)
        }
    }

    companion object {
        private const val TAG = "TextRecognition"
    }
}
