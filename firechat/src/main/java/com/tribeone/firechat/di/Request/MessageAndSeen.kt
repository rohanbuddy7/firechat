package com.tribeone.firechat.di.Request

import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.model.Message

internal class MessageAndSeen (
    val message: Message? = null,
    val seen: HashMap<String,String>? = null,
    val chatListResponse: ChatListResponse? = null
)

class Seen (
    val seen: HashMap<String,String>? = null
)