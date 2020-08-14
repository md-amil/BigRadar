package io.bigradar.app.bigradar.libs

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random


private const val CHANNEL_NAME = "BigRadar"
private const val CHANNEL_ID = "com.example.notifications$CHANNEL_NAME"

class Notification(protected val context: Context) {
    var notificationBuilder: NotificationCompat.Builder
    init {
        notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
    }
    fun title(title: String): Notification {
        notificationBuilder.setContentTitle(title)
        return this
    }
    fun content(body: String): Notification {
        notificationBuilder.setContentText(body)
        return this
    }

    fun notify(id: Int) {
        NotificationManagerCompat.from(context).notify(id, notificationBuilder.build())
    }

    fun notify(): Int {
        val id = Random.nextInt()
        NotificationManagerCompat.from(context).notify(id, notificationBuilder.build())
        return id
    }
}
