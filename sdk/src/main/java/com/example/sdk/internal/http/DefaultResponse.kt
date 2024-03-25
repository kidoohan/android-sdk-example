package com.example.sdk.internal.http

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The response containing only raw body that are not deserialized
 *
 * @constructor Constructs a new [DefaultResponse] instance.
 *
 * @property rawBody the body that are not deserialized.
 */
@Parcelize
data class DefaultResponse(
    val rawBody: String,
) : Parcelable
