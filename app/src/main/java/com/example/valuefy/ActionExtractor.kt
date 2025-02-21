package com.example.valuefy

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ActionExtractor {

    fun extractMeetingDetails(transcribedText: String): Pair<Long?, String?> {
        val datePatterns = listOf(
            """\b(\d{1,2}(st|nd|rd|th)? (January|February|March|April|May|June|July|August|September|October|November|December))\b""", // "5th April", "12 March"
            """\b(next|this) (Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\b""", // "next Monday"
            """\b(tomorrow|today|day after tomorrow)\b""" // "tomorrow"
        )

        val timePatterns = listOf(
            """\b(\d{1,2}:\d{2} (AM|PM))\b""", // "5:30 PM"
            """\b(\d{1,2} (AM|PM))\b""" // "5 PM"
        )

        val foundDate: String? = findPatternMatch(transcribedText, datePatterns)
        val foundTime: String? = findPatternMatch(transcribedText, timePatterns)

        Log.d("ActionExtractor", "Found Date: $foundDate, Found Time: $foundTime")

        return if (foundDate != null || foundTime != null) {
            val eventTimestamp = convertToTimestamp(foundDate, foundTime)
            Pair(eventTimestamp, foundTime)
        } else {
            Pair(null, null)
        }
    }

    private fun findPatternMatch(text: String, patterns: List<String>): String? {
        for (pattern in patterns) {
            val regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            val matcher = regex.matcher(text)
            if (matcher.find()) return matcher.group()
        }
        return null
    }

    private fun convertToTimestamp(date: String?, time: String?): Long? {
        val calendar = Calendar.getInstance()

        date?.let {
            when {
                it.contains("tomorrow", true) -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                it.contains("day after tomorrow", true) -> calendar.add(Calendar.DAY_OF_YEAR, 2)
                it.contains("next", true) -> {
                    val dayOfWeek = getDayOfWeek(it.split(" ")[1])
                    if (dayOfWeek != null) {
                        while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    } else {

                    }
                }
                else -> {
                    val dateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
                    try {
                        val parsedDate = dateFormat.parse(it.replace(Regex("(st|nd|rd|th)"), ""))
                        if (parsedDate != null) {
                            calendar.timeInMillis = parsedDate.time // FIXED HERE
                        } else {

                        }
                    } catch (e: Exception) {
                        Log.e("ActionExtractor", "Date parsing failed: ${e.message}")
                    }
                }
            }
        }

        time?.let {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
            try {
                val parsedTime = timeFormat.parse(it)
                if (parsedTime != null) {
                    calendar.set(Calendar.HOUR_OF_DAY, parsedTime.hours)
                    calendar.set(Calendar.MINUTE, parsedTime.minutes)
                } else {

                }
            } catch (e: Exception) {
                Log.e("ActionExtractor", "Time parsing failed: ${e.message}")
            }
        }

        return calendar.timeInMillis
    }

    private fun getDayOfWeek(day: String): Int? {
        return when (day.lowercase(Locale.ROOT)) {
            "sunday" -> Calendar.SUNDAY
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            else -> null
        }
    }
}
