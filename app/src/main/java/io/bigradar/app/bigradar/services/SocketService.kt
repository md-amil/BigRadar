package io.bigradar.app.bigradar.services

import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import io.bigradar.app.bigradar.controllers.App
import org.json.JSONObject

object SocketService {
    lateinit var socket: Socket

    fun connect(token: String) : SocketService {
        socket = IO.socket("https://app.bigradar.io?token=${App.storage.authToken}")
        return this
    }

    fun sendMessage(): SocketService {
        return this
    }

    fun typing(conversationId: String, isTyping: Boolean): SocketService {
        val data = JSONObject()
        data.put("id", conversationId)
        if (isTyping) {
            socket.emit("client/typing/start", data)
        } else {
            socket.emit("client/typing/stop", data)
        }
        return this
    }

    fun onMessage(): SocketService {
        return this
    }

    fun onTyping(callback: (String, Boolean) -> Void): SocketService {
        socket.on("server/typing/start") {
            val conversationId = JSONObject("${it[0]}").getString("id")
            callback(conversationId, true)
        }

        socket.on("server/typing/stop") {
            val conversationId = JSONObject("${it[0]}").getString("id")
            callback(conversationId, false)
        }
        return this
    }

    fun onConversationDelete() {

    }

    fun onUserStatus() {

    }

    fun onUserJoin() {

    }
    fun onConversation(){

    }
}
