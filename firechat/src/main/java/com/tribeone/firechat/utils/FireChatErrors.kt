package com.tribeone.firechat.utils

import timber.log.Timber
import java.lang.Exception

internal object FireChatErrors {

    val TAG by lazy { "FireChatErrors" }
    const val NO_SERVER_KEY_FOUND = "Please add a server key on firestore"
    const val OTHER_USERS_FCM_KEY_FOUND = "Other user's fcm key not found. Please use setMyFCM() function to set fcm"
    const val USER_ID_NULL = "User id can't be null or empty"
    const val BUILD_VARIANT_NULL = "Build variant is null"

    fun logErrors(error: String) {
        Timber.e(error)
    }

    fun crashIt(error: String) {
        throw Exception(error)
    }

}