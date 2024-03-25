package com.example.sdk.internal.common

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Utilities methods for manipulating dates in iso8601 format. This is much much faster and GC friendly than using SimpleDateFormat so
 * highly suitable if you (un)serialize lots of date objects.
 *
 * Supported parse format: [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm]]
 *
 * @see <a href="http://www.w3.org/TR/NOTE-datetime">this specification</a>
 */
// Date parsing code from Jackson databind ISO8601Utils.java
// https://github.com/FasterXML/jackson-databind/blob/master/src/main/java/com/fasterxml/jackson/databind/util/ISO8601Utils.java
object CalendarUtils {
    /** ID to represent the 'GMT' string, default timezone. */
    private const val GMT_ID = "GMT"

    /** The GMT timezone, prefetched to avoid more lookups. */
    private val TIMEZONE_GMT = TimeZone.getTimeZone(GMT_ID)

    /**
     * Returns a Date object representing this Calendar's time value (millisecond offset from the Epoch").
     *
     * @param timezone the timezone to use.
     * @return a Date representing the time value.
     */
    @JvmOverloads
    @JvmStatic
    fun getCurrentDate(timezone: TimeZone = TIMEZONE_GMT): Date {
        return Calendar.getInstance(timezone).time
    }

    /**
     * Format a date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     *
     * @param date the date to format.
     * @param millis true to include millis precision otherwise false.
     * @param timezone the timezone to use for formatting. (GMT will produce `Z`)
     * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm].
     */
    @JvmOverloads
    @JvmStatic
    fun format(date: Date, millis: Boolean = false, timezone: TimeZone = TIMEZONE_GMT): String {
        val calendar = GregorianCalendar(timezone, Locale.US).apply {
            time = date
        }

        // estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
        var capacity = "yyyy-MM-ddThh:mm:ss".length
        capacity += if (millis) {
            ".sss".length
        } else {
            0
        }
        capacity += if (timezone.rawOffset == 0) {
            "Z".length
        } else {
            "+hh:mm".length
        }
        val formatted = StringBuilder(capacity)

        padInt(formatted, calendar.get(Calendar.YEAR), "yyyy".length)
        formatted.append('-')
        padInt(formatted, calendar.get(Calendar.MONTH) + 1, "MM".length)
        formatted.append('-')
        padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), "dd".length)
        formatted.append('T')
        padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), "hh".length)
        formatted.append(':')
        padInt(formatted, calendar.get(Calendar.MINUTE), "mm".length)
        formatted.append(':')
        padInt(formatted, calendar.get(Calendar.SECOND), "ss".length)
        if (millis) {
            formatted.append('.')
            padInt(formatted, calendar.get(Calendar.MILLISECOND), "sss".length)
        }

        val offset = timezone.getOffset(calendar.timeInMillis)
        if (offset != 0) {
            val hours = abs(offset / (60 * 1000) / 60)
            val minutes = abs(offset / (60 * 1000) % 60)
            formatted.append(if (offset < 0) '-' else '+')
            padInt(formatted, hours, "hh".length)
            formatted.append(':')
            padInt(formatted, minutes, "mm".length)
        } else {
            formatted.append('Z')
        }

        return formatted.toString()
    }

    /**
     * Zero pad a number to a specified length.
     *
     * @param buffer the buffer to use for padding.
     * @param value the integer value to pad if necessary.
     * @param length the length of the string we should zero pad.
     */
    private fun padInt(buffer: StringBuilder, value: Int, length: Int) {
        val strValue = value.toString()
        for (i in length - strValue.length downTo 1) {
            buffer.append('0')
        }
        buffer.append(strValue)
    }
}
