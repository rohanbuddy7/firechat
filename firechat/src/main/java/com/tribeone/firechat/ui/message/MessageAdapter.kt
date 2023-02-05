package com.tribeone.firechat.ui.message

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.R
import com.tribeone.firechat.databinding.FcCardChatBinding
import com.tribeone.firechat.model.Message
import com.tribeone.firechat.ui.chatlist.ChatlistAdapter
import com.tribeone.firechat.utils.ProfileUtils
import com.tribeone.firechat.utils.TimeUtils

internal class MessageAdapter(
    var context: Context,
    var onClickListener: OnClickListener
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var selectedId: Int? = null
    private var data: ArrayList<Message> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =
            FcCardChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    interface OnClickListener {
        fun onClick(position: Int, data: String)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            onClickListener.onClick(position, "")
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal class ViewHolder(itemview: FcCardChatBinding) : RecyclerView.ViewHolder(itemview.root) {
        private val llChatOther = itemview.llChatOther
        private val llChatMe = itemview.llChatMe
        private val tvChatOther = itemview.tvChatOther
        private val tvChatTimeOther = itemview.tvChatTimeOther
        private val tvChatMe = itemview.tvChatMe
        private val ivChatPpOther = itemview.ivChatPpOther
        fun bind(message: Message) {
            if (message.userId == MyApplication.userId) {
                llChatOther.visibility = View.GONE
                llChatMe.visibility = View.VISIBLE
                tvChatMe.text = message.message
            } else {
                llChatOther.visibility = View.VISIBLE
                llChatMe.visibility = View.GONE
                tvChatOther.text = message.message
                message.userId?.let {
                    Glide
                        .with(ivChatPpOther.context)
                        .load(ProfileUtils.getInstance().getProfilePicture(message.userId))
                        .into(ivChatPpOther)
                }
                message.timestamp?.let {
                    val date =
                        TimeUtils.getDatex(message.timestamp.toLong(), "yyyy-MM-dd'T'HH:mm:ss")
                    date?.let { datex ->
                        tvChatTimeOther.text = TimeUtils.getTimeAgo(datex)
                    }
                }
            }
        }
    }

    fun addData(n: ArrayList<Message>) {
        data.addAll(n)
        notifyDataSetChanged()
    }

    fun addNewMessageList(n: ArrayList<Message>) {
        data.addAll(0, n)
        notifyItemInserted(0)
    }

    fun addSingleMessage(m: Message) {
        data.add(0, m)
        notifyItemInserted(0)
    }

    fun getRecentlyAddedMessage(): Message{
        return data[0]
    }

}