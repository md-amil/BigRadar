package io.bigradar.app.bigradar.models.conversation

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.bigradar.app.bigradar.libs.Moment
import java.io.Serializable
import java.text.ParseException
import java.util.*

//enum class MessageStatus {
//    sent, read, delivered
//}
data class Message(
    val conversation_id:String? = null,
    val _id: String = "",
    var createdAt: String,
    var text: String,
    val type: String = "text",
    val read: Boolean = false,
    val isAuthor: Boolean = false,
    val referer: String = "",
    val user: User? = null,
    var status: String = "sending" // sent, read, delivered, email sent, email delivered, email opened, email clicked
    ):Serializable {
    override fun toString(): String {
        return "$_id - $text, isAuthor: $isAuthor"
    }

    fun shortText(len: Int = 10): String {
        if (text.length > len) {
            return "${text.substring(0, len)}..."
        }
        return text
    }

    val date:String get() =  Moment(createdAt).humanize()


}