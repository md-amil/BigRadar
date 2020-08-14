package io.bigradar.app.bigradar.controllers.profile

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.libs.request.Request

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var saveButton:Button
    lateinit var oldPassword:EditText
    lateinit var newPassword:EditText
    lateinit var confirmPassword:EditText
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_password)
        saveButton = findViewById(R.id.update_profile)
        oldPassword = findViewById(R.id.old_password)
        newPassword = findViewById(R.id.new_password)
        progressBar = findViewById(R.id.progressBar)
        confirmPassword = findViewById(R.id.confirm_password)
        toolbar = findViewById(R.id.change_password_toolbar)
        toolbar.title = "Update Password"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        saveButton.setOnClickListener {
            handleSavePassword()
        }
    }
    private fun handleSavePassword(){

        val oldText = oldPassword.text.toString()
        val newText = newPassword.text.toString()
        val confirmText = confirmPassword.text.toString()

        when {
            oldText.isEmpty() -> {
                oldPassword.error = "Enter Old Password"
            }
            newText.isEmpty() -> {
                newPassword.error = "Enter New Password"
            }
            confirmText.isEmpty() -> {
                confirmPassword.error = "Enter Confirm Password"
            }
            newText != confirmText -> {
                confirmPassword.error = "Password does not match"
            }else->{
                saveButton.text = ""
                progressBar.visibility = View.VISIBLE

                Request(this).post("password", mapOf("old_password" to oldText, "password" to newText,"confirm" to confirmText )).then{
                    toast("password changed successfully")
                    saveButton.text = "Update Password"
                    progressBar.visibility = View.INVISIBLE
                    clearInput()
                }.catch {
                    clearInput()
                    saveButton.text = "Update Password"
                    progressBar.visibility = View.INVISIBLE
                    toast(it.message)
                }
            }
        }



    }
    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }
    private fun clearInput(){
        oldPassword.text.clear()
        newPassword.text.clear()
        confirmPassword.text.clear()
    }
    private fun Context.toast(message:String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }

}
