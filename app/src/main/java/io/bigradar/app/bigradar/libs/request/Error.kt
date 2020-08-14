package io.bigradar.app.bigradar.libs.request

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class Error(val response: String?, msg:String? = null, val statusCode: Int) {
    var message: String = ""
    var status: String = "fail"
    init {
        if(response !=null){
            try {
                val json = JSONObject(response)
                if (json.has("message")) {
                    message = json.getString("message")
                }
                if (json.has("status")) {
                    status = json.getString("status")
                }
            } catch (e: JSONException) {
                message = e.message ?: response
            }
        }else{
            message = msg!!
        }
    }
}