package org.tera201.vcstoolkit.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateCalculator(var dateStart: Date, var dateEnd: Date) {

    val textSearch: String
        get() {
            val start = ModelDate(dateStart)
            val end = ModelDate(dateEnd)
            val date = if (start.year != end.year) {
                "$start - $end"
            } else if (start.month != end.month) {
                start.toStringNoYear() + " - " + end.toString()
            } else if (start.day != end.day) {
                start.toStringNoYear() + " - " + end.toStringNoMonth()
            } else {
                start.toString()
            }
            return date
        }

    val differenceDays: Long
        get() {
            val diff = dateEnd.time - dateStart.time
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        }

    private inner class ModelDate(var date: Date) {
        var day: Int
        var month: Int
        var year: Int

        init {
            val cal = Calendar.getInstance()
            cal.time = date
            day = cal[Calendar.DATE]
            month = cal[Calendar.MONTH] + 1
            year = cal[Calendar.YEAR]
        }

        override fun toString(): String {
            val df = SimpleDateFormat("dd MMM, yyyy")
            return df.format(date)
        }

        fun toStringNoYear(): String {
            val df = SimpleDateFormat("dd MMM")
            return df.format(date)
        }

        fun toStringNoMonth(): String {
            val df = SimpleDateFormat("dd, yyyy")
            return df.format(date)
        }
    }
}
