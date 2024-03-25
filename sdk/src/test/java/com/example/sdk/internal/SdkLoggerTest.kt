package com.example.sdk.internal

import android.util.Log
import com.example.sdk.robolectricutils.SdkExampleTestCase
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.lang.IllegalStateException
import java.net.UnknownHostException

/** Tests for [SdkLogger] */
@Config(shadows = [ShadowLog::class])
class SdkLoggerTest : SdkExampleTestCase() {
    @Test
    fun `test logger created by constructor`() {
        val actual = SdkLogger("tag")
        actual.append("a")
        assertThat(actual.message).isEqualTo("a")

        actual.append(StringBuilder("b").append("c"))
        assertThat(actual.message).isEqualTo("abc")

        actual.append("d%s%s-%d", "e", "f", 1)
        assertThat(actual.message).isEqualTo("abcdef-1")

        actual.log()
        with(ShadowLog.getLogsForTag("SDK.tag")[0]) {
            assertThat(type).isEqualTo(Log.DEBUG)
            assertThat(msg).isEqualTo("abcdef-1")
        }
        assertThat(actual.message).isEqualTo("")

        actual.log()
        with(ShadowLog.getLogsForTag("SDK.tag")[1]) {
            assertThat(type).isEqualTo(Log.DEBUG)
            assertThat(msg).isEqualTo("")
        }

        actual.priority = SdkLogger.LogLevel.ERROR
        actual.appendWithKeyValue("key1", "value1")
        actual.appendWithKeyValue("key2", "value2")
        actual.appendWithKeyValue("key3", "value3")
        assertThat(actual.message).isEqualTo("key1\tvalue1\nkey2\tvalue2\nkey3\tvalue3\n")

        actual.log()
        with(ShadowLog.getLogsForTag("SDK.tag")[2]) {
            assertThat(type).isEqualTo(Log.ERROR)
            assertThat(msg).isEqualTo("key1\tvalue1\nkey2\tvalue2\nkey3\tvalue3\n")
        }
    }

    @Test
    fun `test LogLevel`() {
        assertThat(SdkLogger.LogLevel.parse(2)).isEqualTo(SdkLogger.LogLevel.VERBOSE)
        assertThat(SdkLogger.LogLevel.parse(3)).isEqualTo(SdkLogger.LogLevel.DEBUG)
        assertThat(SdkLogger.LogLevel.parse(4)).isEqualTo(SdkLogger.LogLevel.INFO)
        assertThat(SdkLogger.LogLevel.parse(5)).isEqualTo(SdkLogger.LogLevel.WARN)
        assertThat(SdkLogger.LogLevel.parse(6)).isEqualTo(SdkLogger.LogLevel.ERROR)
        assertThat(SdkLogger.LogLevel.parse(7)).isEqualTo(SdkLogger.LogLevel.NONE)
        assertThat(SdkLogger.LogLevel.parse(Int.MAX_VALUE)).isNull()

        assertThat(SdkLogger.LogLevel.parse(SdkLogger.LogLevel.VERBOSE.code)).isEqualTo(
            SdkLogger.LogLevel.VERBOSE,
        )
        assertThat(SdkLogger.LogLevel.parse(SdkLogger.LogLevel.DEBUG.code)).isEqualTo(
            SdkLogger.LogLevel.DEBUG,
        )
        assertThat(SdkLogger.LogLevel.parse(SdkLogger.LogLevel.INFO.code)).isEqualTo(
            SdkLogger.LogLevel.INFO,
        )
        assertThat(SdkLogger.LogLevel.parse(SdkLogger.LogLevel.WARN.code)).isEqualTo(
            SdkLogger.LogLevel.WARN,
        )
        assertThat(SdkLogger.LogLevel.parse(SdkLogger.LogLevel.ERROR.code)).isEqualTo(
            SdkLogger.LogLevel.ERROR,
        )
        assertThat(SdkLogger.LogLevel.parse(SdkLogger.LogLevel.NONE.code)).isEqualTo(
            SdkLogger.LogLevel.NONE,
        )
    }

    @Test
    fun `test Logger_v`() {
        SdkLogger.v("verbose", null)
        assertThat(ShadowLog.getLogsForTag("SDK.verbose")).hasSize(0)

        SdkLogger.v("verbose", "foo", null, 1, 2)
        with(ShadowLog.getLogsForTag("SDK.verbose")[0]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("foo")
        }

        SdkLogger.v("verbose", "", "aaa")
        with(ShadowLog.getLogsForTag("SDK.verbose")[1]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("")
        }

        SdkLogger.v("verbose", "%s %d %f", "foo", 100, 3f)
        with(ShadowLog.getLogsForTag("SDK.verbose")[2]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("foo 100 3.000000")
        }

        SdkLogger.v("verbose", "name: %s")
        with(ShadowLog.getLogsForTag("SDK.verbose")[3]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("name: %s")
        }

        SdkLogger.v("verbose", "name: %s", null)
        with(ShadowLog.getLogsForTag("SDK.verbose")[4]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("name: null")
        }

        SdkLogger.v("verbose", "name: %s", IllegalStateException("foo"))
        with(ShadowLog.getLogsForTag("SDK.verbose")[5]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).startsWith("name: java.lang.IllegalStateException: foo")
        }

        SdkLogger.v("verbose", "name", IllegalStateException("foo"))
        with(ShadowLog.getLogsForTag("SDK.verbose")[6]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("name")
        }

        SdkLogger.v(
            "verbose",
            "name: %s",
            IllegalStateException("foo"),
            IllegalStateException("bar"),
        )
        with(ShadowLog.getLogsForTag("SDK.verbose")[7]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).startsWith("name: java.lang.IllegalStateException: foo")
        }

        SdkLogger.v(
            "verbose",
            "first: %s \nsecond: %s",
            IllegalStateException("foo"),
            IllegalStateException("bar"),
        )
        with(ShadowLog.getLogsForTag("SDK.verbose")[8]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).contains("first: java.lang.IllegalStateException: foo")
            assertThat(msg).contains("second: java.lang.IllegalStateException: bar")
        }

        SdkLogger.v("verbose", "name: %s", UnknownHostException("foo"))
        with(ShadowLog.getLogsForTag("SDK.verbose")[9]) {
            assertThat(type).isEqualTo(Log.VERBOSE)
            assertThat(msg).isEqualTo("name: UnknownHostException (no network)")
        }
    }

    @Test
    fun `test Logger_d`() {
        SdkLogger.d("debug", "foo")
        with(ShadowLog.getLogsForTag("SDK.debug")[0]) {
            assertThat(type).isEqualTo(Log.DEBUG)
            assertThat(msg).isEqualTo("foo")
        }
    }

    @Test
    fun `test Logger_i`() {
        SdkLogger.i("info", "foo")
        with(ShadowLog.getLogsForTag("SDK.info")[0]) {
            assertThat(type).isEqualTo(Log.INFO)
            assertThat(msg).isEqualTo("foo")
        }
    }

    @Test
    fun `test Logger_w`() {
        SdkLogger.w("warn", "foo")
        with(ShadowLog.getLogsForTag("SDK.warn")[0]) {
            assertThat(type).isEqualTo(Log.WARN)
            assertThat(msg).isEqualTo("foo")
        }
    }

    @Test
    fun `test Logger_e`() {
        SdkLogger.e("error", "foo")
        with(ShadowLog.getLogsForTag("SDK.error")[0]) {
            assertThat(type).isEqualTo(Log.ERROR)
            assertThat(msg).isEqualTo("foo")
        }
    }
}
