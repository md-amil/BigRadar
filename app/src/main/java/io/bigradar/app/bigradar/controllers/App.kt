package io.bigradar.app.bigradar.controllers

import android.app.Application
import android.util.Log
import io.bigradar.app.bigradar.controllers.conversation.ConversationActivity
import io.bigradar.app.bigradar.services.CrashHandler
import io.bigradar.app.bigradar.services.SharedPref

class App(): Application() {

    companion object {
        lateinit var storage: SharedPref
        var activeUserName = ""
        var activeUserId = ""
        var activeUserLastSeen = ""
        var activeUserStatus = 0
        var activeConversation = ConversationActivity.ConversationType.OPEN
        var isSocketConnected = false
        var activeConversationPosition = 0
        var activeUserPosition = 0

    }


    override fun onCreate() {
        super.onCreate()
        storage = SharedPref(applicationContext,"login_preferences")
//        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(applicationContext))
    }
}