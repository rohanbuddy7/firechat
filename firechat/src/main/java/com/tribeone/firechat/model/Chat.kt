package com.tribeone.firechat.model

internal data class Chat(

    val type: String? = null,

    val participants: List<String>? = null,

    val message: Message? = null

)
