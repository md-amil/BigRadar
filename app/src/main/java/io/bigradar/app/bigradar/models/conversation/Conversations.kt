package io.bigradar.app.bigradar.models.conversation

data class Conversations(
    val data: Array<Conversation>
){
    override fun toString(): String {
        return if(data.isEmpty()){
            "conversation is empty"
        }else{
            "data is there"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversations

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}