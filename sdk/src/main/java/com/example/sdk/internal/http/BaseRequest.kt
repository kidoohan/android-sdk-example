package com.example.sdk.internal.http

import com.example.sdk.internal.concurrent.tasks.CancellationToken

/**
 * A base implementation of [Request]
 *
 * @property cancellationToken the cancellationToken to cancel this request.
 */
abstract class BaseRequest protected constructor(
    protected open val cancellationToken: CancellationToken?,
) : Request
