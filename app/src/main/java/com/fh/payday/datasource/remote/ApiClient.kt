package com.fh.payday.datasource.remote

import com.fh.payday.BuildConfig
import com.fh.payday.datasource.remote.refreshtoken.RefreshTokenService
import com.fh.payday.datasource.remote.refreshtoken.ServiceHolder
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.token
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Eresolute
 * Created by EResolute on 8/11/2018.
 */
class ApiClient {
    companion object {
        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient())
                .build()
        }

        /**
         * @param timeout TimeOut in seconds
         */
        private fun getOkHttpClient(timeout: Int = 120): OkHttpClient {

            val builder = getOkHttpBuilder()
            val dispatcher = Dispatcher()
            builder.dispatcher(dispatcher)

            builder.connectTimeout(timeout.toLong(), TimeUnit.SECONDS)
            builder.readTimeout(timeout.toLong(), TimeUnit.SECONDS)
            builder.writeTimeout(timeout.toLong(), TimeUnit.SECONDS)

            builder.addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("x-access-token", token)
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }

            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(interceptor)
            }

            return builder.build()
        }

        fun getOkHttpClient(
            token: String,
            sessionId: String,
            connectTimeout: Int = 120,
            readTimeout: Int = 120,
            writeTimeout: Int = 120
        ): OkHttpClient {

            val builder = getOkHttpBuilder()
            val dispatcher = Dispatcher()
            builder.dispatcher(dispatcher)

            builder.connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
            builder.readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
            builder.writeTimeout(writeTimeout.toLong(), TimeUnit.SECONDS)

            builder.addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("x-access-token", token)
                    .header("session-id", sessionId)
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }

            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(interceptor)
            }

            return builder.build()
        }

        private fun okHttpClient(
            token: String,
            sessionId: String,
            serviceHolder: ServiceHolder? = null,
            connectTimeout: Int = 120,
            readTimeout: Int = 120,
            writeTimeout: Int = 120
        ): OkHttpClient {

            return OkHttpClientInstance.Builder(serviceHolder)
                .header("x-access-token", token)
                .header("session-id", sessionId)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .writeTimeout(writeTimeout)
                .build()

        }

        @Volatile
        private var instance: Retrofit? = null

        fun getInstance(
            token: String,
            sessionId: String,
            holder: ServiceHolder? = null,
            connectTimeout: Int = 120,
            readTimeout: Int = 120,
            writeTimeout: Int = 120
        ): Retrofit = instance ?: synchronized(this) {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient(token, sessionId, holder, connectTimeout, readTimeout, writeTimeout))
                .build()
                .also {
                    instance = it
                }
        }

        fun clearInstance() {
            instance = null
        }

    }
}

// todo user ApiClient.retrofit (remove token from ApiClient.retrofit)
fun getServiceHolder(
    customerId: Long,
    refreshToken: String
): ServiceHolder? {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(ApiClient.getOkHttpClient(refreshToken, ""))
        .build()
    val service = retrofit.create(RefreshTokenService::class.java)
    return ServiceHolder(customerId, refreshToken, service)
}