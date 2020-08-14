package io.bigradar.app.bigradar.controllers.conversation

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.heinrichreimersoftware.materialdrawer.DrawerActivity
import com.heinrichreimersoftware.materialdrawer.structure.DrawerHeaderItem
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem
import com.heinrichreimersoftware.materialdrawer.structure.DrawerProfile
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.MaterialSearchView.OnQueryTextListener
import com.squareup.picasso.Picasso
import io.bigradar.app.bigradar.*
import io.bigradar.app.bigradar.adapter.ConversationAdapter
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.controllers.App.Companion.activeConversation
import io.bigradar.app.bigradar.controllers.auth.LoginActivity
import io.bigradar.app.bigradar.controllers.profile.MyProfileActivity
import io.bigradar.app.bigradar.controllers.user.UserListActivity
import io.bigradar.app.bigradar.interfaces.ConversationInterface
import io.bigradar.app.bigradar.libs.CircleTransform
import io.bigradar.app.bigradar.libs.Moment
import io.bigradar.app.bigradar.libs.request.Error
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.Me
import io.bigradar.app.bigradar.models.conversation.Conversation
import io.bigradar.app.bigradar.models.conversation.Conversations
import io.bigradar.app.bigradar.models.conversation.Message
import io.bigradar.app.bigradar.models.conversation.User
import io.bigradar.app.bigradar.services.NotificationService
import kotlinx.android.synthetic.main.activity_conversation.*
import org.json.JSONObject

class ConversationActivity : AppCompatActivity() {
    companion object {
        private lateinit var socket: Socket

        fun getSocket(): Socket {
            return socket
        }
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: MaterialSearchView
    private lateinit var adminProfile:ImageView
    private lateinit var adminName:TextView
    private lateinit var toolbar:Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var conversationListView: RecyclerView
    private lateinit var swipeContainer: SwipeRefreshLayout
    private val notificationService = NotificationService(this)
    private var me: Me? = null
    private var queryString: String? = null

    enum class ConversationType {
        CLOSE,
        OPEN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        try {
            connect()
            gettingIntent()
            initViews()
            otherStuff()
            setupMe()
            initSearchView()
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun gettingIntent() {
        intent.extras?.let {
            val userId: String? = it.getString(ACTIVE_USER_ID_KEY)
            val name: String = it.getString(ACTIVE_USER_NAME_KEY, "")
            val id: String? = it.getString(ACTIVE_CONVERSATION_ID_KEY)
            if (userId != null && id != null) {
                goToChatActivity(
                    userId!!,
                    name,
                    id!!
                )
            }
        }
    }

    private fun otherStuff() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            profileUpdateBroadcastReceiver, IntentFilter(BROADCAST_PROFILE_UPDATE)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            updateReadConversation, IntentFilter(BROADCAST_READ_CONVERSATION)
        )
        conversationAdapter = ConversationAdapter(this@ConversationActivity, listener)
        conversationListView.apply {
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(this@ConversationActivity)
        }
    }


//    private fun initDrawer() {
//
////        drawerTheme = DrawerTheme(this)
////            .setBackgroundColorRes(R.color.background)
////            .setTextColorPrimaryRes(R.color.colorPrimary)
////            .setTextColorSecondaryRes(R.color.colorPrimaryDark)
//
//        onItemClickListener = DrawerItem.OnItemClickListener { _, _, position ->
//            selectItem(0)
//            when(position){
//                0 -> {
//                    toast("home clicked")
//                }
//                1 -> {
//                    val userIntent = Intent(this@ConversationActivity,
//                        UserListActivity::class.java)
//                    startActivity(userIntent)
//                }
//                2-> {
//                    val userIntent = Intent(this@ConversationActivity,
//                        MyProfileActivity::class.java)
//                    if(me!=null){
//                        userIntent.putExtra(USER_URL,me!!.user.avatar!!)
//                    }
//                    startActivity(userIntent)
//                }
//                3->logout()
//            }
//        }
//
//        App.storage.apply {
//            val profile1 = DrawerProfile()
//                .setRoundedAvatar(this@ConversationActivity,drawableToBitmap(resources.getDrawable(R.drawable.avatar)))
//                .setId(1)
//                .setBackground(ContextCompat.getDrawable(this@ConversationActivity, R.color.colorPrimary) )
//                .setName(myName)
//            profile1.avatar.mutate()
//            val profile2 = DrawerProfile()
//                .setId(2)
//                .setRoundedAvatar(this@ConversationActivity,drawableToBitmap(resources.getDrawable(R.drawable.avatar)))
//                .setBackground(ContextCompat.getDrawable(this@ConversationActivity, R.drawable.background_wallpaper))
//                .setName(myName)
//                .setDescription(getString(R.string.lorem_ipsum_medium))
//            addProfile(profile1)
//            addProfile(profile2)
//        }
//        addItem(
//            DrawerItem().setId(1)
//                .setTextPrimary("Conversations")
//        )
//        addItem(
//            DrawerItem().setId(2)
//                .setTextPrimary("User")
//        )
//        addItem(
//            DrawerItem().setId(3)
//                .setTextPrimary("Profile")
//        )
//        addItem(
//            DrawerItem().setId(4)
//                .setTextPrimary("Logout")
//        )
//        setOnProfileSwitchListener { oldProfile, oldId, newProfile, newId ->
//            Toast.makeText(
//                this@ConversationActivity,
//                "Switched from profile *$oldId to profile *$newId",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }


    private fun disConnect() {
        socket.apply {
            off(CONNECT_EVENT, onConnect)
            off(ERROR_EVENT, onError)
            off(USER_JOIN, onUserJoin)
            off(USER_LEAVE, onUserLeave)
            off(TYPING_START_EVENT, onStartTyping)
            off(TYPING_STOP_EVENT, onStopTyping)
            off(MESSAGE_READ, onMessageRead)
            off(CONVERSATION_READ, onConversationRead)
            off(NEW_CONVERSATION_EVENT, onNewConversation)
//                off(Socket.EVENT_DISCONNECT,onDisConnect)
            off(NEW_MESSAGE_EVENT,onNewMessage)
            disconnect()
        }

    }
    private fun connect(){
        val opts = IO.Options()
        opts.query = "token=${App.storage.authToken}"
        opts.secure = true
        opts.forceNew = true
        opts.reconnection = true
        socket = IO.socket("https://app.bigradar.io", opts)
        socket.connect()
        socket.apply {
            on(Socket.EVENT_DISCONNECT,onDisConnect)
            on(CONNECT_EVENT,onConnect)
            on(ERROR_EVENT,onError)
            on(USER_JOIN,onUserJoin)
            on(USER_LEAVE,onUserLeave)
            on(TYPING_START_EVENT,onStartTyping)
            on(TYPING_STOP_EVENT,onStopTyping)
            on(NEW_MESSAGE_EVENT,onNewMessage)
            on(MESSAGE_READ,onMessageRead)
            on(CONVERSATION_READ,onConversationRead)
            on(NEW_CONVERSATION_EVENT,onNewConversation)
            connect()
        }

    }

    override fun onCreateOptionsMenu(menu:Menu):Boolean{
        menuInflater.inflate(R.menu.conversation_option_menu, menu)
        val item:MenuItem = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        return true
    }


    override fun onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profileUpdateBroadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReadConversation)
        disConnect()
    }

    private fun initViews(){
        progressBar = findViewById(R.id.conversation_progress_bar)
        navigationView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        conversationListView = findViewById(R.id.conversation_list_view)
        searchView = findViewById(R.id.search_view)
        toolbar =findViewById(R.id.my_profile_toolbar)

        swipeContainer = findViewById(R.id.conversation_swipe_container)
        toolbar.title = "Conversations"
        setSupportActionBar(toolbar)
        progressBar.visibility = View.VISIBLE
        swipeContainer.setOnRefreshListener {
            if(activeConversation == ConversationType.OPEN){
                loadAllConversation(isRefresh = true, q = queryString)
            }else if(activeConversation == ConversationType.CLOSE){
                loadCloseConversations(isRefresh = true, q = queryString)
            }
        }
        swipeContainer.setColorSchemeResources(R.color.colorPrimary)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val spinner:Spinner =  findViewById(R.id.spinner)
        val adapter:ArrayAdapter<String> = ArrayAdapter(
            applicationContext, R.layout.spinner_item,
            resources.getStringArray(R.array.conversation_type));
        spinner.adapter = adapter
        spinner.onItemSelectedListener = itemSelected
    }


    private val itemSelected = object:AdapterView.OnItemSelectedListener{

        override fun onNothingSelected(p0: AdapterView<*>?) {
            toast("onNothingSelected")
        }

        override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
            val selectedItem: String = parent?.getItemAtPosition(position).toString()
            if (selectedItem == "Open"){
                progressBar.visibility = View.VISIBLE
                loadAllConversation()
                activeConversation = ConversationType.OPEN
            } else if (selectedItem == "Close") {
                progressBar.visibility = View.VISIBLE
                loadCloseConversations()
                activeConversation = ConversationType.CLOSE
            }
        }
    }

    private fun setupMe() {
        navigationView.setNavigationItemSelectedListener(navigationListener)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val header = navigationView.getHeaderView(0)
        adminProfile = header.findViewById(R.id.admin_profile)
        adminName = header.findViewById(R.id.admin_name)
        App.storage.apply {
            adminName.text = myName
            Picasso.get().load("$myAvatar").
            transform(CircleTransform()).placeholder(R.drawable.avatar).into(adminProfile)
        }
        loadMe()
    }

    private fun loadMe() {
        Request(this).get("profile").then {
            Log.d("me_load", it)
            val user: User = Gson().fromJson(it, User::class.java)
            App.storage.apply {
                myName = user.name
                myEmail = user.email ?: ""
                myPhone = user.phone ?: ""
                myAvatar = user.avatar ?: ""
                adminName.text = myName
                Picasso.get().load(myAvatar).transform(CircleTransform())
                    .placeholder(R.drawable.avatar).into(adminProfile)
            }
        }
    }

    private fun initSearchView()
    {
        searchView.setOnQueryTextListener( object:OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                queryString = query
                if(activeConversation == ConversationType.OPEN){
                    loadAllConversation(query)
                }else if(activeConversation == ConversationType.CLOSE){
                    loadCloseConversations(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                queryString = newText
                if(activeConversation == ConversationType.OPEN){
                    loadAllConversation(newText)
                }else if(activeConversation == ConversationType.CLOSE){
                    loadCloseConversations(newText)
                }
                return true
            }
        })
        searchView.setOnSearchViewListener(object: MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                queryString = null
                if(activeConversation == ConversationType.OPEN){
                    loadAllConversation()
                }else if(activeConversation == ConversationType.CLOSE){
                    loadCloseConversations()
                }
            }
            override fun onSearchViewShown() {
                Log.d("query","shown")
            }
        })
    }

    private fun loadAllConversation(q:String?=null,isRefresh: Boolean = false){
        var url = "/conversations"
        if(q !=null) {
            url += "?query=$q"
        }
        if(!isRefresh){
            progressBar.visibility = View.VISIBLE
        }
        Request(this).get(url).then {
            Log.d("conversations",it)
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false
            val conversations = Gson().fromJson(it, Conversations::class.java).data
            conversationAdapter.addAll(conversations)
        }.catch { e:Error ->
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false
            toast("Error found ${e.message} with error code ${e.statusCode}")
        }
    }

    fun loadCloseConversations(q:String?=null,isRefresh:Boolean = false){
        var url = "/conversations?status=close"
        if(q !=null) {
            url += "&&query=$q"
        }
        if(!isRefresh){
            progressBar.visibility = View.VISIBLE
        }

        Request(this).get(url).then {
            Log.d("conversations", it)
            val conversations = Gson().fromJson(it, Conversations::class.java).data
            conversationAdapter.addAll(conversations)
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false
        }.catch {
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false
        }
    }

    private val listener = object:ConversationInterface {
        override fun onclick(id: String, name: String,userId:String,lastSeen:String?) {
            if(lastSeen != null){
                App.activeUserLastSeen = lastSeen!!
            }
            goToChatActivity(userId,name,id)
        }
    }

    private fun goToChatActivity(userId:String,userName:String,conversationId:String){
        App.activeUserId = userId
        App.activeUserName = userName
        val chatIntent = Intent(
            this@ConversationActivity,
            ChatActivity::class.java)
        chatIntent.putExtra(CONVERSATION_ID_KEY, conversationId)
        startActivity(chatIntent)
    }


    private fun logout()
    {
        Request(this).post("logout").then {
            App.storage.authToken = null
            App.storage.isLoggedIn = false
            disConnect()
            val loginIntent = Intent(this, LoginActivity::class.java).setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(loginIntent)
//            finishAffinity()
        }.catch {
            toast(it.message)
        }
    }


    private fun Context.toast(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    private val onConnect = Emitter.Listener {
        this.runOnUiThread {
            App.isSocketConnected = true
                val amil = it.get(1000)
            Log.d("socket","socket connected")
        }
    }
    private val onDisConnect = Emitter.Listener {
        this.runOnUiThread {
            App.isSocketConnected = true
            Log.d("socket","socket DisConnected")
        }
    }

    private val onError = Emitter.Listener {
        this.runOnUiThread{
            Log.d("socket","on error")
        }
    }
    private val onStartTyping = Emitter.Listener {
        this.runOnUiThread {
            val cId = it[0].toString()
            conversationAdapter.updateTyping(cId, true)
            Log.d("socket", "typing start")
        }
    }
    private val onStopTyping = Emitter.Listener {
        this.runOnUiThread {

            val cId = it[0].toString()
            conversationAdapter.updateTyping(cId, false)
            Log.d("socket", "typing stop!")
        }
    }
    private val onUserJoin = Emitter.Listener {
        this.runOnUiThread{
            val uId = JSONObject("${it[0]}").getString("user_id")
            conversationAdapter.updateStatus(uId,1,false)
            Log.d("socket","user join")
        }
    }
    private val onUserLeave = Emitter.Listener {
        this.runOnUiThread{
            val uId = JSONObject("${it[0]}").getString("user_id")
            conversationAdapter.updateStatus(uId,0,true)
            Log.d("socket","user leave")
        }
    }
    private val onMessageRead = Emitter.Listener {
        this.runOnUiThread{
            Log.d("socket","message read")
        }
    }
    private val onConversationRead = Emitter.Listener {
        this.runOnUiThread{
            Log.d("socket","conversation read")
        }
    }


    @Suppress("DEPRECATION")
    private val onNewMessage = Emitter.Listener {
        this.runOnUiThread {
            try {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val allRunningActivity= am.getRunningTasks(1)
                allRunningActivity.forEach {
                    if(it.topActivity!!.className == CHAT_ACTIVITY){
                        conversationAdapter.update(message,true)
                        val newMessageIntent = Intent(BROADCAST_NEW_MESSAGE)
                        newMessageIntent.putExtra(BROADCAST_MESSAGE_KEY, message)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(newMessageIntent)
                    } else if (!message.isAuthor) {
                        notificationService.sendNotification(
                            message.user!!.name,
                            message.text,
                            ChatActivity::class.java,
                            message.conversation_id!!,
                            message.user!!.name,
                            message.user!!._id!!
                        )
                        conversationAdapter.update(message,false)
                    }
                }
            }catch (e:NullPointerException){
                e.printStackTrace()
            }
        }

    }
    private val onNewConversation = Emitter.Listener {
        this.runOnUiThread{
            Log.d("new conversation",it[0].toString())
            val conversation: Conversation = Gson().fromJson(it[0].toString(), Conversation::class.java)
            conversation.user.lastSeen = Moment.nowAsIso()
            conversation.user.status = 1
            notificationService.sendNotification(
                conversation.user.name,
                conversation.message!!.text,
                ChatActivity::class.java,
                conversation._id,
                conversation.user.name,
                conversation.user._id!!
            )
            if(activeConversation == ConversationType.OPEN){
                conversationAdapter.add(conversation)
                conversationListView.smoothScrollToPosition(0)
            }
        }
    }


    private val profileUpdateBroadcastReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            var name = ""

            intent.extras?.let {
                name = it.getString(PROFILE_UPDATED_NAME) ?: ""
            }
            App.storage.apply {
                myName = name
                adminName.text = myName
            }
        }
    }

    private val updateReadConversation = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.extras?.let {
                conversationAdapter.clearUnreadBadge(it.getString(READ_CONVERSATION_ID)!!)
            }
        }
    }

    private val navigationListener = NavigationView.
    OnNavigationItemSelectedListener{ menuItem ->

        when(menuItem.itemId){
            R.id.nav_conversation -> {
//                    toast("home clicked")
            }
            R.id.nav_user -> {
                val userIntent = Intent(this@ConversationActivity,
                    UserListActivity::class.java)
                startActivity(userIntent)
            }
            R.id.nav_logout->logout()

            R.id.nav_profile->{
                val userIntent = Intent(this@ConversationActivity,
                    MyProfileActivity::class.java)
                if(me!=null){
                    userIntent.putExtra(USER_URL,me!!.user.avatar!!)
                }
                startActivity(userIntent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }
}


