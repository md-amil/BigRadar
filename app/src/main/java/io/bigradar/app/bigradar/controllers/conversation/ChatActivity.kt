package io.bigradar.app.bigradar.controllers.conversation

import android.app.NotificationManager
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Ack
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import io.bigradar.app.bigradar.*
import io.bigradar.app.bigradar.adapter.MessageAdapter
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.controllers.App.Companion.activeConversation
import io.bigradar.app.bigradar.interfaces.MessageInterface
import io.bigradar.app.bigradar.libs.Moment
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.conversation.Conversation
import io.bigradar.app.bigradar.models.conversation.Message
import io.bigradar.app.bigradar.models.conversation.Messages
import io.bigradar.app.bigradar.services.NotificationService
import kotlinx.android.synthetic.main.activity_chat.*
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var sendChatBtn: ImageButton
    private lateinit var messageField: EditText
    private lateinit var imm: InputMethodManager
    private lateinit var socket: Socket
    lateinit var  messageAdapter:MessageAdapter
    private lateinit var messages: Messages
    lateinit var notificationService: NotificationService
    private var conversationUserName: String? = null
    private var conversationId: String? = null
    private var userId: String? = null
    private var queuedMessage: Message? = null
    val conversationIdObj = JSONObject()
    private var isTyping = false
    private var isOnline = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        messageAdapter = MessageAdapter(this, longClick)
        initViews()
        initSocket()
        messageListView.apply {
            adapter = messageAdapter
            val lManager = LinearLayoutManager(this@ChatActivity);
            lManager.stackFromEnd = true
            layoutManager = lManager

        }
        setUpToolbar()
        loadAllMessages()
    }

    private fun initSocket() {
        socket = ConversationActivity.getSocket()
        socket.apply {
            on(CONNECT_EVENT, onConnect)
            on(ERROR_EVENT, onError)
            on(USER_JOIN, onUserJoin)
            on(USER_LEAVE, onUserLeave)
            on(TYPING_START_EVENT, onStartTyping)
            on(TYPING_STOP_EVENT, onStopTyping)
            on(MESSAGE_READ, onMessageRead)
            on(CONVERSATION_READ, onConversationRead)
            on(Socket.EVENT_DISCONNECT, onDisconnect)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStart() {
        super.onStart()
        val cId = Conversation.idToNumber(conversationId!!)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(cId)
        socket.emit(EMIT_CONVERSATION_READ, conversationIdObj)
        sendReadConversationBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessageChangeReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        if (activeConversation == ConversationActivity.ConversationType.OPEN) {
            menuInflater.inflate(R.menu.open_chat_option_menu, menu)

        } else if (activeConversation == ConversationActivity.ConversationType.CLOSE) {
            menuInflater.inflate(R.menu.close_chat_option_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_close -> {
                closeConversation()
            }
            R.id.nav_open -> {
                openConversation()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }


    private fun initViews() {
        swipeContainer = findViewById(R.id.chat_swipe_container)
        sendChatBtn = findViewById(R.id.sendChatBtn)
        messageField = findViewById(R.id.sendMessageTxt)
        progressBar = findViewById(R.id.chat_progress_bar)
        toolbar = findViewById(R.id.chat_toolbar)
        sendChatBtn.setOnClickListener { attemptSend() }
        messageField.addTextChangedListener(textChangeListener)

        val bundle = intent.extras
        conversationId = bundle?.getString(CONVERSATION_ID_KEY)
        conversationUserName = App.activeUserName
        userId = App.activeUserId
        conversationIdObj.put("id", conversationId)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            newMessageChangeReceiver, IntentFilter(BROADCAST_NEW_MESSAGE)
        )
        notificationService = NotificationService(this)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        swipeContainer.setOnRefreshListener { loadAllMessages() }
        swipeContainer.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun setUpToolbar() {
        toolbar.title = conversationUserName
        toolbar.isClickable = true
        toolbar.setOnClickListener { goToProfile() }
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        showStatus()
    }

    private fun goToProfile() {
        val profileIntent = Intent(
            this@ChatActivity,
            ProfileActivity::class.java
        )
        profileIntent.putExtra(USER_ID_KEY, userId)
        profileIntent.putExtra(USER_NAME_KEY, conversationUserName)
        startActivity(profileIntent)
    }

    private fun closeConversation() {
        Request(this).put("conversations/${conversationId}/close").then {
            Toast.makeText(this, "conversation closed successfully", Toast.LENGTH_LONG).show()
        }.catch {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun openConversation() {
        Request(this).put("conversations/${conversationId}/open").then {
            Toast.makeText(this, "conversation Open successfully", Toast.LENGTH_SHORT).show()
        }.catch {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadAllMessages() {
        Request(this).get("conversations/$conversationId").then {
            Log.d("conversation", it)
        }.catch {
            Log.d("error", it.message)
        }
        Request(this).get("conversations/$conversationId/messages").then {
            Log.d("messages", it)
            swipeContainer.isRefreshing = false
            progressBar.visibility = View.GONE
            messages = Gson().fromJson(it, Messages::class.java)
            messageAdapter.addAll(messages.data)
            queuedMessage?.let { queued ->
                messageAdapter.add(queued)
            }
            messageListView.scrollToPosition(messageAdapter.itemCount - 1)
        }.catch {
            swipeContainer.isRefreshing = false
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }


    private fun attemptSend() {
        socket.emit(EMIT_TYPING_STOP_EVENT, conversationIdObj)
        val messageToSend = sendMessageTxt.text.toString().trim()

        if (messageToSend.isEmpty()) {
            sendMessageTxt.error = "please enter a message"
            return
        }
        val data = JSONObject()
        data.put("text", messageToSend)
        data.put("conversation_id", conversationId)
        data.put("type", "text")
        val randId = Math.random().toString()
        queuedMessage =
            Message(_id = randId, createdAt = Moment.nowAsIso(), isAuthor = true, text = messageToSend)
        messageAdapter.add(queuedMessage!!)
        messageListView.smoothScrollToPosition(messageAdapter.itemCount)

        socket.emit(EMIT_MESSAGE_EVENT, data, Ack {
            runOnUiThread {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageAdapter.replace(randId, message)
                queuedMessage = null
                messageListView.smoothScrollToPosition(messageAdapter.itemCount)
            }
        })
        sendMessageTxt.text.clear()
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            socket.emit(EMIT_TYPING_START_EVENT, conversationIdObj)
            if (s.toString().trim().isEmpty()) {
                socket.emit(EMIT_TYPING_STOP_EVENT, conversationIdObj)
            }
        }
    }

    private val newMessageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getSerializableExtra(BROADCAST_MESSAGE_KEY) as Message
            if (message.conversation_id == conversationId) {
                messageAdapter.add(message)
                messageListView.scrollToPosition(messageAdapter.itemCount - 1)
                socket.emit(READ_MESSAGE_EVENT, conversationIdObj)
                // or
                // socket.emit(EMIT_MESSAGE_READ,messageIdObj)
            } else if (!message.isAuthor) {
                notificationService.sendNotification(
                    message.user!!.name,
                    message.text,
                    ChatActivity::class.java,
                    message.conversation_id!!,
                    message.user.name,
                    message.user._id!!
                )
            }
        }
    }

    private val onConnect = Emitter.Listener {
        this.runOnUiThread {
            App.isSocketConnected = true
            showStatus()
        }
    }

    private val onDisconnect = Emitter.Listener {
        this.runOnUiThread {
            App.isSocketConnected = false
            showStatus()
        }
    }

    private val onError = Emitter.Listener {
        this.runOnUiThread {
        }
    }

    private val onStartTyping = Emitter.Listener {
        this.runOnUiThread {
            if (it[0] == conversationId) {
                isTyping = true
            }
            showStatus()
        }
    }

    private val onStopTyping = Emitter.Listener {
        this.runOnUiThread {
            val cId = it[0]
            if (cId == conversationId) {
                isTyping = false
            }
            showStatus()
        }
    }

    private val onUserJoin = Emitter.Listener {
        this.runOnUiThread {
            val uId = JSONObject("${it[0]}").getString("user_id")
            if (uId == userId) {
                App.activeUserStatus = 1
            }
            showStatus()
        }
    }

    private val onUserLeave = Emitter.Listener {
        this.runOnUiThread {
            val uId = JSONObject("${it[0]}").getString("user_id")
            if (uId == userId) {
                App.activeUserStatus = 0
                App.activeUserLastSeen = Moment.nowAsIso()
                isTyping = false
            }
            showStatus()
        }
    }
    private val onMessageRead = Emitter.Listener {
        this.runOnUiThread {
            val id = JSONObject("${it[0]}").getString("id")
            messageAdapter.messageRead(id)
        }
    }
    private val onConversationRead = Emitter.Listener {
        this.runOnUiThread {
            val id = JSONObject("${it[0]}").getString("id")
            if (id == conversationId) {
                messageAdapter.conversationRead()
            }
        }
    }



    private fun Context.toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getSubtitle(): String {
        if (!App.isSocketConnected) {
            return "connecting..."
        }else if (isTyping) {
            return "typing..."
        }else if (App.activeUserStatus == 1) {
            return "online"
        } else{
            return "last seen " + Moment(App.activeUserLastSeen).humanize()
        }
    }

    private fun showStatus() {
        toolbar.subtitle = getSubtitle();
    }

    private val longClick = object : MessageInterface {
        override fun onLongClick(msg: String) {
            val clipMan: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("text label", msg)
            clipMan.setPrimaryClip(clip)
            Toast.makeText(this@ChatActivity,"text copied",Toast.LENGTH_SHORT).show()
        }
    }
    private fun sendReadConversationBroadcast(){
        val intent = Intent(BROADCAST_READ_CONVERSATION)
        .putExtra(READ_CONVERSATION_ID, conversationId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }


}