package com.tribeone.firechat.model

internal data class SeenAndDecision(

    val decision: Boolean = false,

    val seen: HashMap<String, String>?,

    val chatId: String?,

)
