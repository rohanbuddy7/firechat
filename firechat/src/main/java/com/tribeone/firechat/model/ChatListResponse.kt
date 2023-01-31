package com.tribeone.firechat.model

import android.os.Parcel
import android.os.Parcelable

internal data class ChatListResponse(

    var chatId: String?,

    val lastMessageAt: Long? = null,

    val lastMessage: String?,

    val lastMessageBy: String?,

    val messageId: String?,

    var messageType: String?,

    val participants: List<String>?,

    val startedBy: String?,

    var seen: HashMap<String, String>?,

    var participantsDetails: HashMap<String, Users>? = null,

): Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readSerializable() as HashMap<String, String>,
        parcel.readSerializable() as HashMap<String, Users>
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(chatId)
        parcel.writeValue(lastMessageAt)
        parcel.writeString(lastMessage)
        parcel.writeString(lastMessageBy)
        parcel.writeString(messageId)
        parcel.writeString(messageType)
        parcel.writeStringList(participants)
        parcel.writeString(startedBy)
        parcel.writeSerializable(seen)
        parcel.writeSerializable(participantsDetails)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatListResponse> {
        override fun createFromParcel(parcel: Parcel): ChatListResponse {
            return ChatListResponse(parcel)
        }

        override fun newArray(size: Int): Array<ChatListResponse?> {
            return arrayOfNulls(size)
        }
    }

}
