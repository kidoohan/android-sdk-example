package com.example.sdk.internal.http.raw

import com.example.sdk.internal.concurrent.ExecutorNodeItem
import com.google.android.gms.tasks.CancellationToken

/**
 * An HTTP request.
 *
 * @constructor
 * Constructs a new [HttpRequest] instance.
 *
 * @property properties the properties of this request.
 * @property cancellationToken the token that propagates notification that operations should be canceled.
 */
class HttpRequest
@JvmOverloads
constructor(
    val properties: HttpRequestProperties,
    override val cancellationToken: CancellationToken? = null,
) : ExecutorNodeItem(cancellationToken)
