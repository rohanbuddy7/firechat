package com.tribeone.firechat.ui.message

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import com.tribeone.firechat.FcMyApplication
import com.tribeone.firechat.di.Request.*
import com.tribeone.firechat.di.network.FcNetworkService
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.model.Message
import com.tribeone.firechat.model.SeenAndDecision
import com.tribeone.firechat.model.Users
import com.tribeone.firechat.ui.base.FcBaseViewModel
import com.tribeone.firechat.utils.FcConstants
import com.tribeone.firechat.utils.FcConstants.FCM.FIRECHAT_MESSAGE_SEEN
import com.tribeone.firechat.utils.FcConstants.FCM.FIRECHAT_NEW_MESSAGE
import com.tribeone.firechat.utils.FireChatErrors
import com.tribeone.firechat.utils.FireChatErrors.NO_SERVER_KEY_FOUND
import com.tribeone.firechat.utils.FireChatErrors.OTHER_USERS_FCM_KEY_FOUND
import com.tribeone.firechat.utils.FcLogger
import com.tribeone.firechat.utils.FcModUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import kotlin.random.Random

@SuppressLint("LogNotTimber")
//@Inject constructor
internal class FcChatViewModel(
    private val buildVariant: String,
    private val fcNetworkService: FcNetworkService,
) : FcBaseViewModel() {

    val loader: MutableLiveData<Boolean> = MutableLiveData()
    val messageList: MutableLiveData<List<Message>> = MutableLiveData()
    val addedNewMessageList: MutableLiveData<List<Message>> = MutableLiveData()
    val messageLastVisible: MutableLiveData<DocumentSnapshot> = MutableLiveData()
    val chatListResponse: MutableLiveData<ArrayList<ChatListResponse?>> = MutableLiveData()
    val sendNewMessage: MutableLiveData<MessageAndSeen> = MutableLiveData()
    val singleChatUpdate: MutableLiveData<ChatListResponse> = MutableLiveData()
    val userAlreadyExistsHereIsChatId: MutableLiveData<String> = MutableLiveData()
    val userDetails: MutableLiveData<HashMap<String, Users>> = MutableLiveData()
    val decideAndMarkLastMessageAsRead: MutableLiveData<SeenAndDecision?> = MutableLiveData()
    val updatedSeenDataForThisChatSendMessage: MutableLiveData<HashMap<String, String>?> =
        MutableLiveData()

    fun setNewMessagesListener(chatid: String) {
        var firsttime = false
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection(FcModUtils.getBuildVariantMessage(buildVariant))
            .document(chatid)
            .collection(FcConstants.Firestore.message)
            .orderBy(FcConstants.Firestore.timestamp, Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener(object : EventListener<QuerySnapshot?> {
                override fun onEvent(snapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (e != null) {
                        FcLogger.debug("error: $e")
                        return
                    }

                    val messages = arrayListOf<Message>()
                    snapshots?.let {
                        for (i in snapshots.documentChanges.indices) {
                            val dc = snapshots.documentChanges[i]
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val m = dc.document.toObject(Message::class.java)
                                    messages.add(m)
                                    if (i == snapshots.documentChanges.size - 1) {
                                        if (firsttime) {
                                            addedNewMessageList.postValue(messages)
                                        }
                                        firsttime = true
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    FcLogger.debug("modified ${dc.document}")
                                }
                                DocumentChange.Type.REMOVED -> {
                                    FcLogger.debug("removed ${dc.document}")
                                }
                            }
                        }
                    }
                }
            })
    }

    fun getMessage(chatId: String, lastVisible: DocumentSnapshot?) {
        pagination(chatId, lastVisible)
    }

    private fun pagination(chatId: String, lastVisibleDocument: DocumentSnapshot?) {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        var query: Query? = null
        if (lastVisibleDocument == null) {
            query = db.collection(FcModUtils.getBuildVariantMessage(buildVariant))
                .document(chatId)
                .collection(FcConstants.Firestore.message)
                .orderBy(FcConstants.Firestore.timestamp, Query.Direction.DESCENDING)
        } else {
            query = db.collection(FcModUtils.getBuildVariantMessage(buildVariant))
                .document(chatId)
                .collection(FcConstants.Firestore.message)
                .orderBy(FcConstants.Firestore.timestamp, Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
        }

        query.limit(10)
            .get()
            .addOnSuccessListener {
                val messages = arrayListOf<Message>()
                for (dc in it.documents) {
                    val m = dc.toObject(Message::class.java)
                    m?.let {
                        messages.add(m)
                    }
                }
                if (it.documents.size > 0) {
                    val newLastVisible: DocumentSnapshot =
                        it.documents[it.size() - 1]//.get("timestamp") as Long
                    messageLastVisible.postValue(newLastVisible)
                }
                messageList.postValue(messages)
                Log.e("TAG", "pagination: $messages")
            }
            .addOnFailureListener {
                Log.e("TAG", "pagination: $it")
            }
    }

    fun getChatList(userid: String, lastVisible: String?) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        var query: Query? = null
        query = if (lastVisible == null) {
            db.collection(FcModUtils.getBuildVariantUsers(buildVariant))
                .document(userid)
                .collection(FcConstants.Firestore.chatlist)
                .orderBy(FcConstants.Firestore.lastMessageAt, Query.Direction.DESCENDING)
        } else {
            db.collection(FcModUtils.getBuildVariantUsers(buildVariant))
                .document(userid)
                .collection(FcConstants.Firestore.chatlist)
                .orderBy(FcConstants.Firestore.lastMessageAt, Query.Direction.DESCENDING)
                .startAfter(lastVisible)
        }
        query
            .limit(50)
            .get()
            .addOnSuccessListener { chatsQuerySnapshot ->
                val chats = arrayListOf<ChatListResponse?>()
                val users = arrayListOf<String>()

                if (chatsQuerySnapshot.documents.size == 0) {
                    chatListResponse.postValue(chats)
                    return@addOnSuccessListener
                }

                for (i in chatsQuerySnapshot.documents.indices) {
                    val dc = chatsQuerySnapshot.documents[i]
                    val id = dc.id
                    Log.e("TAG", "getChatList 1: $id")
                    val lastMessageAt = dc.get(FcConstants.Firestore.lastMessageAt)

                    lastMessageAt?.let {

                        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                        db.collection(FcModUtils.getBuildVariantMessage(buildVariant))
                            .document(id)
                            .get()
                            .addOnSuccessListener { chatDetailsDocSnapshot ->

                                try {
                                    val lastMessage =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.lastMessage)
                                    val lastMessageAt =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.lastMessageAt)
                                    val lastMessageBy =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.lastMessageBy)
                                    val messageId =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.messageId)
                                    val messageType =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.messageType)
                                    val participants =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.participants) as ArrayList<String>?
                                    val startedBy =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.startedBy)
                                    val seen =
                                        chatDetailsDocSnapshot.get(FcConstants.Firestore.seen) as HashMap<String, String>?
                                    if (messageId != null) {
                                        val chatList = ChatListResponse(
                                            chatId = id,
                                            lastMessageAt = lastMessageAt?.toString()?.toLong(),
                                            lastMessage = lastMessage?.toString(),
                                            lastMessageBy = lastMessageBy?.toString(),
                                            messageId = messageId.toString(),
                                            messageType = messageType?.toString(),
                                            participants = participants,
                                            startedBy = startedBy?.toString(),
                                            seen = seen
                                        )
                                        Log.e(
                                            "TAG",
                                            "getChatList 2: ${chatList.chatId} ---> ${chatList.participants}",
                                        )
                                        chats.add(chatList)

                                        //val otherParticipants

                                        if (i == chatsQuerySnapshot.documents.size - 1) {
                                            chatListResponse.postValue(chats)
                                        }
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .addOnFailureListener {
                                Timber.e("TAG getChatList failure: $it")
                            }

                    }
                }

                loader.postValue(false)
            }.addOnFailureListener {
                Log.e("TAG", "getChatList: $it")
                loader.postValue(false)
            }
    }


    fun sendNewMessage(
        chatId: String,
        message: Message,
        chatListResponse: ChatListResponse,
        seen: HashMap<String, String>
    ) {
        val messageId = System.currentTimeMillis().toString()
        loader.postValue(true)

        FirebaseFirestore.getInstance()
            .collection(FcModUtils.getBuildVariantMessage(buildVariant))
            .document(chatId)
            .collection(FcConstants.Firestore.message)
            .document(messageId)
            .set(message)
            .addOnSuccessListener(OnSuccessListener<Void?> {
                FirebaseFirestore.getInstance()
                    .collection(FcModUtils.getBuildVariantMessage(buildVariant))
                    .document(chatId)
                    .set(chatListResponse)
                    .addOnSuccessListener(OnSuccessListener<Void?> {
                        updateTimeInChatlist(
                            participants = chatListResponse.participants ?: arrayListOf(),
                            chatId = chatId,
                            lastMessageAt = hashMapOf(
                                FcConstants.Firestore.lastMessageAt to (chatListResponse.lastMessageAt
                                    ?: 0)
                            )
                        )
                        sendNewMessage.postValue(
                            MessageAndSeen(
                                message = message,
                                seen = seen,
                                chatListResponse = chatListResponse
                            )
                        )
                        loader.postValue(false)
                    }).addOnFailureListener(OnFailureListener {
                        loader.postValue(false)
                    })
            }).addOnFailureListener(OnFailureListener {
                FcLogger.debug("onFailure: data add failed ")
                loader.postValue(false)
            })
    }

    private fun updateTimeInChatlist(
        participants: List<String>,
        chatId: String,
        lastMessageAt: HashMap<String, Long>
    ) {
        for (i in participants) {
            FirebaseFirestore.getInstance()
                .collection(FcModUtils.getBuildVariantUsers(buildVariant))
                .document(i)
                .collection(FcConstants.Firestore.chatlist)
                .document(chatId)
                .set(lastMessageAt)
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    FcLogger.debug("onSuccess: data added ")
                }).addOnFailureListener(OnFailureListener {
                    FcLogger.debug("onFailed: data addition fail ")
                })
        }
    }

    fun getSingleChatData(chatId: String) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection(FcModUtils.getBuildVariantMessage(buildVariant))
            .document(chatId)
            .get()
            .addOnSuccessListener {
                try {
                    val lastMessage = it.get(FcConstants.Firestore.lastMessage)
                    val lastMessageAt = it.get(FcConstants.Firestore.lastMessageAt)
                    val lastMessageBy = it.get(FcConstants.Firestore.lastMessageBy)
                    val messageId = it.get(FcConstants.Firestore.messageId)
                    val messageType = it.get(FcConstants.Firestore.messageType)
                    val participants =
                        it.get(FcConstants.Firestore.participants) as ArrayList<String>?
                    val startedBy = it.get(FcConstants.Firestore.startedBy)
                    val seen = it.get(FcConstants.Firestore.seen) as HashMap<String, String>?
                    if (messageId != null) {
                        val chatList = ChatListResponse(
                            chatId = chatId,
                            lastMessageAt = lastMessageAt?.toString()?.toLong(),
                            lastMessage = lastMessage?.toString(),
                            lastMessageBy = lastMessageBy?.toString(),
                            messageId = messageId.toString(),
                            messageType = messageType?.toString(),
                            participants = participants,
                            startedBy = startedBy?.toString(),
                            seen = seen
                        )
                        singleChatUpdate.postValue(chatList)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    fun sendFCM(
        context: Context,
        serverKey: String,
        requestNotificaton: RequestNotificaton
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            val result = fcNetworkService.sendFCM(
                sessionToken = "key=$serverKey",
                contentType = "application/json",
                requestNotificaton = requestNotificaton
            )
            if (result != null) {
                Log.d("TAG: result ", result.body().toString())
            }
        }
    }


    fun checkFcmAndDeliverMessage(
        context: Context,
        otherUserid: String,
        chatId: String,
        message: String,
        unseenCount: String,
    ) {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection(FcConstants.Firestore.key)
            .document(FcConstants.Firestore.cloud)
            .get()
            .addOnSuccessListener {

                val serverKey = it.get(FcConstants.Firestore.serverKey)

                if (serverKey != null) {

                    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                    db.collection(FcModUtils.getBuildVariantUsers(buildVariant))
                        .document(otherUserid)
                        .get()
                        .addOnSuccessListener {
                            try {
                                val otherUserFcm = it.get(FcConstants.Firestore.fcm)
                                if (otherUserFcm != null) {
                                    val requestNotification = RequestNotificaton(
                                        registration_ids = arrayOf(otherUserFcm.toString()),
                                        data = Data(
                                            type = FIRECHAT_NEW_MESSAGE,
                                            request_uuid = Random.nextInt(100).toString(),
                                            data = InternalData(
                                                title = "New Message",
                                                message = message,
                                                action = FIRECHAT_NEW_MESSAGE,
                                                chatId = chatId,
                                                seen = unseenCount
                                            )
                                        )
                                    )
                                    sendFCM(
                                        context = context,
                                        serverKey = serverKey.toString(),
                                        requestNotificaton = requestNotification
                                    )
                                } else {
                                    FireChatErrors.logErrors(OTHER_USERS_FCM_KEY_FOUND)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .addOnFailureListener {
                            FireChatErrors.logErrors(OTHER_USERS_FCM_KEY_FOUND)
                        }
                } else {
                    FireChatErrors.logErrors(NO_SERVER_KEY_FOUND)
                }
            }.addOnFailureListener {
                FireChatErrors.logErrors(NO_SERVER_KEY_FOUND)
            }
    }

    fun markMessageSeenFCM(
        context: Context,
        otherUserid: String,
        chatId: String,
        unseenCount: String,
    ) {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection(FcConstants.Firestore.key)
            .document(FcConstants.Firestore.cloud)
            .get()
            .addOnSuccessListener {

                val serverKey = it.get(FcConstants.Firestore.serverKey)
                if (serverKey != null) {

                    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                    db.collection(FcModUtils.getBuildVariantUsers(buildVariant))
                        .document(otherUserid)
                        .get()
                        .addOnSuccessListener {
                            try {

                                val otherUserFcm = it.get(FcConstants.Firestore.fcm)
                                if (otherUserFcm != null) {
                                    val requestNotification = RequestNotificaton(
                                        registration_ids = arrayOf(otherUserFcm.toString()),
                                        data = Data(
                                            type = FIRECHAT_MESSAGE_SEEN,
                                            request_uuid = Random.nextInt(100).toString(),
                                            data = InternalData(
                                                action = FIRECHAT_MESSAGE_SEEN,
                                                chatId = chatId,
                                                seen = unseenCount,
                                                userId = FcMyApplication.userId
                                            )
                                        )
                                    )
                                    sendFCM(
                                        context = context,
                                        serverKey = serverKey.toString(),
                                        requestNotificaton = requestNotification
                                    )
                                } else {
                                    FireChatErrors.logErrors(OTHER_USERS_FCM_KEY_FOUND)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .addOnFailureListener {
                            FireChatErrors.logErrors(OTHER_USERS_FCM_KEY_FOUND)
                        }
                } else {
                    FireChatErrors.logErrors(NO_SERVER_KEY_FOUND)
                }
            }
    }

    fun setNewChatForUsers(
        participants: List<String>,
        chatId: String,
        lastMessageAt: HashMap<String, Long>
    ) {
        for (m in participants) {
            FirebaseFirestore.getInstance()
                .collection(FcModUtils.getBuildVariantUsers(buildVariant))
                .document(m)
                .collection(FcConstants.Firestore.chatlist)
                .document(chatId)
                .set(lastMessageAt)
                .addOnSuccessListener {
                    Log.e("TAG", "setNewChatForUsers: success")
                }
                .addOnFailureListener {
                    Log.e("TAG", "setNewChatForUsers: failed")
                }
        }
    }

    fun checkIfUserAlreadyChatsAndGetChatId(userId: String, otherUserid: String) {
        FirebaseFirestore.getInstance()
            .collection(FcModUtils.getBuildVariantUsers(buildVariant))
            .document(userId)
            .collection(FcConstants.Firestore.chatlist)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var gotIt = false
                for (i in querySnapshot.documents.indices) {
                    val dc = querySnapshot.documents[i]
                    val id = dc.id

                    FirebaseFirestore.getInstance()
                        .collection(FcModUtils.getBuildVariantMessage(buildVariant))
                        .document(id)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            try {
                                val chatId = snapshot.get(FcConstants.Firestore.chatId).toString()
                                val participants =
                                    snapshot.get(FcConstants.Firestore.participants) as ArrayList<String>?
                                for (p in participants ?: arrayListOf()) {
                                    if (p == otherUserid) {
                                        userAlreadyExistsHereIsChatId.postValue(chatId)
                                        gotIt = true
                                        break
                                    }
                                }

                            } catch (e: Exception) {
                            }
                        }
                        .addOnFailureListener {
                            Log.e("TAG", "checkIfUserAlreadyExistsAndLoadChat: $it")
                        }
                    if (gotIt) {
                        break
                    }
                }
            }
            .addOnFailureListener {
                Log.e("TAG", "checkIfUserAlreadyExistsAndLoadChat: $it")
            }
    }

    fun getUserDetails(participants: List<String>) {
        val ud = hashMapOf<String, Users>()
        for (i in participants) {
            if (FcMyApplication.allUserDetails[i]?.name == null
                || FcMyApplication.allUserDetails[i]?.profilePicture == null
            ) {
                FirebaseFirestore.getInstance()
                    .collection(FcModUtils.getBuildVariantUsers(buildVariant))
                    .document(i)
                    .get()
                    .addOnSuccessListener { userDocSnapchat ->

                        val id = userDocSnapchat.get(FcConstants.Firestore.id)?.toString()
                        val name = userDocSnapchat.get(FcConstants.Firestore.name)?.toString()
                        val profilePicture =
                            userDocSnapchat.get(FcConstants.Firestore.profilePicture)?.toString()

                        ud[i] = Users(
                            id = id,
                            name = name,
                            profilePicture = profilePicture
                        )

                        if (i == participants[participants.lastIndex]) {
                            userDetails.postValue(ud)
                        }

                    }
                    .addOnFailureListener {
                        FcLogger.error("getUserDetails: failed")
                    }
            } else {
                ud[i] = FcMyApplication.allUserDetails[i] as Users

                if (i == participants[participants.lastIndex]) {
                    userDetails.postValue(ud)
                }
            }
        }

    }

    fun decideAndMarkLastMessageAsRead(context: Context, chatId: String?, lastMessageId: String?) {
        chatId?.let {
            FirebaseFirestore.getInstance()
                .collection(FcModUtils.getBuildVariantMessage(buildVariant))
                .document(chatId)
                .get()
                .addOnSuccessListener {
                    val messageId = it.get(FcConstants.Firestore.messageId)
                    val seen = it.get(FcConstants.Firestore.seen) as HashMap<String, String>
                    if (lastMessageId == messageId) {
                        seen[FcMyApplication.userId.toString()] = "0"
                        updateSeenValues(context, chatId, seen, true)
                    } else {
                        decideAndMarkLastMessageAsRead.postValue(null)
                    }
                }
                .addOnFailureListener {
                    decideAndMarkLastMessageAsRead.postValue(null)
                }
        }
    }

    fun updateSeenValues(
        context: Context,
        chatId: String?,
        seen: HashMap<String, String>?,
        decideCallback: Boolean = false
    ) {
        chatId?.let {
            FcMyApplication.userId?.let {
                FirebaseFirestore.getInstance()
                    .collection(FcModUtils.getBuildVariantMessage(buildVariant))
                    .document(chatId)
                    .update(FcConstants.Firestore.seen, seen)
                    .addOnSuccessListener {
                        if (decideCallback) {
                            decideAndMarkLastMessageAsRead.postValue(
                                SeenAndDecision(
                                    decision = true,
                                    seen = seen,
                                    chatId = chatId
                                )
                            )
                        } else {
                            FcMyApplication.otherUserId?.let { otheruserid ->
                                markMessageSeenFCM(
                                    context = context,
                                    otherUserid = otheruserid,
                                    chatId = chatId,
                                    unseenCount = seen?.get(otheruserid).toString()
                                )
                            }
                        }

                        Log.e("TAG", "markMessageAsRead: success")
                    }
                    .addOnFailureListener {
                        Log.e("TAG", "markMessageAsRead: failed")
                    }
            }
        }
    }

    fun getUpdatedSeenData(chatId: String?, requirement: String) {
        chatId?.let {
            FcMyApplication.userId?.let {
                FirebaseFirestore.getInstance()
                    .collection(FcModUtils.getBuildVariantMessage(buildVariant))
                    .document(chatId)
                    .get()
                    .addOnSuccessListener {
                        try {
                            when (requirement) {
                                FcMessageFragment.sendMessage -> {
                                    updatedSeenDataForThisChatSendMessage
                                        .postValue(it.get(FcConstants.Firestore.seen) as HashMap<String, String>)
                                }
                            }
                        } catch (e: Exception) {
                            FcLogger.error(e.message.toString())
                            updatedSeenDataForThisChatSendMessage.postValue(null)
                        }
                        Log.e("TAG", "markMessageAsRead: success")
                    }
                    .addOnFailureListener {
                        Log.e("TAG", "markMessageAsRead: failed")
                    }
            }
        }
    }

}
