package com.fh.payday.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

fun compressImageSize(file: File, maxSize: Int): File {
    val compressed = compressImageSize(BitmapFactory.decodeFile(file.path), maxSize)

    FileOutputStream(file).use {
        compressed.compress(Bitmap.CompressFormat.JPEG, 80, it)
    }

    return file
}

fun compressImageSize(bitmap: Bitmap, maxSize: Int): Bitmap {
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

    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}