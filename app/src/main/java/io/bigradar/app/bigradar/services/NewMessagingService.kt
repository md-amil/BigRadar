
package io.bigradar.app.bigradar.services

import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import io.bigradar.app.bigradar.controllers.conversation.ChatActivity

class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val TAG = "fireBase"

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var name = ""
        var conversationId = ""
        var userId = ""

        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            name = remoteMessage.data["name"] ?: ""
            conversationId = remoteMessage.data["conversation_id"] ?: ""
            userId = remoteMessage.data["name"] ?: ""
        }
        remoteMessage.notification?.let {
//            NotificationService(applicationContext).sendNotification(
//                name, "${it.body}",
//                ChatActivity::class.java,
//                conversationId,
//                name,
//                userId
//            )
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

}