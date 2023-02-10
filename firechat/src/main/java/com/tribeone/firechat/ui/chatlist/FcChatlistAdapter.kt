package com.tribeone.firechat.ui.chatlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tribeone.firechat.FcMyApplication
import com.tribeone.firechat.databinding.FcCardChatListBinding
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.utils.FcConstants
import com.tribeone.firechat.utils.FcProfileUtils
import com.tribeone.firechat.utils.FcTimeUtils

internal class FcChatlistAdapter(
    var context: Context,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<FcChatlistAdapter.ViewHolder>() {

    private var selectedId: Int? = null
    private var data: ArrayList<ChatListResponse?> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = FcCardChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    interface OnClickListener {
        fun onClick(position: Int, chatListResponse: ChatListResponse?)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, position, data[position], onClickListener)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal class ViewHolder(itemView: FcCardChatListBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val tvNameChatList = itemView.tvNameChatList
        private val ivDpChatList = itemView.ivDpChatList
        private val tvMessageChatList = itemView.tvMessageChatList
        private val tvTimeChatList = itemView.tvTimeChatList
        private val tvUnreadMsgsCountChatList = itemView.tvUnreadMsgsCountChatList

        fun bind(
            context: Context,
            position: Int,
            c: ChatListResponse?,
            onClickListener: OnClickListener
        ) {
            if (c?.messageType == FcConstants.Firestore.distinctType) {
                c.participants?.let {
                    for (i in it.indices) {
                        if (FcMyApplication.userId != c.participants[i]) {
                            val name: String =
                                FcProfileUtils.getInstance().getName(c.participants[i])
                            val profilePic: String? =
                                FcProfileUtils.getInstance().getProfilePicture(c.participants[i])

                            tvNameChatList.text = name
                            if (profilePic != null && profilePic.trim().isNotEmpty()) {
                                Glide.with(context).load(profilePic).into(ivDpChatList)
                            }
                            break
                        }
                    }
                }
            }
            tvMessageChatList.text = c?.lastMessage
            when (val count = c?.seen?.get(FcMyApplication.userId)) {
                "0" -> {
                    tvUnreadMsgsCountChatList.visibility = View.GONE
                }
                else -> {
                    tvUnreadMsgsCountChatList.visibility = View.VISIBLE
                    tvUnreadMsgsCountChatList.text = count
                }
            }
            c?.lastMessageAt?.let {
                val date = FcTimeUtils.getDatex(c.lastMessageAt.toLong(), "yyyy-MM-dd'T'HH:mm:ss")
                date?.let { datex ->
                    tvTimeChatList.text = FcTimeUtils.getTimeAgo(datex)
                }
            }

            itemView.setOnClickListener {
                onClickListener.onClick(position, c)
            }

        }
    }

    fun addData(n: ArrayList<ChatListResponse?>) {
        this.data = n
        notifyDataSetChanged()
    }

    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }

    fun updateSingleChat(c: ChatListResponse) {
        for (i in data.indices) {
            if (data[i]?.chatId == c.chatId) {
                data[i] = c
                notifyItemChanged(i)
                break
            }
        }
    }

    fun updateOnlySeenValues(chatId: String, seen: HashMap<String, String>) {
        for (i in data.indices) {
            if (data[i]?.chatId == chatId) {
                data[i]?.seen = seen
                notifyItemChanged(i)
                break
            }
        }
    }

    fun getData(): ArrayList<ChatListResponse?> {
        return data
    }

}