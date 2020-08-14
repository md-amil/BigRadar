package io.bigradar.app.bigradar.controllers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.cardview.widget.CardView
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.controllers.profile.ChangePasswordActivity
import io.bigradar.app.bigradar.libs.request.Request
import org.json.JSONObject


/**
 *
 * Setting token  POST /token/fcm
 * {"token": "token"}
 */

class SettingActivity : AppCompatActivity() {
    private lateinit var changePassword:CardView

    val switches = mapOf(
        R.id.email_message to listOf("email", "message"),
        R.id.email_conversation to listOf("email", "conversation")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initViews()
        loadNotification()
    }

    private fun Context.toast(message:String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }
    private fun  initViews() {
        changePassword = findViewById(R.id.change_password)
        changePassword.setOnClickListener {
            val passwordIntent = Intent(this,
                ChangePasswordActivity::class.java)
            startActivity(passwordIntent)
        }
        for ((k, v)in switches) {
            findViewById<Switch>(k).setOnCheckedChangeListener { _, isChecked: Boolean ->
                handleNotificationUpdate(v[0], v[1], isChecked)
            }
        }

    }
    private fun loadNotification() {
        Request(this).get("notifications").then {
            val data = JSONObject(it)
            for((k, v) in switches) {
                if (!data.has(v[0])) {
                    continue
                }
                val setting = data.getJSONObject(v[0])
                if (!setting.has(v[1])) {
                    continue
                }

                findViewById<Switch>(k).isChecked = setting.getBoolean(v[1])
            }
        }.catch {
            toast(it.message)
        }
    }


    private fun handleNotificationUpdate(type: String, name: String, isChecked: Boolean) {
        val json = JSONObject()
        val setting = JSONObject()
        setting.put("$type.$name", isChecked)
        json.put("notifications", setting)
        Request(this@SettingActivity).post("notifications", json).then {
            toast("updated")
        }.catch {
            toast(it.message)
        }
    }
}
