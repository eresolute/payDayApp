@file:JvmName("CameraUtils")

package com.fh.payday.utilities

import android.hardware.Camera
import java.util.*
import kotlin.math.abs

@Suppress("deprecation")
object CameraUtils {

    private const val ASPECT_TOLERANCE = 0.2

    fun getOptimalPreviewSize(
        displayOrientation: Int,
        width: Int,
        height: Int,
        parameters: Camera.Parameters
    ): Camera.Size? {
        var targetRatio = width.toDouble() / height
        val sizes = parameters.supportedPreviewSizes
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE
        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = height.toDouble() / width
        }

        // Try to find an size match aspect ratio and size
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                if (abs(size.height - height) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - height).toDouble()
                }
            }
        }

        // Cannot find the one match the aspect ratio, ignore
        // the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (abs(size.height - height) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - height).toDouble()
                }
            }
        }

        return optimalSize
    }

    fun getBestAspectPreviewSize(
        displayOrientation: Int,
        width: Int,
        height: Int,
        parameters: Camera.Parameters
    ): Camera.Size? {
        return getBestAspectPreviewSize(displayOrientation, width, height,
            parameters)
    }

    private fun getBestAspectPreviewSize(
        displayOrientation: Int,
        width: Int,
        height: Int,
        parameters: Camera.Parameters,
        closeEnough: Double = 0.0
    ): Camera.Size? {
        var targetRatio = width.toDouble() / height
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE
        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = height.toDouble() / width
        }
        val sizes = parameters.supportedPreviewSizes
        Collections.sort(sizes,
            Collections.reverseOrder(SizeComparator()))
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (abs(ratio - targetRatio) < minDiff) {
                optimalSize = size
                minDiff = abs(ratio - targetRatio)
            }
            if (minDiff < closeEnough) {
                break
            }
        }
        return optimalSize
    }

    private class SizeComparator : Comparator<Camera.Size> {
        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
            val left = lhs.width * lhs.height
            val right = rhs.width * rhs.height
            if (left < right) {
                return -1
            } else if (left > right) {
                return 1
            }
            return 0
        }
    }
}
