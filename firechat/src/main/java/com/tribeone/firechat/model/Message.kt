package com.tribeone.firechat.model

internal data class Message(

    val sorts: Int? = null,

    val messageId: String? = null,

    val type: String? = null,

    val read: Boolean? = null,

    val message: String? = null,

    val timestamp: Long? = null,

    val userId: String? = null,

    val seen: String? = null,

)
