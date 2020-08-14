package io.bigradar.app.bigradar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.changeBackground
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.interfaces.ConversationInterface
import io.bigradar.app.bigradar.libs.Moment
import io.bigradar.app.bigradar.models.conversation.Conversation
import io.bigradar.app.bigradar.models.conversation.Message

class ConversationAdapter(val context:Context, private val listener:ConversationInterface):
    RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    private val conversations:ArrayList<Conversation> = ArrayList()


    fun addAll(value: Array<Conversation>) {
        conversations.clear()
        conversations.addAll(value)
        notifyDataSetChanged()
    }

    fun clearUnreadBadge(id: String) {
        for ((i, conversation) in conversations.withIndex()) {
            if (id == conversation._id) {
                conversation.unreadMessage = 0
                App.activeUserStatus = conversation.user.status ?: 0
                notifyItemChanged(i)
                return
            }

        }
    }
    fun add(value:Conversation){
        value.unreadMessage = 1
        conversations.add(0,value)
        notifyItemInserted(0)

    }

    fun update(message: Message,isChat:Boolean){
        for ((i, convers) in conversations.withIndex()) {
            if (convers._id == message.conversation_id) {
                convers.messages?.add(message)
                convers.message?.text = message.text
                if (!isChat) {
                    convers.unreadMessage += 1
                }
                convers.updatedAt = message.createdAt
                notifyItemChanged(i)
            }
        }
    }

    fun updateTyping(id: String, isTyping: Boolean) {
        for ((i, conversation) in conversations.withIndex()) {
            if (conversation.user._id == id) {
                conversation.user.isTyping = isTyping
                notifyItemChanged(i)
            }
        }

    }

    fun updateStatus(id: String, status: Int, isLeaving: Boolean) {
        for ((i, conversation) in conversations.withIndex()) {
            if (conversation.user._id == id) {
                conversation.user.status = status
                if (isLeaving) {
                    conversation.user.lastSeen = Moment.nowAsIso()
                }
                notifyItemChanged(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.conversation_item, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return conversations.count()
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        holder.bind(conversations[position],position,listener)
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        fun bind(conversation: Conversation,position: Int, listener:ConversationInterface) {
            val dp: TextView = itemView.findViewById(R.id.listDp)
            val name: TextView = itemView.findViewById(R.id.listName)
            val msg: TextView = itemView.findViewById(R.id.listMessage)
            val date: TextView = itemView.findViewById(R.id.listDate)
            val badge: TextView = itemView.findViewById(R.id.badge)
            val presenceOnline: ImageView = itemView.findViewById(R.id.present_online_view)
            dp.text = conversation.user.shortName
            changeBackground(context, dp, conversation.user.color)
            name.text = conversation.user.name
            date.text = conversation.date
            if (conversation.user.isTyping) {
                msg.text = "typing..."
            } else {
                msg.text = conversation.message?.shortText()
            }
            if (conversation.user.status == 1) {
                presenceOnline.visibility = View.VISIBLE
            } else {
                presenceOnline.visibility = View.INVISIBLE
            }
            if (conversation.unreadMessage <= 0) {
                badge.visibility = View.INVISIBLE
            } else {
                badge.visibility = View.VISIBLE
                badge.text = conversation.unreadMessage.toString()
            }
            itemView.setOnClickListener {
                conversation.unreadMessage = 0
                App.activeUserStatus = conversation.user.status ?: 0
                notifyItemChanged(position)
                listener.onclick(conversation._id,conversation.user.name,conversation.user._id!!,conversation.user.lastSeen)
            }
        }
    }
}