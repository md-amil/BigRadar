package io.bigradar.app.bigradar.controllers.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.bigradar.app.bigradar.*
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.libs.CircleTransform
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.Me
import io.bigradar.app.bigradar.models.conversation.User

@Suppress("DEPRECATION")
class MyProfileActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var changePassword:CardView
    private lateinit var changeProfile:CardView
    private lateinit var avatarView:ImageView
    private lateinit var name:TextView
    private lateinit var email:TextView
    private var  myData: Me? = null
    private var userAvatar: String = "jksdlfjklsk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            profileUpdateBroadcastReceiver, IntentFilter(BROADCAST_PROFILE_UPDATE)
        )
        name = findViewById(R.id.my_profile_name_view)
        email = findViewById(R.id.my_profile_email_view)
        changePassword = findViewById(R.id.change_password)
        changeProfile= findViewById(R.id.change_profile)
        avatarView = findViewById(R.id.my_profile_avatar)
        toolbar = findViewById(R.id.my_profile_toolbar)
        toolbar.title = "Profile"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        userAvatar = intent.extras?.getString(USER_URL) ?: "sdlkfjsd"
        changePassword.setOnClickListener { startActivity(Intent(this,
            ChangePasswordActivity::class.java)) }
        changeProfile.setOnClickListener {
                val intent = Intent(this,ChangeProfileActivity::class.java)
                startActivity(intent)
        }

    }


    override fun onStart() {
        super.onStart()
        App.storage.apply {
            Picasso.get().load(myAvatar).transform(CircleTransform()).placeholder(R.drawable.avatar).into(avatarView)
            name.text = myName
            email.text = myEmail
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profileUpdateBroadcastReceiver)

    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }

    private val profileUpdateBroadcastReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            intent.extras?.let {
                name.text = it.getString(PROFILE_UPDATED_NAME) ?: ""
            }
        }
    }
}
