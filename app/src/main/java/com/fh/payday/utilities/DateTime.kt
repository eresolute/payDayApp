package com.fh.payday.utilities

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*


class DateTime {

    companion object {
        const val REGEX_MM_YY = "(0?[1-9]|[12][0-9]|3[01])[/.-](\\d\\d)"
        //const val REGEX_DD_MM_YYYY = "(0?[1-9]|1[012])[/.-](0?[1-9]|[12][0-9]|3[01])[/.-]((19|20)\\d\\d)"

        @JvmOverloads
        fun parse(
            text: String?,
            inputFormat: String = "yyyy-MM-dd",
            outputFormat: String = "dd/MM/yyyy"
        ): String {
            return try {
                text?.let {
                    val iFormatter = DateTimeFormat.forPattern(inputFormat)
                    val oFormatter = DateTimeFormat.forPattern(outputFormat).withLocale(Locale.ENGLISH)
                    oFormatter.print(iFormatter.parseDateTime(it))
                } ?: ""
            } catch (e: IllegalArgumentException) {
                ""
            }
        }

        @JvmOverloads
        fun parse(
            text: String?,
            inputFormat: String = "yyyy-MM-dd",
            outputFormat: String = "dd/MM/yyyy",
            dateTimeZone: DateTimeZone
        ): String {
            return try {
                text?.let {
                    val iFormatter = DateTimeFormat.forPattern(inputFormat)
                    val oFormatter = DateTimeFormat.forPattern(outputFormat).withLocale(Locale.ENGLISH)
                    oFormatter.print(iFormatter.parseDateTime(it).withZone(dateTimeZone))
                } ?: ""
            } catch (e: IllegalArgumentException) {
                ""
            }
        }

        @JvmOverloads
        fun getMonth(
            text: String,
            inputFormat: String = "yyyy-MM-dd"
        ): String {
            val date = DateTimeFormat.forPattern(inputFormat).parseDateTime(text)
            return date.toString("MMM")
        }

        fun date(day: Int, month: Int, year: Int, outputFormat: String = "dd/MM/yyyy"): String {
            return try {
                val date = DateTime().withDate(year, month, day)
                val formatter = DateTimeFormat.forPattern(outputFormat)
                formatter.print(date)
            } catch (e: IllegalArgumentException) {
                ""
            }
        }

        @JvmOverloads
        fun isValidDateRange(fromDate: String?, toDate: String?, format: String = "yyyy-MM-dd"): Boolean {
            fromDate ?: return false
            toDate ?: return false

            return try {
                DateTimeFormat.forPattern(format).parseDateTime(fromDate) <= DateTimeFormat.forPattern(format).parseDateTime(toDate)
            } catch (e: Exception) {
                false
            }
        }

        @JvmOverloads
        fun isValid(text: String?, format: String = "yyyy-MM-dd") = try {
            DateTimeFormat.forPattern(format).parseDateTime(text)
            true
        } catch (e: Exception) {
            false
        }

        @JvmOverloads
        fun currentDate(format: String = "dd/MM/yyyy"): String {
            val now = DateTime.now()
            val formatter = DateTimeFormat.forPattern(format)
            return formatter.print(now)
        }

        @JvmOverloads
        fun firstDayOfCurrentMonth(format: String = "dd/MM/yyyy"): String {
            val firstDay = DateTime.now().dayOfMonth().withMinimumValue()
            val formatter = DateTimeFormat.forPattern(format)
            return formatter.print(firstDay)
        }

        @JvmOverloads
        fun currentDayOfLastSixMonths(format: String = "dd/MM/yyyy"): String {
            val now = DateTime.now()
            val formatter = DateTimeFormat.forPattern(format)
            val currentDayOfLastSixMonths = now.minusMonths(5)
            return formatter.print(currentDayOfLastSixMonths)
        }

        @JvmOverloads
        fun firstDayOfMonth(str: String, format: String = "yyyy-MM-dd"): String {
            val formatter = DateTimeFormat.forPattern(format)

            val date = DateTime.parse(str, DateTimeFormat.forPattern("MM/yyyy")).dayOfMonth().withMinimumValue()
            return formatter.print(date)
        }

        @JvmOverloads
        fun lastDayOfMonth(str: String, format: String = "yyyy-MM-dd"): String {
            val formatter = DateTimeFormat.forPattern(format)

            val date = DateTime.parse(str, DateTimeFormat.forPattern("MM/yyyy")).dayOfMonth().withMaximumValue()
            return formatter.print(date)
        }

        @JvmOverloads
        fun lastDayOfCurrentMonth(format: String = "dd/MM/yyyy"): String {
            val lastDay = DateTime.now().dayOfMonth().withMaximumValue()
            val formatter = DateTimeFormat.forPattern(format)
            return formatter.print(lastDay)
        }

        @JvmOverloads
        fun isAfter(
            text: String?,
            format: String = "dd/MM/yyyy",
            dateTime: DateTime = DateTime.now()
        ) = try {
            val date = DateTimeFormat.forPattern(format).parseDateTime(text)
            date.isAfter(dateTime)
        } catch (e: Exception) {
            false
        }

        @JvmOverloads
        fun isBefore(
            text: String?,
            format: String = "dd/MM/yyyy",
            dateTime: DateTime = DateTime.now()
        ) = try {
            val date = DateTimeFormat.forPattern(format).parseDateTime(text)
            date.isBefore(dateTime)
        } catch (e: Exception) {
            false
        }

        @JvmOverloads
        fun parseDate(text: String?, format: String = "yyyy-MM-dd"): DateTime = try {
            when {
                text.isNullOrEmpty() -> DateTime.now()
                else -> DateTimeFormat.forPattern(format).parseDateTime(text)
            }
        } catch (e: Exception) {
            DateTime.now()
        }


        @JvmOverloads
        fun Calendar.convertToString(format: String = "yyyy-MM-dd"): String {
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            return sdf.format(this.time)
        }

        @JvmOverloads
        fun isToday(date: String?, format: String = "yyyy-MM-dd"): Boolean = try {
            LocalDate.now().compareTo(LocalDate.parse(date, DateTimeFormat.forPattern(format))) == 0
        } catch (e: Exception) {
            false
        }
    }

}