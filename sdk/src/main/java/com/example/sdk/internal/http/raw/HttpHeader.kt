package com.example.sdk.internal.http.raw

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HttpHeader(
    val name: String,
    var value: String?,
) : Parcelable {
    /**
     * Get the comma separated value as an array.
     *
     * @return The value of this header that are separated by a comma.
     */
    fun getValues(): List<String>? {
        return value?.run {
            this.split(",")
        }
    }

    /**
     * Add a new value to the end of this header.
     *
     * @param value The value to add.
     */
    fun addValue(value: String?) {
        if (value != null) {
            this.value = if (this.value != null) {
                this.value.plus(",$value")
            } else {
                value
            }
        }
    }
}
