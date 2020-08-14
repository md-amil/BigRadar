package io.bigradar.app.bigradar.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.interfaces.ConversationInterface
import io.bigradar.app.bigradar.interfaces.MessageInterface
import io.bigradar.app.bigradar.models.conversation.Message
import io.bigradar.app.bigradar.models.conversation.User
import io.bigradar.app.bigradar.toast

class MessageAdapter(val context: Context,private val listener: MessageInterface) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var messages:ArrayList<Message>  = ArrayList()

    fun replace(id:String,message:Message) {
        var i = 0
        messages.forEach {
            if(id==it._id){
                messages[i] = message
                notifyItemChanged(i)
                return@forEach
            }
            i++
        }
    }
    fun addAll(data:Array<Message>)
    {
        messages.clear()
        messages.addAll(data)
        notifyDataSetChanged()
    }
    fun add(data:Message)
    {
        messages.add(data)
        notifyDataSetChanged()
    }
    fun messageRead(id:String){
        messages.forEach {
            it.status = "read"
        }
        notifyDataSetChanged()
    }
    fun conversationRead(){
        messages.forEach {
            it.status = "read"
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (messages[position].isAuthor) {
            return 1
        }
        return 2
    }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         if (viewType == 2) {
             val inComingMessage = LayoutInflater.from(context).inflate(R.layout.in_coming_message, parent, false)
             return ViewHolder(inComingMessage)
         }
         val outGoingMessage = LayoutInflater.from(context).inflate(R.layout.out_going_message, parent, false)
         return ViewHolder(outGoingMessage)
     }

     override fun getItemCount(): Int {
         return messages.count()
     }

     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val message = messages[position]
         if (message.isAuthor) {
             holder.outgoingBind(message)
         } else {
             holder.incomingBind(message)
         }
     }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

         fun incomingBind(message: Message) {
             val leftTxt: TextView = itemView.findViewById(R.id.leftText)
             val incomingTime: TextView = itemView.findViewById(R.id.incoming_time)
             incomingTime.text = message.date
            leftTxt.text = message.text
             itemView.setOnLongClickListener {
                 listener.onLongClick(message.text)
                 true
             }
         }
         fun outgoingBind(message: Message) {
             val rightTxt: TextView = itemView.findViewById(R.id.right_text)
             val outgoingTime: TextView = itemView.findViewById(R.id.outgoing_time)
             val statusView: ImageView = itemView.findViewById(R.id.status_view)
             Log.d("status",message.status)
             if(message.status =="sending"){
                 statusView.visibility = View.VISIBLE
                 statusView.setImageResource(R.drawable.offline_status_icon)
             }else if(message.status == "sent") {
                 statusView.setImageResource(R.drawable.ic_check)
             }else if(message.status == "read")
                statusView.setImageResource(R.drawable.ic_status_done)
             else{
                 statusView.visibility = View.GONE
             }
             rightTxt.text = message.text
             outgoingTime.text = message.date
             itemView.setOnLongClickListener {
                 listener.onLongClick(message.text)
                 true
             }
         }
     }
}




