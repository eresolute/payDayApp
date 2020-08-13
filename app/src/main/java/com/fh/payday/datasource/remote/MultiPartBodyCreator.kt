package com.fh.payday.datasource.remote

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


fun String.asRequestBody (
        mediaType: MediaType = MediaType.parse("text/plain")!!
): RequestBody = RequestBody.create(mediaType, this)

fun File.asRequestBody (
        mediaType: MediaType = MediaType.parse("image/jpeg")!!
): RequestBody = RequestBody.create(mediaType, this)

fun RequestBody.asMultipartBody (
        name: String,
        filename: String
): MultipartBody.Part =  MultipartBody.Part.createFormData(name, filename, this)
