package io.bigradar.app.bigradar.controllers.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.socketio.client.IO
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.controllers.conversation.ConversationActivity
import io.bigradar.app.bigradar.libs.request.Request
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.net.URISyntaxException


class LoginActivity : AppCompatActivity() {
    lateinit var progress:ProgressBar
    lateinit var loginButton: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            loginButton = findViewById(R.id.loginbtn)
            progress = findViewById(R.id.progressBar1)
            progress.visibility = View.GONE
            loginButton.setOnClickListener {
                loginClicked()
            }
        }

        private fun loginClicked() {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            if (email.isEmpty()) {
                emailField.error = "please enter email"
                return
            }
            if (password.isEmpty()) {
                passwordField.error = "please enter password"
                return
            }

            loginButton.text = ""
            progress.visibility = View.VISIBLE
            Request(this).post(
                "login",
                mapOf("email" to email, "password" to password, "hello" to "dj")
            ).then {
                loginButton.text = "Login"
                progress.visibility = View.GONE
                App.storage.authToken = JSONObject(it).getString("token")
                App.storage.isLoggedIn = true
                firebase()
            }.catch {
                loginButton.text = "Login"
                progress.visibility = View.GONE
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }

        private fun firebase() {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.d("fireBase", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    Log.d("token", token!!)
                    Request(this).post("token/fcm", mapOf("token" to token)).then {
                        startActivity(
                            Intent(this@LoginActivity, ConversationActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        finish()
                    }.catch {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
