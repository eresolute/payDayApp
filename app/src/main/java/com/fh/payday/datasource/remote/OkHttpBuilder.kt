package com.fh.payday.datasource.remote

import android.annotation.SuppressLint
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.PRODUCTION_URL
import com.fh.payday.utilities.SIT_URL
import com.fh.payday.utilities.UAT_URL
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private fun getUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    val sslSocketFactory = sslContext.socketFactory

    val builder = OkHttpClient.Builder()
    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    builder.hostnameVerifier { _, _ -> true }

    return builder
}

private fun buildCertPinner(): CertificatePinner {
    val pattern = "mobile.paydaysit.financehouse.ae"
    val pin1 = "sha256/N1bvjfL1tqp1h+/3KwjuF3+FSpVmjVXYW3dYXN/uQt0="
    val pin2 = "sha256/4a6cPehI7OG6cuDZka5NDZ7FR8a60d3auda+sKfg4Ng="
    return CertificatePinner.Builder()
        .add(pattern, pin1)
        .add(pattern, pin2)
        .build()
}

fun getOkHttpBuilder(): OkHttpClient.Builder = when(BASE_URL) {
    PRODUCTION_URL -> {
        val builder = OkHttpClient.Builder()
        builder.certificatePinner(buildCertPinner())
    }
    SIT_URL, UAT_URL -> getUnsafeOkHttpClientBuilder()
    else -> OkHttpClient.Builder()
}


