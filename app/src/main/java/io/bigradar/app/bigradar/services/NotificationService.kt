package io.bigradar.app.bigradar.services

import androidx.core.app.NotificationManagerCompat
import android.R.drawable.ic_input_add
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.content.ContextWrapper
import android.graphics.Color
import androidx.core.app.NotificationCompat
import io.bigradar.app.bigradar.CONVERSATION_ID_KEY
import io.bigradar.app.bigradar.CONVERSATION_USER_NAME
import io.bigradar.app.bigradar.USER_ID_KEY
import io.bigradar.app.bigradar.models.conversation.Conversation
import java.util.*

private const val CHANNEL_NAME = "BigRadar"
private const val CHANNEL_ID = "com.example.notifications$CHANNEL_NAME"
class NotificationService (base: Context): ContextWrapper(base) {
        var manager:NotificationManager? =null
        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannels()
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        private fun createChannels() {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.description = "this is the bigradar notification"
            notificationChannel.lightColor = Color.RED
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager!!.createNotificationChannel(notificationChannel)
        }
        fun sendNotification(
            title: String,
            body: String,
            activityName: Class<*>,
            conversationId:String,
            name:String,userId:String
        ) {
            val notificationId = Conversation.idToNumber(conversationId)
            val intent = Intent(this, activityName)
            intent.putExtra(CONVERSATION_ID_KEY, conversationId)
            intent.putExtra(CONVERSATION_USER_NAME, name)
            intent.putExtra(USER_ID_KEY,userId)
            val pendingIntent = PendingIntent.getActivity(this, 267, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notification= NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(ic_input_add)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(
                    NotificationCompat.BigTextStyle().setSummaryText("summary").setBigContentTitle(title).bigText(body)
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        }
    }

