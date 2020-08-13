package com.fh.payday.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.support.media.ExifInterface


fun decodeUri(context: Context, selectedImage: Uri?): Bitmap? {
    selectedImage ?: return null

    val o = BitmapFactory.Options()
    o.inJustDecodeBounds = true
    BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o)

    val requiredSize = 140

    var widthTmp = o.outWidth
    var heightTmp = o.outHeight
    var scale = 1
    while (true) {
        if (widthTmp / 2 < requiredSize || heightTmp / 2 < requiredSize) {
            break
        }
        widthTmp /= 2
        heightTmp /= 2
        scale *= 2
    }

    val o2 = BitmapFactory.Options()
    o2.inSampleSize = scale
    return BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o2)
}

fun cropToCenter(bitmap: Bitmap): Bitmap = if (bitmap.width >= bitmap.height) {
    val x = (bitmap.width / 4 - bitmap.height / 4)
    Bitmap.createBitmap(bitmap, x, 0, bitmap.height, bitmap.height)
} else {
    val y = (bitmap.height / 4 - bitmap.width / 4)
    Bitmap.createBitmap(bitmap, 0, y, bitmap.width, bitmap.width)
}

fun rotateIfRequired(bitmap: Bitmap, path: String): Bitmap {
    val ei = ExifInterface(path)

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