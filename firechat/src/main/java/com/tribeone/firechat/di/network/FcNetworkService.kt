package com.tribeone.firechat.di.network

import com.tribeone.firechat.di.Request.RequestNotificaton
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Singleton

@Singleton
internal interface FcNetworkService {

    @POST("fcm/send")
    suspend fun sendFCM(
        @Header(FcNetworking.authorization) sessionToken: String,
        @Header(FcNetworking.contentType) contentType: String,
        @Body requestNotificaton: RequestNotificaton
    ): Response<JSONObject>?

}