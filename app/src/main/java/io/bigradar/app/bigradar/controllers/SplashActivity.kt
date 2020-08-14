package io.bigradar.app.bigradar.controllers

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.bigradar.app.bigradar.ACTIVE_CONVERSATION_ID_KEY
import io.bigradar.app.bigradar.ACTIVE_USER_ID_KEY
import io.bigradar.app.bigradar.ACTIVE_USER_NAME_KEY
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.controllers.auth.LoginActivity
import io.bigradar.app.bigradar.controllers.conversation.ConversationActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if(App.storage.isLoggedIn){
            val i = Intent(this, ConversationActivity::class.java)
            intent.extras?.let {
                val userId = it.getString("user_id")
                val userName = it.getString("name")
                val conversationId = it.getString("conversation_id")
                i.putExtra(ACTIVE_USER_ID_KEY,userId)
                i.putExtra(ACTIVE_USER_NAME_KEY,userName)
                i.putExtra(ACTIVE_CONVERSATION_ID_KEY, conversationId)
            }
            startActivity(i)
            finish()
        }else{
            val loginIntent = Intent(this,
                LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }
}
