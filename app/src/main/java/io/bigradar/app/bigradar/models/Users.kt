package io.bigradar.app.bigradar.models
import io.bigradar.app.bigradar.models.conversation.User
import org.json.JSONArray

data class Users(
    val docs: Array<User>,
    val pages:Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Users

        if (!docs.contentEquals(other.docs)) return false

        return true
    }

    override fun hashCode(): Int {
        return docs.contentHashCode()
    }
}