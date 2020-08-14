package io.bigradar.app.bigradar.controllers.user

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import io.bigradar.app.bigradar.*
import io.bigradar.app.bigradar.adapter.ProfileAdapter
import io.bigradar.app.bigradar.interfaces.ProfileInterFace
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.Field
import org.json.JSONArray
import org.json.JSONObject

class UserDetailActivity : AppCompatActivity() {
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    lateinit var recyclerView: RecyclerView
    lateinit var profileAdapter: ProfileAdapter
    private var userId:String? = null
    var user: UserPayload? = null
    lateinit var fields:Array<Field>
    lateinit var toolbar:Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
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
    override fun onSupportNavigateUp():Boolean
    {
        onBackPressed()
        return true
    }
    private fun initViews()
    {
        swipeContainer = findViewById(R.id.profile_swipe_container)
        toolbar = findViewById(R.id.change_password_toolbar)
        recyclerView  = findViewById(R.id.profile_recycler_view)
        swipeContainer.setOnRefreshListener { loadFields(true) }
        swipeContainer.setColorSchemeResources(R.color.colorPrimary)
        progressBar = findViewById(R.id.user_detail_progress_bar)
    }
    private fun loadFields(isRefresh:Boolean = false)
    {
        if(!isRefresh){
            progressBar.visibility = View.VISIBLE
        }
        Request(this).get("fields").then { fieldResponse->
            fields = Gson().fromJson(fieldResponse, Array<Field>::class.java)
            Request(this).get("users/${userId}").then { userResponse->
                progressBar.visibility = View.GONE
                swipeContainer.isRefreshing = false
                user = JSONUtil.flattenData(JSONObject(userResponse).get("user") as JSONObject, fields)
                profileAdapter = ProfileAdapter(this@UserDetailActivity,fields, user!!, listener)
                recyclerView.apply {
                    adapter = profileAdapter
                    layoutManager = LinearLayoutManager(this@UserDetailActivity)
                }
            }.catch {
                progressBar.visibility = View.GONE
                Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                swipeContainer.isRefreshing = false
            }
        }.catch {
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false
            Toast.makeText(this@UserDetailActivity,"filed not loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun alertDialog(field:Field,value:String,position: Int)
    {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.set_profile_dialog,null)
        val editText = dialogView.findViewById<EditText>(R.id.dialog_edit_text)
        val title = dialogView.findViewById<TextView>(R.id.title_view)
        editText.showKeyboard()
        openKeyInput(editText,this@UserDetailActivity)
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
            }
            .show()
    }
    private fun updateUserInfo(field:Field,data:String,position:Int)
    {
        val payload = JSONObject()
        if(field.type == "list") {
            payload.put(field.name, JSONArray(data.split(", ")))
        } else {
            payload.put(field.name, data)
        }
        Request(this).put("users/${userId}", payload).then {
            user!!.put(field.name, data)
            profileAdapter.notifyItemChanged(position)
        }.catch {
            toast("failed to update")
        }
    }

    private val listener = object : ProfileInterFace {
        override fun onclick(field: Field, value:String, position:Int,editable:Boolean) {
            if(editable){
                alertDialog(field, value, position)
            }
        }
    }

    private fun Context.toast(message:String) {
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }
    
    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}
