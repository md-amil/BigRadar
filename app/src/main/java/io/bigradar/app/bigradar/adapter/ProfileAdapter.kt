package io.bigradar.app.bigradar.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.UserPayload
import io.bigradar.app.bigradar.interfaces.ProfileInterFace
import io.bigradar.app.bigradar.models.Field
import org.json.JSONObject

class ProfileAdapter(val context: Context, private val fields:Array<Field>, val user : UserPayload, val listener:ProfileInterFace): RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fields.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val field = fields[position]
        holder.bind(field, user[field.name]!!, position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fieldView:TextView = itemView.findViewById(R.id.field_view)
        private val valueView:TextView = itemView.findViewById(R.id.value_view)
        private val editable:ImageView = itemView.findViewById(R.id.editable_icon)

        fun bind(field: Field, value:String, position:Int){
            fieldView.text = field.label
            itemView.setOnClickListener {
                listener.onclick(field, value, position,field.edit)
            }
            if(field.edit){
                editable.visibility = View.VISIBLE
                valueView.background = ContextCompat.getDrawable(context, R.drawable.dotted_border)

            }else{
                editable.visibility = View.INVISIBLE
                valueView.background = ContextCompat.getDrawable(context, R.drawable.no_border)
            }

            if (value.isEmpty()) {
                valueView.text = context.getString(R.string.empty)
                valueView.setTextColor(context.getColor(R.color.errorText))
                valueView.setTypeface(null, Typeface.ITALIC)
            } else {
                valueView.text = value
                valueView.setTextColor(context.getColor(R.color.textColor))
            }

        }
    }
}