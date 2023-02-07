package com.tribeone.firechat.utils

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.tribeone.firechat.model.Users
import com.tribeone.firechat.ui.main.FcHomeActivityFc

class FireChatHelper private constructor() {

    companion object {
        const val staging = "staging"
        const val production = "production"
        private var fireChatHelper: FireChatHelper? = null
        var buildVariants: String? = null

        fun getInstance(buildVariants: String): FireChatHelper {
            if (fireChatHelper == null) {
                fireChatHelper = FireChatHelper()
            }
            this.buildVariants = buildVariants
            return fireChatHelper!!
        }
    }

    public var onMessageReceivedListener: OnMessageReceivedListener? = null
    /*private var _buildVariants: String? = null
    var buildVariants: String?
        get() = _buildVariants
        set(value) {
            _buildVariants = value
        }*/

    fun openChats(user: Users, activity: Activity, chatId: String? = null) {
        if (validate(activity)) {
            if(user.id.isNullOrEmpty()){
                FireChatErrors.crashIt(FireChatErrors.USER_ID_NULL)
            }
            FcHomeActivityFc.openChat(user, activity, chatId)
        }
    }

    fun startDistinctConversation(user: Users, otherUserId: String, activity: Activity) {
        if (validate(activity)) {
            if(user.id.isNullOrEmpty()){
                FireChatErrors.crashIt(FireChatErrors.USER_ID_NULL)
            }
            FcHomeActivityFc.startDistinctConversation(user, otherUserId, activity)
        }
    }

    private fun validate(activity: Activity): Boolean {
        return if (buildVariants != null && (buildVariants == staging || buildVariants == production)) {
            true
        } else {
            Toast.makeText(activity, "Please add a correct build variant", Toast.LENGTH_SHORT)
                .show()
            false
        }
    }

    fun setMyDataAndFCM(userid: String, fcmToken: String, user: Users? ) {
        user?.let {
            user.id?.apply {
                val data = hashMapOf<String, String>(
                    Constants.Firestore.id to user.id,
                    Constants.Firestore.name to (user.name ?: ""),
                    Constants.Firestore.profilePicture to (user.profilePicture ?: ""),
                    Constants.Firestore.fcm to fcmToken
                )
                FirebaseFirestore.getInstance()
                    .collection(ModUtils.getBuildVariantUsers(buildVariants))
                    .document(userid)
                    .set(data)
                    .addOnSuccessListener(OnSuccessListener<Void?> {
                        Logger.debug("onSuccess: data addition success ")
                    }).addOnFailureListener(OnFailureListener {
                        Logger.debug("onFailure: data add failed ")
                    })
            }
        }
    }

    fun setNotificationListener(o: OnMessageReceivedListener){
        onMessageReceivedListener = o
    }

    interface OnMessageReceivedListener{
        fun onMessageReceived(
            title: String,
            message: String,
            unseenCount: String,
            chatId: String,
            action: String,
        )
    }

}