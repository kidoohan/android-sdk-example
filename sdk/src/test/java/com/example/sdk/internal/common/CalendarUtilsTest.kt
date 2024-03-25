package com.example.sdk.internal.common

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

/** Tests for [CalendarUtils]  */
class CalendarUtilsTest {
    private lateinit var date: Date
    private lateinit var dateZeroMillis: Date
    private lateinit var dateZeroSecondAndMillis: Date
    private lateinit var dateWithoutTime: Date

    @Before
    fun setUp() {
        var calendar = GregorianCalendar(2019, 10 - 1, 3, 23, 21, 30).apply {
            timeZone = TimeZone.getTimeZone("GMT")
            set(Calendar.MILLISECOND, 789)
        }
        date = calendar.time

        calendar.set(Calendar.MILLISECOND, 0)
        dateZeroMillis = calendar.time

        calendar.set(Calendar.SECOND, 0)
        dateZeroSecondAndMillis = calendar.time

        calendar = GregorianCalendar(2021, 2 - 1, 7, 0, 0, 0).apply {
            timeZone = TimeZone.getTimeZone("GMT")
            set(Calendar.MILLISECOND, 0)
        }
        dateWithoutTime = calendar.time
    }

    @Test
    fun `test format`() {
        // millis is false, timezone is GMT
        assertThat(CalendarUtils.format(date)).isEqualTo("2019-10-03T23:21:30Z")
        assertThat(CalendarUtils.format(dateZeroMillis)).isEqualTo("2019-10-03T23:21:30Z")
        assertThat(CalendarUtils.format(dateZeroSecondAndMillis)).isEqualTo("2019-10-03T23:21:00Z")
        assertThat(CalendarUtils.format(dateWithoutTime)).isEqualTo("2021-02-07T00:00:00Z")

        // millis is true, timezone is GMT
        assertThat(CalendarUtils.format(date, true)).isEqualTo("2019-10-03T23:21:30.789Z")
        assertThat(CalendarUtils.format(dateZeroMillis, true)).isEqualTo("2019-10-03T23:21:30.000Z")
        assertThat(
            CalendarUtils.format(
                dateZeroSecondAndMillis,
                true,
            ),
        ).isEqualTo("2019-10-03T23:21:00.000Z")
        assertThat(
            CalendarUtils.format(
                dateWithoutTime,
                true,
            ),
        ).isEqualTo("2021-02-07T00:00:00.000Z")

        // with timezone with plus offset
        run {
            val timezone = TimeZone.getTimeZone("GMT+02:00")
            assertThat(
                CalendarUtils.format(
                    date,
                    false,
                    timezone,
                ),
            ).isEqualTo("2019-10-04T01:21:30+02:00")
            assertThat(
                CalendarUtils.format(
                    date,
                    true,
                    timezone,
                ),
            ).isEqualTo("2019-10-04T01:21:30.789+02:00")
        }

        // with timezone with minus offset
        run {
            val timezone = TimeZone.getTimeZone("GMT-02:00")
            assertThat(
                CalendarUtils.format(
                    date,
                    false,
                    timezone,
                ),
            ).isEqualTo("2019-10-03T21:21:30-02:00")
            assertThat(
                CalendarUtils.format(
                    date,
                    true,
                    timezone,
                ),
            ).isEqualTo("2019-10-03T21:21:30.789-02:00")
        }
    }
}
