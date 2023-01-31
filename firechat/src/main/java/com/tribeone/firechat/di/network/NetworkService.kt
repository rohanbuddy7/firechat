package com.tribeone.firechat.di.network

import com.tribeone.firechat.di.Request.RequestNotificaton
import dagger.Provides
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.annotation.Nullable
import javax.inject.Singleton

@Singleton
internal interface NetworkService {

    @POST("fcm/send")
    suspend fun sendFCM(
        @Header(Networking.authorization) sessionToken: String,
        @Header(Networking.contentType) contentType: String,
        @Body requestNotificaton: RequestNotificaton
    ): Response<JSONObject>?

}