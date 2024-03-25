package com.example.sdk.internal

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * Static convenience methods that help a method or constructor check whether it was invoked correctly
 * (whether its preconditions have been met).
 */
object Validate {
    /**
     * Ensure the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails
     * @throws IllegalArgumentException if [expression] is false
     */
    @JvmOverloads
    @JvmStatic
    fun checkArgument(expression: Boolean, errorMessage: String = "Failed check.") {
        if (!expression) {
            throw IllegalArgumentException(errorMessage)
        }
    }

    /**
     * Ensures the truth of an expression involving the state of the calling instance, but not
     * involving any parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails
     * @throws IllegalStateException if `expression` is false
     */
    @JvmOverloads
    @JvmStatic
    fun checkState(expression: Boolean, errorMessage: String = "Failed check.") {
        if (!expression) {
            throw IllegalStateException(errorMessage)
        }
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param errorMessage the exception message to use if the check fails
     * @return the non-null reference that was validated
     * @throws NullPointerException if [reference] is null
     */
    @JvmOverloads
    @JvmStatic
    fun <T> checkNotNull(reference: T?, errorMessage: String = "Required value was null."): T {
        if (reference == null) {
            throw NullPointerException(errorMessage)
        }
        return reference
    }

    /**
     * Ensures that a [value] passed as a parameter to the calling method is strictly greater
     * than [lowerBoundExclusive].
     *
     * @param value a comparable value
     * @param lowerBoundExclusive the exclusive lower bound
     * @param errorMessage the exception message to use if the check fails
     * @return the reference that was validated
     * @throws IllegalArgumentException if `value` is less than or equal to
     * `lowerBoundExclusive`
     */
    @JvmOverloads
    @JvmStatic
    fun <T : Comparable<T>> checkGreaterThan(
        value: T,
        lowerBoundExclusive: T,
        errorMessage: String = "$value is less than or equal to $lowerBoundExclusive",
    ): T {
        if (value <= lowerBoundExclusive) {
            throw IllegalArgumentException(errorMessage)
        }
        return value
    }

    /**
     * Ensures that a [value] passed as a parameter to the calling method is greater than or
     * equal to [lowerBoundInclusive].
     *
     * @param value a comparable value
     * @param lowerBoundInclusive the inclusive lower bound
     * @param errorMessage the exception message to use if the check fails
     * @return the reference that was validated
     * @throws IllegalArgumentException if `value` is less than `lowerBoundInclusive`
     */
    @JvmOverloads
    @JvmStatic
    fun <T : Comparable<T>> checkGreaterThanOrEqualTo(
        value: T,
        lowerBoundInclusive: T,
        errorMessage: String = "$value is less than $lowerBoundInclusive",
    ): T {
        if (value < lowerBoundInclusive) {
            throw IllegalArgumentException(errorMessage)
        }
        return value
    }

    /**
     * Ensures that a [value] passed as a parameter to the calling method is strictly less
     * than [upperBoundExclusive].
     *
     * @param value a comparable value
     * @param upperBoundExclusive the exclusive upper bound
     * @param errorMessage the exception message to use if the check fails
     * @return the reference that was validated
     * @throws IllegalArgumentException if `value` is greater than or equal to
     * `upperBoundExclusive`
     */
    @JvmOverloads
    @JvmStatic
    fun <T : Comparable<T>> checkLessThan(
        value: T,
        upperBoundExclusive: T,
        errorMessage: String = "$value is greater than or equal to $upperBoundExclusive",
    ): T {
        if (value >= upperBoundExclusive) {
            throw IllegalArgumentException(errorMessage)
        }
        return value
    }

    /**
     * Ensures that a `value` passed as a parameter to the calling method is less than or
     * equal to `upperBoundInclusive`.
     *
     * @param value a comparable value
     * @param upperBoundInclusive the inclusive upper bound
     * @param errorMessage the exception message to use if the check fails
     * @return the reference that was validated
     * @throws IllegalArgumentException if `value` is greater than `upperBoundInclusive`
     */
    @JvmOverloads
    @JvmStatic
    fun <T : Comparable<T>> checkLessThanOrEqualTo(
        value: T,
        upperBoundInclusive: T,
        errorMessage: String = "$value is greater than $upperBoundInclusive",
    ): T {
        if (value > upperBoundInclusive) {
            throw IllegalArgumentException(errorMessage)
        }
        return value
    }

    /**
     * Ensures that the [Collection] is not `null`, and none of its elements are
     * `null`.
     *
     * @param value a [Collection] of boxed objects
     * @param valueName the name of the argument to use if the check fails
     * @return the validated [Collection]
     * @throws NullPointerException if the `value` or any of its elements were `null`
     */
    @JvmStatic
    fun <C : Collection<T?>, T> checkCollectionElementsNotNull(
        value: C?,
        valueName: String,
    ): C {
        val nonNullValue = checkNotNull(value, "$valueName must not be null.")
        for ((ctr, elem) in nonNullValue.withIndex()) {
            checkNotNull(elem, "$valueName[$ctr] must not be null.")
        }
        return nonNullValue
    }

    /**
     * Ensures that the [Collection] is not `null`, and contains at least one element.
     *
     * @param value a [Collection] of boxed elements.
     * @param valueName the name of the argument to use if the check fails.
     * @return the validated [Collection]
     * @throws NullPointerException if the `value` was `null`
     * @throws IllegalArgumentException if the `value` was empty
     */
    @JvmStatic
    fun <C : Collection<T>, T> checkCollectionNotEmpty(
        value: C?,
        valueName: String,
    ): C {
        val nonNullValue = checkNotNull(value, "$valueName must not be null.")
        checkArgument(nonNullValue.isNotEmpty(), "$valueName is empty.")
        return nonNullValue
    }

    /**
     * Ensures that the [Map] is not 'null', and contains at least one entry.
     *
     * @param value a [Map].
     * @param valueName the name of the argument to use if the check fails.
     * @return the validated [Map]
     * @throws NullPointerException if the `value` was `null`
     * @throws IllegalArgumentException if the `value` was empty
     */
    @JvmStatic
    fun <M : Map<K, V>, K, V> checkMapNotEmpty(
        value: M?,
        valueName: String,
    ): M {
        val nonNullValue = checkNotNull(value, "$valueName must not be null.")
        checkArgument(nonNullValue.isNotEmpty(), "$valueName is empty.")
        return nonNullValue
    }

    /**
     * Ensures that an string reference passed as a parameter to the calling method is not blank.
     *
     * @param string a [String] value
     * @param errorMessage the exception message to use if the check fails
     * @return the validated [String]
     * @throws IllegalArgumentException if the `string` is blank
     */
    @JvmOverloads
    @JvmStatic
    fun checkStringNotBlank(
        string: String?,
        errorMessage: String = "String is null or blank.",
    ): String {
        if (string.isNullOrBlank()) {
            throw IllegalArgumentException(errorMessage)
        }
        return string
    }

    /**
     * Checks if the current thread is not the main thread, otherwise throws.
     *
     * @param errorMessage the exception message to use if the check fails.
     * @throws IllegalStateException if current thread is the main thread.
     */
    @JvmOverloads
    @JvmStatic
    fun checkNotMainThread(
        errorMessage: String = "Must not be called on the main application thread.",
    ) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw IllegalStateException(errorMessage)
        }
    }

    /**
     * Check if application have the given [permission].
     *
     * @param context the context.
     * @param permission the permissions you want to check.
     * @return the `true` if application have permission, `false` otherwise.
     */
    @JvmStatic
    fun hasPermission(context: Context?, permission: String): Boolean {
        return context?.checkCallingOrSelfPermission(permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
