package io.bigradar.app.bigradar.models.conversation

import io.bigradar.app.bigradar.libs.Moment

data class Conversation(
    val _id: String,
    val admin_id: String?,
    val message: Message?,
    val messages: ArrayList<Message>?,
    val status: String,
    val totalMessage: Int,
    var unreadMessage: Int,
    var updatedAt: String,
    val user: User
) {
    companion object {
        fun idToNumber(id: String): Int {
            return id.takeLast(3).toInt(16)
        }
    }


    val date:String get() =  Moment(updatedAt).humanize()
}



