package io.bigradar.app.bigradar.libs.request

import android.content.Context
import android.content.Intent
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.bigradar.app.bigradar.controllers.App
import io.bigradar.app.bigradar.controllers.auth.LoginActivity
import io.bigradar.app.bigradar.isNetworkConnected
import org.json.JSONObject


typealias SuccessAck = (body: String) -> Unit

class Request(private val context: Context) {
    private var successCallback: SuccessAck? = null
    private var _headers = HashMap<String, String>()
    private var errorCallback: ((res: Error) -> Unit)? = null

    private fun doRequest(method: Int, url: String, data: JSONObject? = null): Request {
        val r =
            object : StringRequest(method, "https://app.bigradar.io/api/$url", Response.Listener {
                successCallback?.invoke(it)
            }, Response.ErrorListener {

                if (it.networkResponse != null) {
                val statusCode = it.networkResponse.statusCode
                if(statusCode == 401){
                    context.startActivity(Intent(context,LoginActivity::class.java))
                }
                errorCallback?.invoke(
                    Error(it.networkResponse.data.toString(charset("UTF-8")), statusCode = statusCode)
                )
            }else{
                if(!isNetworkConnected(context)){
                    errorCallback?.invoke(
                        Error(null,"No Internet Connection",500)
                    )
                }else{
                    Error(null,"error",400)
                }
            }

        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                _headers["Authorization"] = App.storage.authToken ?: ""
                _headers["Accept"] = "application/json"
                return _headers
            }

            override fun getBody(): ByteArray? {
                if (data == null) {
                    return null
                }
                return data.toString().toByteArray()
            }
        }
        r.retryPolicy = DefaultRetryPolicy(10000, 1, 1.0f)
//        r.setShouldCache(false)
        Volley.newRequestQueue(context).add(r)
        return this
    }

    fun header(key: String, value: String): Request
    {
        _headers[key] = value
        return this
    }

    fun headers(h: HashMap<String, String>): Request
    {
        _headers = h
        return this
    }

    fun get(url: String): Request
    {
        return doRequest(Method.GET, url)
    }

    fun post(url: String, data: JSONObject? = null): Request
    {
        return doRequest(Method.POST, url, data)
    }

    fun post(url: String, data: Map<String, String>): Request
    {
        val json = JSONObject()
        for ((k, v) in data) {
            json.put(k, v)
        }
        return post(url, json)
    }

    fun put(url: String, data: JSONObject? = null): Request
    {
        doRequest(Method.PUT, url, data)
        return this
    }
    fun put(url: String,data:Map<String,String>):Request
    {
        val json = JSONObject()
        for ((k, v) in data) {
            json.put(k, v)
        }
        put(url, json)
        return this
    }

    fun delete(url: String): Request {
        return doRequest(Method.DELETE, url)
    }

    fun then(callback: SuccessAck): Request {
        successCallback = callback
        return this
    }

    fun catch(callback: (err: Error) -> Unit): Request {
        errorCallback = callback
        return this
    }

}