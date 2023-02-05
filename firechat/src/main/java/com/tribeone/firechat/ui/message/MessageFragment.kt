package com.tribeone.firechat.ui.message

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.MyApplication.Companion.messageFragmentVisible
import com.tribeone.firechat.databinding.FcFragmentChatBinding
import com.tribeone.firechat.di.component.FragmentComponent
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.model.Message
import com.tribeone.firechat.model.UpdateChatList
import com.tribeone.firechat.ui.base.BaseFragment
import com.tribeone.firechat.ui.chatlist.ChatlistFragment
import com.tribeone.firechat.ui.main.FcHomeActivity
import com.tribeone.firechat.utils.*

@SuppressLint("LogNotTimber")
internal class MessageFragment : BaseFragment<ChatViewModel>(),
    MessageAdapter.OnClickListener {

    companion object {
        val CHAT_LIST_RESPONSE = "chatListResponse"
        var sendMessage: String = "sendMessage"
        var backPressed: String = "backPressed"
        var MESSAGE_FRAGMENT_BACK: String = "messageFragmentBack"
    }

    private lateinit var mMessageReceiver: BroadcastReceiver
    private var startedNewChat: Boolean = false
    private var lastVisible: DocumentSnapshot? = null
    private var adapter: MessageAdapter? = null
    private var binding: FcFragmentChatBinding? = null
    private var chatId: String? = null
    private var hasMore = false
    private var isLoading = false
    private var chatListResponse: ChatListResponse? = null

    override fun onResume() {
        super.onResume()
        messageFragmentVisible = true
    }

    override fun onStop() {
        super.onStop()
        messageFragmentVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding == null) {
            binding = FcFragmentChatBinding.inflate(inflater, container, false)
        }
        return binding?.root
    }

    override fun setBuildVariant(): String {
        if (FireChatHelper.buildVariants == null) {
            FireChatErrors.crashIt(FireChatErrors.BUILD_VARIANT_NULL)
        }
        return FireChatHelper.buildVariants!!
    }

    override fun injectDependencies(fragment: FragmentComponent) {
        fragment.inject(this)
    }

    override fun setupView() {

        chatListResponse = arguments?.getParcelable<ChatListResponse>(CHAT_LIST_RESPONSE)

        initBroadcastAndOthers()

        val c = chatListResponse
        c?.participants?.let {
            for (i in it.indices) {
                if (MyApplication.userId != c.participants[i]) {
                    val name: String = ProfileUtils.getInstance().getName(c.participants[i])
                    val profilePic: String? =
                        ProfileUtils.getInstance().getProfilePicture(c.participants[i])

                    binding?.tvChatName?.text = name
                    binding?.ivChatProfileImage?.let {
                        Glide.with(this).load(profilePic).into(it)
                    }
                    MyApplication.otherUserId = c.participants[i]
                    break
                }
            }
        }

        //checking chatId
        if (chatListResponse?.chatId == null) {
            chatId = "chatId${System.currentTimeMillis()}"
            chatListResponse?.chatId = chatId
            chatListResponse?.messageType = Constants.Firestore.distinctType
            startedNewChat = true
            if (MyApplication.userId != null && MyApplication.otherUserId != null) {
                viewModel.checkIfUserAlreadyChatsAndGetChatId(
                    userId = MyApplication.userId!!,
                    otherUserid = MyApplication.otherUserId!!
                )
            }
        } else {
            chatId = chatListResponse?.chatId
            initOlderMessages()
        }


        adapter = MessageAdapter(requireContext(), this)
        val linear = LinearLayoutManager(requireContext(), GridLayoutManager.VERTICAL, true)
        binding?.rvChat?.layoutManager = linear
        binding?.rvChat?.adapter = adapter
        binding?.rvChat?.addOnScrollListener(object : PaginationScrollListener(linear) {
            override fun loadMoreItems() {
                if (hasMore) {
                    isLoading = true
                    apiCall()
                }
            }

            override fun isLastPage(): Boolean {
                return !hasMore
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })

        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter( Constants.FCM.FIRECHAT_BROADCAST));

        binding?.ivChatBack?.setOnClickListener {
            onbackPress()
        }

        binding?.tvSend?.setOnClickListener {
            //getUpdatedSeenData(chatId, sendMessage)
            sendMessage()
        }

        markMessageAsRead()

        onBackPressedDetectorForDialogFragment()

    }

    private fun onbackPress() {
        val resultBundle = Bundle().apply {
            putString(Constants.Firestore.chatId, chatId)
            val recentMessage = adapter?.getRecentlyAddedMessage()
            Logger.debug(recentMessage?.message?:"")
            putString(Constants.Firestore.lastMessageId, recentMessage?.messageId)
        }
        setFragmentResult(MESSAGE_FRAGMENT_BACK, resultBundle)
        (requireActivity() as FcHomeActivity).onBackPressed()
    }

    private fun sendMessage() {
        chatId?.let {
            val messageText = binding?.etMsg?.text.toString()
            val messageId = System.currentTimeMillis()
            val seenCount = calculateSeenCount(chatListResponse)
            val message = checkSendMessageCondition(messageText, messageId)
            val chatListUpdate = chatListUpdate(messageText, messageId, seenCount)
            if (message != null) {
                viewModel.sendNewMessage(chatId!!, message, chatListUpdate, seenCount)
            }
        } ?: kotlin.run {
            Toaster.show(requireContext(), "Chatid not found")
        }
    }

    private fun markMessageAsRead() {
        MyApplication.userId?.let {
            //if (chatListResponse?.seen?.get(MyApplication.userId!!) != "0") {
                chatListResponse?.seen?.set(MyApplication.userId!!, "0")
                viewModel.updateSeenValues(requireContext(), chatId, chatListResponse?.seen)
            //}
        }
    }

    private fun initOlderMessages() {
        chatId?.let {
            apiCall()
            viewModel.setNewMessagesListener(chatId!!)
        }
    }

    private fun apiCall() {
        viewModel.getMessage(
            chatId = chatId!!,
            lastVisible = lastVisible
        )
    }

    private fun checkSendMessageCondition(messageText: String, messageId: Long): Message? {
        if (binding?.etMsg?.text.isNullOrEmpty()) {
            return null
        } else {
            val message = Message(
                sorts = Constants.Firestore.distinctChat,
                messageId = messageId.toString(),
                type = Constants.Firestore.textTypeChat,
                read = false,
                message = messageText,
                timestamp = messageId,
                userId = MyApplication.userId,
            )
            binding?.etMsg?.text?.clear()
            //ModUtils.closeKeyboard(requireContext(), binding?.etMsg)
            return message
        }
    }

    private fun chatListUpdate(
        messageText: String,
        messageId: Long,
        seenCount: HashMap<String, String>
    ): ChatListResponse {
        return ChatListResponse(
            chatId = chatListResponse?.chatId,
            lastMessageAt = messageId,
            lastMessage = messageText,
            lastMessageBy = MyApplication.userId,
            messageId = messageId.toString(),
            messageType = chatListResponse?.messageType,
            participants = chatListResponse?.participants,
            startedBy = chatListResponse?.startedBy,
            seen = seenCount
        )
    }

    private fun calculateSeenCount(chatListResponse: ChatListResponse?): HashMap<String, String> {
        var otherUserSeenCount = 0
        val userSeenCount = 0
        chatListResponse?.let {
            if (it.messageType == Constants.Firestore.distinctType) {
                MyApplication.otherUserId?.let { otherUserId ->
                    val count = it.seen?.get(otherUserId)?.toInt() ?: 0
                    otherUserSeenCount = count + 1
                    chatListResponse.seen?.set(otherUserId, otherUserSeenCount.toString())
                }
            }
        }
        return hashMapOf(
            MyApplication.otherUserId.toString() to otherUserSeenCount.toString(),
            MyApplication.userId.toString() to userSeenCount.toString()
        )
    }

    override fun setupObservers() {

        viewModel.messageList.observe(this, Observer {
            isLoading = false
            if (it.isNotEmpty()) {
                hasMore = true
                for (i in it) {
                    Log.e("TAG", "setupObservers addData: ${i.message}")
                }
                adapter?.addData(it as ArrayList<Message>)
            } else {
                hasMore = false
            }
        })

        viewModel.addedNewMessageList.observe(this, Observer {
            if (it.isNotEmpty()) {
                for (i in it) {
                    Log.e("TAG", "setupObservers addNewMessageList: ${i.message}")
                }
                adapter?.addNewMessageList(it as ArrayList<Message>)
                binding?.rvChat?.scrollToPosition(0)
            }
        })

        viewModel.messageLastVisible.observe(this, Observer {
            lastVisible = it
        })

        viewModel.sendNewMessage.observe(this, Observer { messageAndSeen ->
            MyApplication.otherUserId?.let { otheruserid ->
                chatId?.let { chatid ->
                    viewModel.checkFcmAndDeliverMessage(
                        context = requireContext(),
                        otherUserid = otheruserid,
                        chatId = chatid,
                        message = messageAndSeen.message?.message ?: "",
                        unseenCount = messageAndSeen.seen?.get(otheruserid).toString()
                    )
                }
            }

            //updating chat list
            messageAndSeen.chatListResponse?.let {
                org.greenrobot.eventbus.EventBus.getDefault().post(UpdateChatList(it))
            }

            if (startedNewChat) {
                chatId?.let {
                    initOlderMessages()
                    viewModel.setNewChatForUsers(
                        participants = chatListResponse?.participants ?: arrayListOf(),
                        chatId = chatId!!,
                        lastMessageAt = hashMapOf(
                            "lastMessageAt" to (chatListResponse?.lastMessageAt ?: 0)
                        )
                    )
                } ?: kotlin.run {
                    Toaster.show(requireContext(), "Chatid not found")
                }
            }
        })

        viewModel.userAlreadyExistsHereIsChatId.observe(this, Observer {
            chatId = it
            initOlderMessages()
        })

        viewModel.updatedSeenDataForThisChatSendMessage.observe(this, Observer {
            if(it == null) {
                sendMessage()
            } else {
                updateSeenDataLocally(it)
                sendMessage()
            }
        })

    }

    private fun updateSeenDataLocally(seen: HashMap<String,String>){
        chatListResponse?.seen = seen
        try {
            chatListResponse?.let {
                (parentFragment as ChatlistFragment).adapter?.updateSingleChat(it)
            }
        } catch (e: Exception) {
            Logger.error(e.toString())
        }
    }

    private fun getUpdatedSeenData(chatId: String?, requirement: String) {
        viewModel.getUpdatedSeenData(chatId, requirement)
    }

    override fun onClick(position: Int, data: String) {

    }

    fun onBackPressedDetectorForDialogFragment(){
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            Log.i(tag, "keyCode: $keyCode")
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event?.action != KeyEvent.ACTION_DOWN) {
                    true;
                } else {
                    onbackPress()
                    true;
                }
            } else {
                false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        markMessageAsRead()
    }

    private fun initBroadcastAndOthers() {
        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // Get extra data included in the Intent
                when (intent.getStringExtra(Constants.FCMParams.action)) {
                    Constants.FCM.FIRECHAT_MESSAGE_SEEN -> {
                        val _chatId = intent.getStringExtra(Constants.FCMParams.chatId)
                        val _userId = intent.getStringExtra(Constants.FCMParams.userId)
                        _chatId?.let {
                            _userId?.let {
                                if(_chatId == chatId){
                                    chatListResponse?.seen?.set(_userId.toString(), "0")
                                    chatListResponse?.seen?.let {
                                        updateSeenDataLocally(it)
                                    }
                                }
                            }?: kotlin.run {
                                Logger.debug("userId is null")
                            }
                        } ?: kotlin.run {
                            Logger.debug("chatId is null")
                        }
                    }
                }
            }
        }
    }


    }