package io.bigradar.app.bigradar.controllers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.bigradar.app.bigradar.R


class ExceptionDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception_display)
        val exceptionText = findViewById<TextView>(R.id.exception_text)
        exceptionText.text = intent.extras!!.getString("error")
    }



}