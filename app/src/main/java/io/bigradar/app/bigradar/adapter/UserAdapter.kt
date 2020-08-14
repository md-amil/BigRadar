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
import io.bigradar.app.bigradar.camelCase
import io.bigradar.app.bigradar.changeBackground
import io.bigradar.app.bigradar.interfaces.UserAdapterInterface
import io.bigradar.app.bigradar.models.conversation.Filter
import io.bigradar.app.bigradar.models.conversation.User


class UserAdapter(val context: Context,  private val listener:UserAdapterInterface):
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var users: ArrayList<User> = ArrayList()
    private var filters:ArrayList<Filter> = ArrayList()
    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1
    private val VIEW_TYPE_CHIP  = 2
    private var isLoaderVisible = false

    fun clear() {
        users.clear()
        notifyDataSetChanged()
    }

    fun addFilter(f: ArrayList<Filter>) {
        filters.clear()
        filters.addAll(f)
        notifyDataSetChanged()
    }

    fun removeChip(position: Int) {
        filters.removeAt(position)
    }

    fun addAll(data: Array<User>, isRefresh: Boolean = false) {

        users.addAll(data)
        notifyDataSetChanged()
    }

    fun addLoading() {
        isLoaderVisible = true
        users.add(User("", "", "", "", "", 0, null, "", ""))
        notifyItemInserted(users.count() - 1)
    }

    fun removeLoading(){
        isLoaderVisible = false
        val position: Int = users.count() - 1
        users.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position < filters.count()){
            VIEW_TYPE_CHIP
        }else if (isLoaderVisible && position == users.count() - 1) {
            VIEW_TYPE_LOADING
        }else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CHIP -> {
                val view = LayoutInflater.from(context).inflate(R.layout.chip_item, parent,false)
                ViewHolder(view)
            }
            VIEW_TYPE_NORMAL -> {
                val view = LayoutInflater.from(context).inflate(R.layout.user_item, parent,false)
                ViewHolder(view)
            }
            else -> {
                ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_loading, parent,false))
            }
        }
    }

    override fun getItemCount(): Int {
        return filters.count() + users.count()
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int){
        if(position < filters.count()){
            holder.bindChip(filters[position],position)
        }else if (isLoaderVisible){
            if (position == (users.count() + filters.count()) - 1) holder.bindLoader() else holder.bindUser(users[position - filters.count()])
        }else {
            holder.bindUser(users[position - filters.count()])
        }
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindUser(user: User) {
            val dp:TextView = itemView.findViewById(R.id.user_dp)
            val email:TextView = itemView.findViewById(R.id.user_email)
            val name:TextView = itemView.findViewById(R.id.user_name)
            val phone:TextView = itemView.findViewById(R.id.user_phone)
            name.text = user.name
            dp.text = user.shortName
            changeBackground(context, dp, user.color)
                email.text = user.email
                phone.text = user.phone
            itemView.setOnClickListener {
                listener.onClick(user._id!!,user.name)
            }
        }

        fun bindLoader() {

        }
        fun bindChip(filter:Filter,p: Int){
            val chipText:TextView = itemView.findViewById(R.id.chipTextView)
            val chipClose:ImageView = itemView.findViewById(R.id.close_chip)
            chipText.text =
                "${camelCase(filter.field)} ${camelCase(filter.condition)} ${camelCase(filter.value)}"
            chipClose.setOnClickListener{
                filters.remove(filter)
                notifyDataSetChanged()
                Log.d("close position",p.toString())
                listener.onClose(filter)
            }
        }
    }
}