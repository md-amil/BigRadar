package io.bigradar.app.bigradar.controllers.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.heinrichreimersoftware.materialdrawer.DrawerActivity
import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.bigradar.app.bigradar.JSONUtil.buildQuery
import io.bigradar.app.bigradar.R
import io.bigradar.app.bigradar.USER_ID_KEY
import io.bigradar.app.bigradar.USER_NAME_KEY
import io.bigradar.app.bigradar.adapter.UserAdapter
import io.bigradar.app.bigradar.interfaces.UserAdapterInterface
import io.bigradar.app.bigradar.libs.PaginationListener
import io.bigradar.app.bigradar.libs.PaginationListener.Companion.PAGE_START
import io.bigradar.app.bigradar.libs.request.Request
import io.bigradar.app.bigradar.models.Users
import io.bigradar.app.bigradar.models.conversation.Element
import io.bigradar.app.bigradar.models.conversation.Filter
import io.bigradar.app.bigradar.toast


class UserListActivity : AppCompatActivity() {
    private lateinit var searchView: MaterialSearchView
    private lateinit var toolbar: Toolbar
    private lateinit var userAdapter: UserAdapter
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var chipContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeContainer: SwipeRefreshLayout
    private val manager = LinearLayoutManager(this)
    private var currentPage: Int = PAGE_START
    private var isLastPage = false
    private var totalPage: Int? = null
    private var isLoading = false
    private var isFilter = false
    private var activeElement: Element? = null
    private var elements: Array<Element>? = null
    private var queryString: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        initViews()
        initSearchView()
        userAdapter = UserAdapter(this, listener)
        userRecyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = userAdapter
            layoutManager = manager
        }
        loadUsers()
        userRecyclerView.addOnScrollListener(scrollListener)
        swipeContainer.setColorSchemeResources(R.color.colorPrimary)
        swipeContainer.setOnRefreshListener {
            if (isFilter && activeElement != null) {
                getFilterUser(activeElement!!.filters)
            } else {
                clear()
                loadUsers(isRefresh = true)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu) :Boolean{
        menuInflater.inflate(R.menu.user_option_menu, menu)
        val item: MenuItem = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                item.setActionView(R.layout.progress_bar_layout)
                Request(this).get("me/users/filters").then {
                    item.actionView = null
                    elements = Gson().fromJson(it, Array<Element>::class.java)
                    showDialog()
                }.catch {
                    item.actionView = null
                    toast(this, it.message)
                }

            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun showDialog() {
        if (elements == null) {
            return
        }
        val elementName = arrayListOf<String>()
        elementName.clear()
        elements!!.forEach {
            elementName.add(it.name)
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Filters")
        builder.setItems(elementName.toTypedArray()) { _, which ->
            activeElement = elements!![which]
            toolbar.title = activeElement!!.name
            getFilterUser(activeElement!!.filters)
        }
        val dialog = builder.create()
        dialog.show()
    }


    private fun getFilterUser(f: ArrayList<Filter>, q: String? = null) {
        var url = "users?${buildQuery(filters = f, match = activeElement!!.match)}"

        if (q != null) {
            url += buildQuery(q, match = "&")
        }
        Log.d("url", url)
        if (activeElement == null) {
            return
        }

        isFilter = true
        userAdapter.addFilter(f)

        Request(this).get(url).then {
            val res = Gson().fromJson(it, Users::class.java)
            totalPage = res.pages
            userAdapter.clear()
            userAdapter.addAll(res.docs)
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false

        }.catch{
            progressBar.visibility = View.GONE
            swipeContainer.isRefreshing = false
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun initViews(){
        searchView = findViewById(R.id.search_view)
        toolbar = findViewById(R.id.users_toolbar)
        progressBar = findViewById(R.id.user_progress_bar)
        swipeContainer = findViewById(R.id.user_swipe_container)
        userRecyclerView = findViewById(R.id.user_recycler_view)
        chipContainer = findViewById(R.id.chipContainer)
        toolbar.title = "Users"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }


    private fun initSearchView()
    {
        searchView.setOnQueryTextListener( object: MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (isFilter) {
                    clear()
                    getFilterUser(activeElement!!.filters, q = query)
                } else {
                    clear()
                    loadUsers(query, true)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        searchView.setOnSearchViewListener(object: MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                queryString = null
                clear()
                loadUsers(isRefresh = true)
            }
            override fun onSearchViewShown() {
                Log.d("query","shown")
            }
        })
    }

    private fun loadUsers(query: String? = null, isRefresh: Boolean = false) {
        var url = "users?"
        if (query != null) {
            url += buildQuery(q = query, match = "or&")
        } else {
            progressBar.visibility = View.VISIBLE
        }
        url += "page=${currentPage}"
        Log.d("QUERY_PARAM", url)
        Request(this).get(url).then {
            if (isRefresh) {
                userAdapter.clear()
            }
            val res = Gson().fromJson(it, Users::class.java)
            totalPage = res.pages
            if (currentPage != PAGE_START) userAdapter.removeLoading()
            userAdapter.addAll(res.docs)
            swipeContainer.isRefreshing = false
            progressBar.visibility = View.GONE
            if (totalPage == null) {
                return@then
            }
            if (currentPage < totalPage!!) {
                userAdapter.addLoading()
            } else {
                isLastPage = true
            }
            isLoading = false

        }.catch{
            swipeContainer.isRefreshing = false
            progressBar.visibility = View.GONE
        }
    }

    private val listener = object :UserAdapterInterface {
        override fun onClick(uid: String, name: String) {
            val userIntent = Intent(
                this@UserListActivity,
                UserDetailActivity::class.java
            )
            userIntent.putExtra(USER_ID_KEY, uid)
            userIntent.putExtra(USER_NAME_KEY, name)
            startActivity(userIntent)
        }

        override fun onClose(filter: Filter) {
            activeElement!!.filters.remove(filter)
            if (activeElement!!.filters.count() < 1) {
                toolbar.title = "Users"
                isFilter = false
                clear()
                loadUsers()
            } else {
                getFilterUser(activeElement!!.filters)
            }
        }
    }



    private val  scrollListener = object: PaginationListener(manager) {
        override fun loadMoreItems() {
            isLoading = true
            currentPage += 1
//            loadMoreUser()
            loadUsers()
        }

        override fun isLastPage(): Boolean {
            return isLastPage
        }


        override fun isLoading(): Boolean {
            return isLoading
        }

    }

    private fun clear() {
        totalPage = null
        currentPage = PAGE_START
        isLastPage = false
        userAdapter.clear()
        progressBar.visibility = View.VISIBLE
    }

    private fun loadMoreUser() {
        var url = ""
        url += "page=${currentPage}"
        Request(this).get(url).then {

            val res = Gson().fromJson(it, Users::class.java)
            totalPage = res.pages
            if (currentPage != PAGE_START) userAdapter.removeLoading()
            userAdapter.addAll(res.docs)
            swipeContainer.isRefreshing = false
            progressBar.visibility = View.GONE
            if (totalPage == null) {
                return@then
            }
            if (currentPage < totalPage!!) {
                userAdapter.addLoading()
            } else {
                isLastPage = true
            }
            isLoading = false

        }.catch {
            swipeContainer.isRefreshing = false
            progressBar.visibility = View.GONE
        }
    }



}
