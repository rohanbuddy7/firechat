package com.tribeone.firechat.ui.chatlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.MyApplication.Companion.chatlistFragmentVisible
import com.tribeone.firechat.databinding.FragmentChatlistBinding
import com.tribeone.firechat.di.component.FragmentComponent
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.model.UpdateChatList
import com.tribeone.firechat.ui.base.BaseFragment
import com.tribeone.firechat.ui.message.ChatViewModel
import com.tribeone.firechat.ui.main.MainActivity
import com.tribeone.firechat.ui.message.MessageFragment
import com.tribeone.firechat.utils.*
import com.tribeone.firechat.utils.Constants.FCM.FIRECHAT_MESSAGE_SEEN
import com.tribeone.firechat.utils.Constants.FCM.FIRECHAT_NEW_MESSAGE
import com.tribeone.firechat.utils.FireChatErrors.BUILD_VARIANT_NULL
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.collections.ArrayList

internal class ChatlistFragment : BaseFragment<ChatViewModel>(), ChatlistAdapter.OnClickListener {

    private var once: Boolean = true
    private var onceObserver: Boolean = true
    private var binding: FragmentChatlistBinding? = null
    public var adapter: ChatlistAdapter? = null
    private var userId: String? = null
    private var chatId: String? = null
    private lateinit var mMessageReceiver: BroadcastReceiver

    /*override fun provideLayoutId(): Int {
        return R.layout.fragment_chatlist
    }*/

    override fun injectDependencies(fragment: FragmentComponent) {
        fragment.inject(this)
    }

    override fun setBuildVariant(): String {
        if (FireChatHelper.buildVariants == null) {
            FireChatErrors.crashIt(BUILD_VARIANT_NULL)
        }
        return FireChatHelper.buildVariants!!
    }

    override fun onResume() {
        super.onResume()
        chatlistFragmentVisible = true
    }

    override fun onStop() {
        super.onStop()
        chatlistFragmentVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (binding == null) {
            binding = FragmentChatlistBinding.inflate(layoutInflater, container, false)
        }
        return binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun setupView() {
        if (once) {
            EventBus.getDefault().register(this)
            once = false
            chatId = arguments?.getString(Constants.Firestore.chatId)
            //userId = "7EWcbEpojdaY5A5ccWGR"//MyApplication.userId
            userId = MyApplication.userId

            initBroadcastAndOthers()

            adapter = ChatlistAdapter(requireContext(), this)
            binding?.rvChatlist?.adapter = adapter

            binding?.tvStartchat?.setOnClickListener {
                (activity as MainActivity).showChat(null)
            }

            binding?.ivChatListBack?.setOnClickListener {
                (requireActivity() as MainActivity).onBackPressed()
            }

            binding?.pullToRefresh?.setOnRefreshListener {
                adapter?.clearData()
                apiCall()
            }

            LocalBroadcastManager
                .getInstance(requireContext())
                .registerReceiver(mMessageReceiver, IntentFilter(Constants.FCM.FIRECHAT_BROADCAST));

            apiCall()

            if (FireChatHelper.buildVariants == FireChatHelper.staging) {
                binding?.tvStagingInfo?.visibility = View.VISIBLE
                binding?.tvStagingInfo?.text =
                    "userId = ${MyApplication.userId} -- ${MyApplication.user?.name}"
            } else {
                binding?.tvStagingInfo?.visibility = View.GONE
            }
        }

    }

    private fun apiCall() {
        userId?.let { userId ->
            binding?.progressCircular?.visibility = View.VISIBLE
            viewModel.getChatList(userId, null)
        } ?: kotlin.run {
            Toaster.show(requireContext(), "Userid not found")
        }
    }

    override fun setupObservers() {
        if (onceObserver) {
            onceObserver = false
            viewModel.chatListResponse.observe(this, Observer { chatlistresponse ->
                binding?.pullToRefresh?.isRefreshing = false
                binding?.progressCircular?.visibility = View.GONE
                if (chatlistresponse.size == 0) {
                    binding?.tvZeroState?.visibility = View.VISIBLE
                } else {
                    binding?.tvZeroState?.visibility = View.GONE
                    val sorted = chatlistresponse.sortedByDescending { it?.lastMessageAt }
                    val new = ArrayList<ChatListResponse?>()
                    new.addAll(sorted)
                    adapter?.addData(new)

                    val allParticipants = arrayListOf<String>()
                    for (i in sorted) {
                        i?.participants?.find { it != MyApplication.userId }?.let { user ->
                            allParticipants.add(user)
                        }
                    }
                    viewModel.getUserDetails(allParticipants)

                }
            })

            viewModel.singleChatUpdate.observe(this, Observer {
                Log.e("TAG", "setupObservers: redvel")
                adapter?.updateSingleChat(it)
            })

            viewModel.userDetails.observe(this, Observer {
                MyApplication.allUserDetails = it
                adapter?.notifyDataSetChanged()
                notificationRedirection()
            })

            /*viewModel.updatedSeenDataForThisChatBackPressed.observe(this, Observer {
                updateSeenDataLocally(it)
            })*/

            viewModel.decideAndMarkLastMessageAsRead.observe(this, Observer {
                if (it?.decision == true) {
                    it.seen?.let { seen ->
                        updateSeenDataLocally(it.chatId, seen)
                    }
                }
            })

        }
    }

    private fun notificationRedirection() {
        if (chatId != null) {
            val chatListResponse = adapter?.getData()?.find { it?.chatId == chatId }
            chatListResponse?.let {
                (activity as MainActivity).showChat(it)
                chatId = null
            }
        }
    }

    private fun updateSeenDataLocally(chatId: String?, seen: HashMap<String, String>) {
        chatId?.let {
            adapter?.updateOnlySeenValues(chatId, seen)
        }
    }

    override fun onClick(position: Int, chatListResponse: ChatListResponse?) {
        (activity as MainActivity).showChat(chatListResponse)
    }

    private fun initBroadcastAndOthers() {
        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // Get extra data included in the Intent
                when (intent.getStringExtra(Constants.FCMParams.action)) {
                    FIRECHAT_NEW_MESSAGE -> {
                        val chatId = intent.getStringExtra(Constants.FCMParams.chatId)
                        chatId?.let {
                            viewModel.getSingleChatData(chatId)
                        } ?: kotlin.run {
                            Toaster.show(requireContext(), "chatId is null")
                        }
                    }
                    FIRECHAT_MESSAGE_SEEN -> {
                        val _chatId = intent.getStringExtra(Constants.FCMParams.chatId)
                        val _userId = intent.getStringExtra(Constants.FCMParams.userId)
                        _chatId?.let {
                            _userId?.let {
                                val chatListResponse =
                                    adapter?.getData()?.find { it?.chatId == _chatId }
                                chatListResponse?.seen?.set(_userId.toString(), "0")
                                chatListResponse?.seen?.let {
                                    updateSeenDataLocally(_chatId, it)
                                }
                            } ?: kotlin.run {
                                Logger.debug("userId is null")
                            }
                        } ?: kotlin.run {
                            Logger.debug("chatId is null")
                        }
                    }
                }
            }
        }

        setFragmentResultListener(MessageFragment.MESSAGE_FRAGMENT_BACK) { reqKey, bundle ->
            if (reqKey == MessageFragment.MESSAGE_FRAGMENT_BACK) {
                val chatId = bundle.getString(Constants.Firestore.chatId)
                val lastMessageId = bundle.getString(Constants.Firestore.lastMessageId)
                markMessageAsRead(chatId)
                decideAndMarkLastMessageAsRead(chatId, lastMessageId)
                //viewModel.getUpdatedSeenData(chatId, MessageFragment.backPressed)
            }
        }
    }

    private fun decideAndMarkLastMessageAsRead(chatId: String?, lastMessageId: String?) {
        viewModel.decideAndMarkLastMessageAsRead(requireContext(), chatId, lastMessageId)
    }

    private fun markMessageAsRead(chatId: String?) {
        MyApplication.userId?.let {
            val chatListResponse = adapter?.getData()?.find { it?.chatId == chatId }
            if (chatListResponse?.seen?.get(MyApplication.userId!!) != "0") {
                chatListResponse?.seen?.set(MyApplication.userId!!, "0")
                chatListResponse?.let {
                    adapter?.updateSingleChat(it)
                }
                //viewModel.updateSeenValues(chatId, chatListResponse?.seen)
            } else {
                adapter?.notifyDataSetChanged()
            }
        }
    }

    @Subscribe
    fun update(updateChatList: UpdateChatList) {
        updateChatList.chatListResponse?.let {
            adapter?.updateSingleChat(it)
        }
    }

}