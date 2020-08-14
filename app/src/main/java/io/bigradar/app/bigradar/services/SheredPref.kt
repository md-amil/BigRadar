package io.bigradar.app.bigradar.services

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import io.bigradar.app.bigradar.models.Me
import io.bigradar.app.bigradar.models.conversation.Location
import io.bigradar.app.bigradar.models.conversation.User



class SharedPref(context: Context, fileName:String) {
    val defToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1ZDliOGM2MDJkZWZkOTQ4Y2I3Y2RjNTMiLCJpYXQiOjE1NzA0NzUxMDQyNTB9.VSIGHvoZbZFGlpvcKVASLd3JGlLzmgwr9DlSdruyjsU"
    private val PREFS_FILENAME = fileName
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME,0)
    private val IS_LOGGED_IN = "isLoggedIn"
    private val AUTH_TOKEN = "auth_token"
    private val USER_EMAIL = "userEmail"
    private val USER_NAME = "userName"
    private val USER_PHONE = "userPhone"
    private val USER_AVATAR = "userAvatar"
    private val ME = "me_data"
    private val gson = Gson()


    var isLoggedIn:Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN,false)
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN,value).apply()

    var authToken:String?
        get() = prefs.getString(AUTH_TOKEN,"")
        set(value) = prefs.edit().putString(AUTH_TOKEN,value).apply()

    var myName:String?
        get() = prefs.getString(USER_NAME,"")
        set(value) = prefs.edit().putString(USER_NAME,value).apply()

    var myEmail:String?
        get() = prefs.getString(USER_EMAIL,"")
        set(value) = prefs.edit().putString(USER_EMAIL,value).apply()

    var myPhone:String?
        get() = prefs.getString(USER_PHONE,"")
        set(value) = prefs.edit().putString(USER_PHONE,value).apply()

    var myAvatar:String?
        get() = prefs.getString(USER_AVATAR,"hello world")
        set(value) = prefs.edit().putString(USER_AVATAR,value).apply()

//    var me: Me?
//        get() =Gson().fromJson(prefs.getString(ME,""), Me::class.java)
//        set(value) = prefs.edit().putString(ME,gson.toJson(value)).apply()

//    val requestQueue = Volley.newRequestQueue(context)

}