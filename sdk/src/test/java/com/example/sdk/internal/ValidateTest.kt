package com.example.sdk.internal

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test
import org.robolectric.Shadows

/** Tests for [Validate]. */
class ValidateTest {
    @Test
    fun `test checkArgument`() {
        Assert.assertThrows("Failed check.", IllegalArgumentException::class.java) {
            Validate.checkArgument(false)
        }
        Assert.assertThrows("error message", IllegalArgumentException::class.java) {
            Validate.checkArgument(false, "error message")
        }
        Validate.checkArgument(true, "error message")
    }

    @Test
    fun `test checkState`() {
        Assert.assertThrows("Failed check.", IllegalStateException::class.java) {
            Validate.checkState(false)
        }
        Assert.assertThrows("error message", IllegalStateException::class.java) {
            Validate.checkState(false, "error message")
        }
        Validate.checkState(true, "error message")
    }

    @Test
    fun `test checkNotNull`() {
        Assert.assertThrows("Required value was null.", NullPointerException::class.java) {
            Validate.checkNotNull(null)
        }
        Assert.assertThrows("error message", NullPointerException::class.java) {
            Validate.checkNotNull(null, "error message")
        }
        val expected = Any()
        assertThat(Validate.checkNotNull(expected)).isEqualTo(expected)
    }

    @Test
    fun `test checkGreaterThan`() {
        Assert.assertThrows("3 is less than or equal to 4", IllegalArgumentException::class.java) {
            Validate.checkGreaterThan(3, 4)
        }
        Assert.assertThrows("3 is less than or equal to 3", IllegalArgumentException::class.java) {
            Validate.checkGreaterThan(3, 3)
        }
        assertThat(Validate.checkGreaterThan("c", "b")).isEqualTo("c")
        assertThat(Validate.checkGreaterThan(10L, 8L)).isEqualTo(10L)
        assertThat(Validate.checkGreaterThan(10f, 8f)).isEqualTo(10f)
        assertThat(Validate.checkGreaterThan(100.0, 80.0)).isEqualTo(100.0)
    }

    @Test
    fun `test checkGreaterThanOrEqualTo`() {
        Assert.assertThrows("3 is less than to 4", IllegalArgumentException::class.java) {
            Validate.checkGreaterThanOrEqualTo(3, 4)
        }
        Assert.assertThrows("a is less than to b", IllegalArgumentException::class.java) {
            Validate.checkGreaterThanOrEqualTo("a", "b")
        }
        assertThat(Validate.checkGreaterThanOrEqualTo(3L, 3L)).isEqualTo(3L)
        assertThat(Validate.checkGreaterThanOrEqualTo(100f, 99f)).isEqualTo(100f)
        assertThat(Validate.checkGreaterThanOrEqualTo("fff", "ffa")).isEqualTo("fff")
    }

    @Test
    fun `test checkLessThan`() {
        Assert.assertThrows(
            "4 is greater than or equal to 3",
            IllegalArgumentException::class.java,
        ) {
            Validate.checkLessThan(4, 3)
        }
        Assert.assertThrows(
            "3 is greater than or equal to 3",
            IllegalArgumentException::class.java,
        ) {
            Validate.checkLessThan(3, 3)
        }
        Assert.assertThrows(
            "abc is greater than or equal to abc",
            IllegalArgumentException::class.java,
        ) {
            Validate.checkLessThan("abc", "abc")
        }
        assertThat(Validate.checkLessThan("b", "c")).isEqualTo("b")
        assertThat(Validate.checkLessThan(8L, 19L)).isEqualTo(8L)
        assertThat(Validate.checkLessThan(99f, 100f)).isEqualTo(99f)
        assertThat(Validate.checkLessThan(1000.0, 1001.0)).isEqualTo(1000.0)
    }

    @Test
    fun `test checkLessThanOrEqualTo`() {
        Assert.assertThrows("100 is greater than to 99", IllegalArgumentException::class.java) {
            Validate.checkLessThanOrEqualTo(100, 99)
        }
        Assert.assertThrows("b is greater than to a", IllegalArgumentException::class.java) {
            Validate.checkLessThanOrEqualTo("b", "a")
        }
        assertThat(Validate.checkLessThanOrEqualTo(3L, 3L)).isEqualTo(3L)
        assertThat(Validate.checkLessThanOrEqualTo(99f, 100f)).isEqualTo(99f)
        assertThat(Validate.checkLessThanOrEqualTo("ffa", "fff")).isEqualTo("ffa")
    }

    @Test
    fun `test checkCollectionElementsNotNull`() {
        val nullSet: Set<String>? = null
        Assert.assertThrows("set must not be null.", NullPointerException::class.java) {
            Validate.checkCollectionElementsNotNull(nullSet, "set")
        }

        Assert.assertThrows("set[1] must not be null.", NullPointerException::class.java) {
            Validate.checkCollectionElementsNotNull(setOf("aaa", null, "bbb"), "set")
        }

        assertThat(Validate.checkCollectionElementsNotNull(emptyList<Int>(), "list")).isEmpty()
        assertThat(
            Validate.checkCollectionElementsNotNull(listOf("foo"), "list"),
        ).containsExactly("foo")
    }

    @Test
    fun `test checkCollectionNotEmpty`() {
        val nullList: List<String>? = null
        Assert.assertThrows("list must not be null.", NullPointerException::class.java) {
            Validate.checkCollectionNotEmpty(nullList, "list")
        }
        Assert.assertThrows("set is empty", IllegalArgumentException::class.java) {
            Validate.checkCollectionNotEmpty(emptySet<Float>(), "set")
        }
        assertThat(Validate.checkCollectionNotEmpty(listOf("foo"), "list")).containsExactly("foo")
    }

    @Test
    fun `test checkMapNotEmpty`() {
        val nullMap: Map<String, Int>? = null
        Assert.assertThrows("map must not be null.", NullPointerException::class.java) {
            Validate.checkMapNotEmpty(nullMap, "map")
        }
        Assert.assertThrows("map is empty", IllegalArgumentException::class.java) {
            Validate.checkMapNotEmpty(emptyMap<String, String>(), "map")
        }
        assertThat(Validate.checkMapNotEmpty(mapOf("a" to "b"), "map")).containsExactly("a", "b")
    }

    @Test
    fun `test checkStringNotBlank`() {
        val nullString: String? = null
        Assert.assertThrows("String is null or blank", IllegalArgumentException::class.java) {
            Validate.checkStringNotBlank(nullString)
        }
        Assert.assertThrows("String is null or blank", IllegalArgumentException::class.java) {
            Validate.checkStringNotBlank("")
        }
        assertThat(Validate.checkStringNotBlank("aaa")).isEqualTo("aaa")
    }

    @Test
    fun `test hasPermission`() {
        val application: Application = ApplicationProvider.getApplicationContext()
        val shadowApplication = Shadows.shadowOf(application)

        shadowApplication.grantPermissions(Manifest.permission.INTERNET)
        assertThat(Validate.hasPermission(application, Manifest.permission.INTERNET)).isTrue()
        assertThat(
            Validate.hasPermission(
                application,
                Manifest.permission.READ_PHONE_STATE,
            ),
        ).isFalse()
    }
}
