package io.bigradar.app.bigradar

import android.util.Log
import com.google.gson.Gson
import io.bigradar.app.bigradar.libs.Moment
import io.bigradar.app.bigradar.models.Field
import io.bigradar.app.bigradar.models.conversation.Filter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap
import kotlin.collections.count
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.last
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toMutableList

object JSONUtil {
    fun buildQuery(q: String? = null, filters: ArrayList<Filter>? = null, match: String): String {
        if (filters != null) {
            var query: String = filters.joinToString("&") {
                "filters[]=" + Gson().toJson(it)
            }
            query += "match=$match"
            return query
        } else {
            var out = ""
            arrayOf("name").forEach {
                val filter = JSONObject()
                filter.put("condition", "contains")
                filter.put("field", it)
                filter.put("value", q)
                out += "filters[]=${filter}&"
            }
            out += "match=$match"
            return out
        }
    }

    fun getString(data: JSONObject, key: String, def: String = ""): String {
        var obj = data;
        val keys = key.split('.').toMutableList()
        val last = keys.last()
        keys.removeAt(keys.count() - 1)
        for (k in keys) {
            if (!obj.has(k) || obj[k] !is JSONObject) {
                return def
            }
            obj = obj.getJSONObject(k)
        }
        if (!obj.has(last)) {
            return def
        }
        return obj.getString(last)
    }

    fun getArray(data: JSONObject, key: String, def: JSONArray = JSONArray()): Any {
        var obj = data
        val keys = key.split('.').toMutableList()
        val last = keys.last()
        keys.removeAt(keys.count() - 1)
        for (k in keys) {
            if (!obj.has(k) || obj[k] !is JSONObject) {
                return def
            }
            obj = obj.getJSONObject(k)
        }
        if (!obj.has(last)) {
            return def
        }

        val out = obj.optJSONArray(last)
        if(obj.optJSONArray(last)!=null){
            return out!!
        }else{
            return obj.getString(last)
        }
//        return obj.getJSONArray(last)
//        Log.d("json","out "+ )

//        return JSONArray(obj.get(last).toString())
    }

    fun toList(items: JSONArray): Array<String> {
        return Array(items.length()) {
            items.getString(it)
        }
    }

    fun formateDate(d: String): String {
        if(d.isEmpty()){
            return ""
        }
        return Moment(d).format(Moment.FORMAT_DATETIME)
    }

    fun flattenData(data: JSONObject, fields: Array<Field>): MutableMap<String, String> {
        val ret = mutableMapOf<String, String>()
        for( field in fields ) {
            if (field.type == "list") {
//                typeof(getArray(data,field.name))
                try{
                    ret[field.name] = toList(getArray(data, field.name) as JSONArray ).joinToString(", ")
                }catch (e:ClassCastException){
                    ret[field.name] = getArray(data,field.name) as String
                }
                continue
            }

            if (field.type == "date") {
                ret[field.name] = formateDate(getString(data, field.name))
                continue
            }

            if(field.type == "list"){
                try{
                    ret[field.name] = getString(data,field.name)
                }catch (e:JSONException){
                    Log.e("json_util",e.localizedMessage?:"")
                }
            }
            if(field.type == "text" || field.type == "string" || field.type == "number" || field.type == "url"){
                ret[field.name] = getString(data, field.name)
            }
            if(field.type == "select"){
                try{
                    ret[field.name] = getString(data,field.name)
                }catch (e:JSONException){
                    Log.e("json util",e.localizedMessage?:"")
                }

            }
        }
        return ret
    }
}