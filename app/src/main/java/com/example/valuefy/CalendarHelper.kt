package com.example.valuefy

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast

class CalendarHelper(private val context: Context) {
    fun addEvent(title: String, description: String, timeInMillis: Long?) {
        if (timeInMillis == null) {
            Toast.makeText(context, "No valid date/time found!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeInMillis + 3600000)
        }
        context.startActivity(intent)
    }
}
