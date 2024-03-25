package com.example.sdk.internal.http

import com.example.sdk.internal.http.raw.HttpRequestProperties
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.Task

/** An HTTP request wrapper interface. */
interface Request {
    /** the properties for http request of type Deferred */
    val rawRequestProperties: Task<HttpRequestProperties>

    /** A factory for [Request] instances. */
    fun interface Factory {
        /**
         * Returns a new [Request].
         *
         * @param cancellationToken the cancellationToken to cancel this operation.
         */
        fun create(cancellationToken: CancellationToken?): Request
    }
}
