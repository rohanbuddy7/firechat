package com.tribeone.firechat.utils

object FcConstants {

    object Firestore {
        const val fcm = "fcm"
        const val message = "Messages"
        const val chatlist = "chatlist"
        const val lastMessage = "lastMessage"
        const val lastMessageAt = "lastMessageAt"
        const val lastMessageBy = "lastMessageBy"
        const val messageId = "messageId"
        const val messageType = "messageType"
        const val startedBy = "startedBy"
        const val unreadMessage = "unreadMessage"
        const val read = "read"
        const val timestamp = "timestamp"
        const val type = "type"
        const val userid = "userid"
        const val participants = "participants"
        const val distinctChat = 1
        const val groupChat = 2
        const val distinctType = "distinct"
        const val textTypeChat = "text"
        const val key = "key"
        const val cloud = "cloud"
        const val serverKey = "serverKey"
        const val id = "id"
        const val name = "name"
        const val profilePicture = "profilePicture"
        const val seen = "seen"
        const val availableOnMessagePage = "availableOnMessagePage"
        const val online = "online"
        const val lastMessageId = "lastMessageId"
        const val typeOpenChat = "openchat"
        const val typeStartNewConversation = "startnewconv"
        const val userId = "userid"
        const val otherUserId = "otherUserid"
        const val user = "user"
        const val chatId = "chatId"
        const val otherUser = "otherUser"
    }

    object FCM {
        const val FIRECHAT_BROADCAST = "FIRECHAT_BROADCAST"
        const val FIRECHAT_BROADCAST_ON_CHATLIST_SCREEN = "FIRECHAT_BROADCAST_ON_CHATLIST_SCREEN"
        const val FIRECHAT_BROADCAST_ON_MESSAGE_SCREEN = "FIRECHAT_BROADCAST_ON_MESSAGE_SCREEN"
        const val FIRECHAT_NEW_MESSAGE = "FIRECHAT_NEW_MESSAGE"
        const val CHANNEL_MESSAGE_RECEIVED = "CHANNEL_MESSAGE_RECEIVED"
        const val FIRECHAT_MESSAGE_SEEN = "FIRECHAT_MESSAGE_SEEN"
    }

    object FCMParams {
        const val title = "title"
        const val message = "message"
        const val action = "action"
        const val name = "name"
        const val chatId = "chatId"
        const val userId = "userId"
        const val seen = "seen"
        const val type = "type"
        const val request_uuid = "request_uuid"
        const val data = "data"
    }

    object Action {
        const val OPEN_CHATLIST = "OPEN_CHATLIST"
    }

    object RealTimeDbConstants {
    }

    object Temp {
        const val message = "Messages"
    }

    const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
    const val EXTRA_CHANNEL_ID = "CHANNEL_ID"

}