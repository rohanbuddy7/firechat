package com.tribeone.firechat.utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal object FcTimeUtils {

    private var TAG = javaClass.simpleName
    private const val SECOND_MILLIS = 1000
    const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    @SuppressLint("LogNotTimber")
    fun getTimeAgo(serverDate: String, needMonthsValue: Boolean = false): String {

        //val indianFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        //indianFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        //val value = indianFormat.format(timestamp)

        val utcFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = utcFormat.parse(serverDate)
        if (timestamp != null) {
            val time = timestamp.time
            val now = System.currentTimeMillis()
            if (time > now || time <= 0) return ""

            val diff = now - time
            var timeAgo =  if (diff < MINUTE_MILLIS) {
                "just now"
            } else if (diff < 2 * MINUTE_MILLIS) {
                "a minute ago"
            } else if (diff < 50 * MINUTE_MILLIS) {
                "${diff / MINUTE_MILLIS} minutes ago"
            } else if (diff < 2 * HOUR_MILLIS) {
                "an hour ago"
            }  else if (diff < 24 * HOUR_MILLIS) {
                "${diff / HOUR_MILLIS} hours ago"
            } else if (diff < 48 * HOUR_MILLIS) {
                "yesterday"
            } else {
                "${diff / DAY_MILLIS} days ago"
            }

            if(needMonthsValue){

                val calendar = Calendar.getInstance()
                calendar.time = timestamp
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)

                val currentCalendar = Calendar.getInstance()
                val currentYear = currentCalendar.get(Calendar.YEAR)
                val currentMonth = currentCalendar.get(Calendar.MONTH)

                if (year < currentYear) {
                    val interval = currentYear - year
                    timeAgo = if (interval == 1) "$interval year ago" else "$interval years ago"
                } else if (month < currentMonth) {
                    val interval = currentMonth - month
                    timeAgo = if (interval == 1) "$interval month ago" else "$interval months ago"
                }

                return timeAgo

            }

            return timeAgo

        } else {
            Log.e(TAG, "getTimeAgo: time parsing error")
            return serverDate
        }
    }

    fun getDatex(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

}