package com.tribeone.firechat.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.tribeone.firechat.FcMyApplication
import com.tribeone.firechat.R
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.model.Users
import com.tribeone.firechat.ui.base.FcBaseActivity
import com.tribeone.firechat.ui.message.FcMessageFragmentFc.Companion.CHAT_LIST_RESPONSE
import com.tribeone.firechat.utils.FcConstants
import com.tribeone.firechat.utils.FcConstants.Firestore.userid
import com.tribeone.firechat.utils.FireChatErrors
import com.tribeone.firechat.utils.FireChatErrors.BUILD_VARIANT_NULL
import com.tribeone.firechat.utils.FireChatHelper


internal class FcHomeActivityFc : FcBaseActivity() {

    companion object {

        fun openChat(user: Users, activity: Activity, chatId: String? = null) {
            val intent = Intent(activity, FcHomeActivityFc::class.java)
            val bundle = Bundle()
            bundle.putParcelable(FcConstants.Firestore.user, user)
            bundle.putString(FcConstants.Firestore.chatId, chatId)
            bundle.putString(FcConstants.Firestore.type, FcConstants.Firestore.typeOpenChat)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }

        fun startDistinctConversation(user: Users, otherUserId: String, activity: Activity) {
            val intent = Intent(activity, FcHomeActivityFc::class.java)
            val bundle = Bundle()
            bundle.putParcelable(FcConstants.Firestore.user, user)
            bundle.putString(FcConstants.Firestore.otherUserId, otherUserId)
            bundle.putString(FcConstants.Firestore.type, FcConstants.Firestore.typeStartNewConversation)
            intent.putExtras(bundle)
            activity.startActivity(intent, bundle)
        }
    }

    private var user: Users? = null

    //private var otheruser: Users? = null
    private var type: String? = null
    private var userId: String? = null
    private var chatId: String? = null
    private var otherUserId: String? = null
    private var navHostFragment: NavHostFragment? = null
    private var navController: NavController? = null

    override fun provideLayoutId(): Int {
        return R.layout.fc_activity_main
    }

    override fun setupView() {
        user = intent.getParcelableExtra(FcConstants.Firestore.user)
        chatId = intent.getStringExtra(FcConstants.Firestore.chatId)
        userId = user?.id
        otherUserId = intent.getStringExtra(FcConstants.Firestore.otherUserId)
        type = intent.getStringExtra(FcConstants.Firestore.type)

        FirebaseApp.initializeApp(this)

        val x = if(supportFragmentManager == null) {
            "1 null"
        } else {
            "1 notnull"
        }

        val y = if(supportFragmentManager.findFragmentById(R.id.fl_root_container) == null) {
            "2 null"
        } else {
            "2 notnull"
        }

        Log.e("TAG", "setupViewzomb1: $x", )
        Log.e("TAG", "setupViewzomb2: $y", )
        Log.e("TAG", "abcde: $y", )

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fl_root_container) as NavHostFragment
        navController = navHostFragment?.navController

        FcMyApplication.userId = userId
        FcMyApplication.user = user

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "${task.exception}", Toast.LENGTH_LONG).show()
                return@OnCompleteListener
            }

            // Get FCM registration token
            val token = task.result
            if (token != null) {
                FcMyApplication.userId?.let {
                    FcMyApplication.userFCM = token
                    if (FireChatHelper.buildVariants == null) {
                        FireChatErrors.crashIt(BUILD_VARIANT_NULL)
                    }
                    FireChatHelper.getInstance(FireChatHelper.buildVariants!!)
                        .setMyDataAndFCM(FcMyApplication.userId!!, token, user)
                }
            }
        })

        //showChat(null)
        val args = Bundle()
        args.putString(FcConstants.Firestore.userId, userid)
        navController?.setGraph(R.navigation.fc_navigation, args)


        when (type) {
            FcConstants.Firestore.typeOpenChat -> {
                showChatList()
            }
            FcConstants.Firestore.typeStartNewConversation -> {
                FcMyApplication.otherUserId = otherUserId
                showChat(null)
            }
        }

    }

    private fun showChatList() {
        val args = Bundle()
        args.putString(FcConstants.Firestore.userId, userid)
        args.putString(FcConstants.Firestore.chatId, chatId)
        navController?.navigate(R.id.chatlistFragment, args)
    }

    fun showChat(chatListResponse: ChatListResponse?) {
        val chat = startNewChatOrOpenChat(chatListResponse)
        val args = Bundle()
        args.putParcelable(CHAT_LIST_RESPONSE, chat)
        navController?.navigate(R.id.messageFragment, args)
    }

    private fun startNewChatOrOpenChat(c: ChatListResponse?): ChatListResponse {
        return if (c == null) {
            val otherUserId = FcMyApplication.otherUserId ?: ""
            ChatListResponse(
                chatId = null,
                lastMessageAt = System.currentTimeMillis(),
                lastMessage = null,
                lastMessageBy = null,
                messageId = null,
                messageType = null,
                participants = arrayListOf(FcMyApplication.userId ?: "", otherUserId),
                startedBy = FcMyApplication.userId,
                seen = hashMapOf(
                    (FcMyApplication.userId ?: "") to "0",
                    otherUserId to "0"
                ),
            )
        } else {
            return c
        }
    }

    override fun onBackPressed() {
        if (navController?.currentDestination?.id == R.id.chatlistFragment) {
            super.onBackPressed()
        } else {
            navController?.navigateUp()
        }
    }

}