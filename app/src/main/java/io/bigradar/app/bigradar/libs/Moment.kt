package io.bigradar.app.bigradar.libs

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

const val ONE_DAY = 1000 * 60 * 60 * 24

class Moment(private val dateStr: String) {

    companion object {
        val FORMAT_DATE = "d MMM"
        val FORMAT_DATE_LONG = "d MMM, YYYY"
        val FORMAT_TIME = "h:mm a"
        val FORMAT_DATETIME = "d MMM h:mm a"
        val PATTERN = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'"
        var isoDateFormatter: SimpleDateFormat? = null
        fun nowAsIso():String {
            val tz = TimeZone.getTimeZone("UTC")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf.timeZone = tz
            return sdf.format(Date())
        }
    }

    private val date: Date

    init {
        if (isoDateFormatter == null) {
            isoDateFormatter = SimpleDateFormat(
                PATTERN, Locale.getDefault())
            isoDateFormatter!!.timeZone = TimeZone.getTimeZone("UTC")
        }

        if (dateStr.isEmpty()) {
            date = Date();
        } else {
            date = isoDateFormatter!!.parse(dateStr) ?: Date()
        }

    }

    fun format(formatter: String): String {
        return SimpleDateFormat(formatter, Locale.getDefault()).format(date)
    }

    val isToday: Boolean
        get() {
            val diff = Calendar.getInstance().timeInMillis - Calendar.getInstance().run {
                time = date
                timeInMillis
            }
            return diff < ONE_DAY
        }

    val isYesterday: Boolean
        get() {
            return false; // yet to be implemented
        }

    fun humanize(): String {
        if (isToday) {
            return format(FORMAT_TIME)
        }
        return format(FORMAT_DATE)
    }

    override fun toString(): String {
        return format(FORMAT_DATE);
    }


}
