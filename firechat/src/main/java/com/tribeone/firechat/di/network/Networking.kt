package com.tribeone.firechat.di.network

import android.os.Build
import com.tribeone.firechat.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit


internal object Networking {

    const val authorization = "Authorization"
    const val contentType = "Content-Type"
    val BASE_URL = "https://fcm.googleapis.com/"
    const val NETWORK_CALL_TIMEOUT = 60

    /*val okHttpClientBuilder = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor()
            .apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
        .addInterceptor(Interceptor { chain ->
            val original: Request = chain.request()
            val requestBuilder = original.newBuilder()

            val extendedUserAgent = "build-version:" + BuildConfig.VERSION_CODE
                .toString() + "|" + "manufacturer:" + Build.MANUFACTURER
                .toString() + "|" + "model:" + Build.MODEL
                .toString() + "|" + "os-version:" + Build.VERSION.SDK_INT
            requestBuilder.addHeader("X-Extended-User-Agent", extendedUserAgent)
            Timber.tag("Network").d("X-Extended-User-Agent: $extendedUserAgent")

            val request = requestBuilder.build()
            return@Interceptor chain.proceed(request)
        })
        .readTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)*/


    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            //.client(okHttpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}