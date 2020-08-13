package com.fh.payday.datasource.remote

import com.fh.payday.BuildConfig
import com.fh.payday.datasource.remote.refreshtoken.ServiceHolder
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class OkHttpClientInstance {

    class Builder(
        private val serviceHolder: ServiceHolder?
    ) {
        private val headers = HashMap<String, String>()
        private var connectTimeout: Int = 30
        private var readTimeout: Int = 30
        private var writeTimeout: Int = 30

        fun header(key: String, value: String): Builder {
            headers[key] = value
            return this
        }

        fun connectTimeout(timeout: Int): Builder {
            this.connectTimeout = timeout
            return this
        }

        fun readTimeout(timeout: Int): Builder {
            this.readTimeout = timeout
            return this
        }

        fun writeTimeout(timeout: Int): Builder {
            this.writeTimeout = timeout
            return this
        }

        fun build(): OkHttpClient {
            val authenticator = TokenAuthenticator(serviceHolder)

            val builder = getOkHttpBuilder()
                .dispatcher(Dispatcher())
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .addHeader("accept", "*/*")
                        .addHeader("accept-encoding:gzip", "gzip, deflate")
                        .addHeader("accept-language", "en-US,en;q=0.9")
                        .method(original.method(), original.body())

                    for (entry in headers.entries) {
                        requestBuilder.addHeader(entry.key, entry.value)
                    }

                    val response = chain.proceed(requestBuilder.build())
                    if ("updated" == response.request().header("token-status")) {
                        val newToken = response.request().header("x-access-token")
                        val newRefreshToken = response.request().header("refresh-token")
                        if (newToken.isNullOrEmpty() || newRefreshToken.isNullOrEmpty())
                            return@addInterceptor response

                        return@addInterceptor response
                            .add("token", newToken)
                            ?.add("refreshToken", newToken) ?: response
                    }

                    response
                }
                .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
                .writeTimeout(writeTimeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)

            builder.authenticator(authenticator)

            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(interceptor)
            }

            return builder.build()
        }

        private fun Response.add(key: String, value: String): Response? {
            return try {
                val jsonStr = body().asString() ?: return null
                val jsonObject = JSONObject(jsonStr)
                jsonObject.put(key, value)
                val contentType = body()?.contentType()
                val body = ResponseBody.create(contentType, jsonObject.toString())
                newBuilder().body(body).build()
            } catch (e: JSONException) {
                null
            }
        }

    }
}

fun ResponseBody?.asString(): String? {
    val source = this?.source()
    source?.request(Long.MAX_VALUE)
    val buffer = source?.buffer()
    return buffer?.clone()?.readString(Charset.forName("UTF-8"))
}