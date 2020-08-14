package io.bigradar.app.bigradar.controllers.conversation

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import io.bigradar.app.bigradar.*
import io.bigradar.app.bigradar.adapter.ProfileAdapter
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.interfaces.ProfileInterFace
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.Field
import org.json.JSONArray
import org.json.JSONObject


class ProfileActivity : AppCompatActivity() {
    private lateinit var swipeContainer:SwipeRefreshLayout
    lateinit var recyclerView:RecyclerView
    lateinit var profileAdapter:ProfileAdapter
    private var userId:String? = null
    var user: UserPayload? = null
    lateinit var fields:Array<Field>
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var progressBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        userId = intent.extras?.getString(USER_ID_KEY)
        val userName = intent.extras?.getString(USER_NAME_KEY)
        initViews()
        loadFields()
        toolbar.title = userName
        setSupportActionBar(toolbar)
        if(supportActionBar!=null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean
    {
        onBackPressed()
        return true
    }

    private fun initViews()
    {
        progressBar = findViewById(R.id.profile_progress_bar)
        swipeContainer = findViewById(R.id.profile_swipe_container)
        toolbar = findViewById(R.id.change_password_toolbar)
        recyclerView  = findViewById(R.id.profile_recycler_view)
        swipeContainer.setOnRefreshListener { loadFields(true) }
        swipeContainer.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun loadFields(isRefresh:Boolean = false)
    {
        if(!isRefresh){
            progressBar.visibility  = View.VISIBLE
        }
        Request(this).get("fields").then {fieldResponse->
            fields = (Gson().fromJson(fieldResponse, Array<Field>::class.java))
            Request(this).get("users/${userId}").then {userResponse->
                Log.d("users",userResponse)
                progressBar.visibility  = View.GONE
                swipeContainer.isRefreshing = false
                user = JSONUtil.flattenData(JSONObject(userResponse).get("user") as JSONObject,fields)
                profileAdapter = ProfileAdapter(this@ProfileActivity,fields, user!!, listener)
                recyclerView.apply {
                    adapter = profileAdapter
                    layoutManager = LinearLayoutManager(this@ProfileActivity)
                }
            }.catch {
                progressBar.visibility  = View.GONE
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                swipeContainer.isRefreshing = false
            }
        }.catch {
            progressBar.visibility  = View.GONE
            swipeContainer.isRefreshing = false
            Toast.makeText(this@ProfileActivity,"filed not loaded",Toast.LENGTH_SHORT).show()
        }
    }

    private fun alertDialog(field:Field,value:String,position: Int)
    {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.set_profile_dialog,null)
        val editText = dialogView.findViewById<EditText>(R.id.dialog_edit_text)
        val title = dialogView.findViewById<TextView>(R.id.title_view)
        editText.requestFocus()
        editText.setText(value)
        editText.hint = field.label
        title.text = field.label
        builder.setView(dialogView)
            .setPositiveButton("save"){ _, _->
                editText.text.trim().toString().also {
                    updateUserInfo(field, it,position)
                }
            }
            .setNegativeButton("cancel"){ _, _ ->

            }.show()


    }

    private fun updateUserInfo(field: Field, data: String, position:Int)
    {
        val payload = JSONObject()
        if(field.type == "list") {
            payload.put(field.name, JSONArray(data.split(", ")))
        } else {
            payload.put(field.name, data)
        }
        Request(this).put("users/${userId}", payload).then {
            user!![field.name] = data
            profileAdapter.notifyItemChanged(position)
        }.catch {
            toast("failed to update")
        }
    }
    private val listener = object :ProfileInterFace{
        override fun onclick(field: Field,value:String,position:Int,editable: Boolean) {

            if(editable){
                alertDialog(field,value,position)
            }
        }
    }
    private fun Context.toast(message:String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }

}
