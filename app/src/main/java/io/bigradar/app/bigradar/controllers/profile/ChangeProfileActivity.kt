package io.bigradar.app.bigradar.controllers.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import io.bigradar.app.bigradar.*
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.Me
import io.bigradar.app.bigradar.models.conversation.Message
import io.bigradar.app.bigradar.models.conversation.User
import org.json.JSONObject

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var nameField:EditText
    private lateinit var phoneField:EditText
    private lateinit var updateProfile: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar:Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_update)
        nameField  = findViewById(R.id.name_field)
        phoneField = findViewById(R.id.phone_field)
        updateProfile = findViewById(R.id.update_profile)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.change_profile_toolbar)
        toolbar.title = "Update Profile"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        nameField.setText(App.storage.myName)
        phoneField.setText(App.storage.myPhone)
        updateProfile.setOnClickListener {
            val name = nameField.text.toString()
            val phone = phoneField.text.toString()
            if(name.isEmpty()){
                nameField.error = "enter name"
            }else{
                handleUpdateProfile(name,phone)
            }
        }
    }
    private fun handleUpdateProfile(name:String,phone:String){
        updateProfile.text = ""
        progressBar.visibility = View.VISIBLE

        val data = JSONObject()
        if(phone.isNotEmpty()){
            data.put("phone",phone)
        }
        data.put("name",name)

        Request(this).post("profile", data).then {
            nameField.text.clear()
            phoneField.text.clear()
            offLoading()
            Log.d("profile_data",it)
            val profileUpdateIntent = Intent(BROADCAST_PROFILE_UPDATE)
            val user: User = Gson().fromJson(it, User::class.java)
            val response = JSONObject(it)
            App.storage.apply {
                myName = user.name
                myPhone = phone
            }
            profileUpdateIntent.putExtra(PROFILE_UPDATED_NAME, user.fullname)

            LocalBroadcastManager.getInstance(this).sendBroadcast(profileUpdateIntent)
            finish()
            Toast.makeText(this,"Profile Updated",Toast.LENGTH_SHORT).show()
        }.catch {
            offLoading()
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }



    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }

    private fun offLoading(){
        progressBar.visibility = View.INVISIBLE
        updateProfile.text = "update profile"
    }

}
