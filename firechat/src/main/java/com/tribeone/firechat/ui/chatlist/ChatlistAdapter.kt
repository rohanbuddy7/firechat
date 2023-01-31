package com.tribeone.firechat.ui.chatlist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.databinding.CardChatListBinding
import com.tribeone.firechat.model.ChatListResponse
import com.tribeone.firechat.utils.Constants
import com.tribeone.firechat.utils.ProfileUtils
import com.tribeone.firechat.utils.TimeUtils

internal class ChatlistAdapter(
    var context: Context,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<ChatlistAdapter.ViewHolder>() {

    private var selectedId: Int? = null
    private var data: ArrayList<ChatListResponse?> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =
            CardChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    internal class ViewHolder(itemView: CardChatListBinding) : RecyclerView.ViewHolder(itemView.root) {
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
            if (c?.messageType == Constants.Firestore.distinctType) {
                c.participants?.let {
                    for (i in it.indices) {
                        if (MyApplication.userId != c.participants[i]) {
                            val name: String = ProfileUtils.getInstance().getName(c.participants[i])
                            val profilePic: String? = ProfileUtils.getInstance().getProfilePicture(c.participants[i])

                            tvNameChatList.text = name
                            Glide.with(context).load(profilePic).into(ivDpChatList)
                            break
                        }
                    }
                }
            }
            tvMessageChatList.text = c?.lastMessage
            when (val count = c?.seen?.get(MyApplication.userId)) {
                "0" -> {
                    tvUnreadMsgsCountChatList.visibility = View.GONE
                }
                else -> {
                    tvUnreadMsgsCountChatList.visibility = View.VISIBLE
                    tvUnreadMsgsCountChatList.text = count
                }
            }
            c?.lastMessageAt?.let {
                val date = TimeUtils.getDatex(c.lastMessageAt.toLong(), "yyyy-MM-dd'T'HH:mm:ss")
                date?.let { datex ->
                    tvTimeChatList.text = TimeUtils.getTimeAgo(datex)
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

    fun getData(): ArrayList<ChatListResponse?>{
        return data
    }

}