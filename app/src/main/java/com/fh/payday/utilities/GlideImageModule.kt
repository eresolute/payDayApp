package com.fh.payday.utilities

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.fh.payday.datasource.remote.getOkHttpBuilder
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * PayDayFH
 * Created by EResolute on 9/3/2018.
 */

private const val TIME_OUT: Long = 30

@GlideModule
class GlideImageModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = getOkHttpBuilder()
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .build()

        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}
