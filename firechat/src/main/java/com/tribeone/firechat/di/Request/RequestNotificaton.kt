package com.tribeone.firechat.di.Request

import com.google.gson.annotations.SerializedName

internal class RequestNotificaton (
    var registration_ids: Array<String>? = null,
    var data: Data? = null
)

internal class Data (
    var type: String? = null,
    var request_uuid: String? = null,
    var data: InternalData? = null,
)

internal class InternalData (
    var title: String? = null,
    var message: String? = null,
    var action: String? = null,
    var chatId: String? = null,
    var seen: String? = null,
    var userId: String? = null,
)