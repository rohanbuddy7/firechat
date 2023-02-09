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
import com.tribeone.firechat.FcMyApplication
import com.tribeone.firechat.FcMyApplication.Companion.chatlistFragmentVisible
import com.tribeone.firechat.databinding.FcFragmentChatlistBinding
import com.tribeone.firechat.di.component.FcFragmentComponent
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.model.UpdateChatList
import com.tribeone.firechat.ui.base.FcBaseFragment
import com.tribeone.firechat.ui.message.FcChatViewModel
import com.tribeone.firechat.ui.main.FcHomeActivity
import com.tribeone.firechat.ui.message.FcMessageFragment
import com.tribeone.firechat.utils.*
import com.tribeone.firechat.utils.FcConstants.FCM.FIRECHAT_MESSAGE_SEEN
import com.tribeone.firechat.utils.FcConstants.FCM.FIRECHAT_NEW_MESSAGE
import com.tribeone.firechat.utils.FireChatErrors.BUILD_VARIANT_NULL
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.collections.ArrayList

internal class FcChatlistFragment : FcBaseFragment<FcChatViewModel>(), FcChatlistAdapter.OnClickListener {

    private var once: Boolean = true
    private var onceObserver: Boolean = true
    private var binding: FcFragmentChatlistBinding? = null
    public var adapterFc: FcChatlistAdapter? = null
    private var userId: String? = null
    private var chatId: String? = null
    private lateinit var mMessageReceiver: BroadcastReceiver

    /*override fun provideLayoutId(): Int {
        return R.layout.fragment_chatlist
    }*/

    override fun injectDependencies(fcFragment: FcFragmentComponent) {
        fcFragment.inject(this)
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
            binding = FcFragmentChatlistBinding.inflate(layoutInflater, container, false)
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
            chatId = arguments?.getString(FcConstants.Firestore.chatId)
            //userId = "7EWcbEpojdaY5A5ccWGR"//MyApplication.userId
            userId = FcMyApplication.userId

            initBroadcastAndOthers()

            adapterFc = FcChatlistAdapter(requireContext(), this)
            binding?.rvChatlist?.adapter = adapterFc

            binding?.tvStartchat?.setOnClickListener {
                (activity as FcHomeActivity).showChat(null)
            }

            binding?.ivChatListBack?.setOnClickListener {
                (requireActivity() as FcHomeActivity).onBackPressed()
            }

            binding?.pullToRefresh?.setOnRefreshListener {
                adapterFc?.clearData()
                apiCall()
            }

            LocalBroadcastManager
                .getInstance(requireContext())
                .registerReceiver(mMessageReceiver, IntentFilter(FcConstants.FCM.FIRECHAT_BROADCAST));

            apiCall()

            if (FireChatHelper.buildVariants == FireChatHelper.staging) {
                binding?.tvStagingInfo?.visibility = View.VISIBLE
                binding?.tvStagingInfo?.text =
                    "userId = ${FcMyApplication.userId} -- ${FcMyApplication.user?.name}"
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
            FcToaster.show(requireContext(), "Userid not found")
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
                    adapterFc?.addData(new)

                    val allParticipants = arrayListOf<String>()
                    for (i in sorted) {
                        i?.participants?.find { it != FcMyApplication.userId }?.let { user ->
                            allParticipants.add(user)
                        }
                    }
                    viewModel.getUserDetails(allParticipants)

                }
            })

            viewModel.singleChatUpdate.observe(this, Observer {
                Log.e("TAG", "setupObservers: redvel")
                adapterFc?.updateSingleChat(it)
            })

            viewModel.userDetails.observe(this, Observer {
                FcMyApplication.allUserDetails = it
                adapterFc?.notifyDataSetChanged()
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
            val chatListResponse = adapterFc?.getData()?.find { it?.chatId == chatId }
            chatListResponse?.let {
                (activity as FcHomeActivity).showChat(it)
                chatId = null
            }
        }
    }

    private fun updateSeenDataLocally(chatId: String?, seen: HashMap<String, String>) {
        chatId?.let {
            adapterFc?.updateOnlySeenValues(chatId, seen)
        }
    }

    override fun onClick(position: Int, chatListResponse: ChatListResponse?) {
        (activity as FcHomeActivity).showChat(chatListResponse)
    }

    private fun initBroadcastAndOthers() {
        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // Get extra data included in the Intent
                when (intent.getStringExtra(FcConstants.FCMParams.action)) {
                    FIRECHAT_NEW_MESSAGE -> {
                        val chatId = intent.getStringExtra(FcConstants.FCMParams.chatId)
                        chatId?.let {
                            viewModel.getSingleChatData(chatId)
                        } ?: kotlin.run {
                            FcToaster.show(requireContext(), "chatId is null")
                        }
                    }
                    FIRECHAT_MESSAGE_SEEN -> {
                        val _chatId = intent.getStringExtra(FcConstants.FCMParams.chatId)
                        val _userId = intent.getStringExtra(FcConstants.FCMParams.userId)
                        _chatId?.let {
                            _userId?.let {
                                val chatListResponse =
                                    adapterFc?.getData()?.find { it?.chatId == _chatId }
                                chatListResponse?.seen?.set(_userId.toString(), "0")
                                chatListResponse?.seen?.let {
                                    updateSeenDataLocally(_chatId, it)
                                }
                            } ?: kotlin.run {
                                FcLogger.debug("userId is null")
                            }
                        } ?: kotlin.run {
                            FcLogger.debug("chatId is null")
                        }
                    }
                }
            }
        }

        setFragmentResultListener(FcMessageFragment.MESSAGE_FRAGMENT_BACK) { reqKey, bundle ->
            if (reqKey == FcMessageFragment.MESSAGE_FRAGMENT_BACK) {
                val chatId = bundle.getString(FcConstants.Firestore.chatId)
                val lastMessageId = bundle.getString(FcConstants.Firestore.lastMessageId)
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
        FcMyApplication.userId?.let {
            val chatListResponse = adapterFc?.getData()?.find { it?.chatId == chatId }
            if (chatListResponse?.seen?.get(FcMyApplication.userId!!) != "0") {
                chatListResponse?.seen?.set(FcMyApplication.userId!!, "0")
                chatListResponse?.let {
                    adapterFc?.updateSingleChat(it)
                }
                //viewModel.updateSeenValues(chatId, chatListResponse?.seen)
            } else {
                adapterFc?.notifyDataSetChanged()
            }
        }
    }

    @Subscribe
    fun update(updateChatList: UpdateChatList) {
        updateChatList.chatListResponse?.let {
            adapterFc?.updateSingleChat(it)
        }
    }

}