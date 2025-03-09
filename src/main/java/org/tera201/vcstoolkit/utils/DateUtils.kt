package org.tera201.vcstoolkit.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class DateUtils {
    companion object {
        fun timestampToLocalDate(timestamp: Int): LocalDate {
            val instant: Instant = Instant.ofEpochSecond(timestamp.toLong())
            return instant.atZone(ZoneId.systemDefault()).toLocalDate()
        }

        fun timestampToLocalDateTime(timestamp: Int): LocalDateTime {
            val instant = Instant.ofEpochSecond(timestamp.toLong())
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        }


        fun timestampToZonedDateTime(timestamp: Int): ZonedDateTime {
            val instant: Instant = Instant.ofEpochSecond(timestamp.toLong())
            return instant.atZone(ZoneId.systemDefault())
        }

        fun getDayOfWeek(timestamp: Int): DayOfWeek {
            val date = timestampToLocalDate(timestamp)
            return date.dayOfWeek
        }

        fun getDayOfMouth(timestamp: Int): Int {
            val date = timestampToLocalDate(timestamp)
            return date.dayOfMonth
        }

        fun getMonthOfYear(timestamp: Int): Month {
            val date = timestampToLocalDate(timestamp)
            return date.month
        }

        fun getHourOfDay(timestamp: Int): Int {
            val dateTime: LocalDateTime = timestampToLocalDateTime(timestamp)
            return dateTime.hour
        }

        fun getStringDate(timestamp: Int): String {
            val date = timestampToLocalDate(timestamp)
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            return date.format(formatter)
        }
    }
}