package com.example.sdk.internal.http

import com.google.android.gms.tasks.CancellationToken

/**
 * A base implementation of [Request]
 *
 * @property cancellationToken the cancellationToken to cancel this request.
 */
abstract class BaseRequest protected constructor(
    protected open val cancellationToken: CancellationToken?,
) : Request 
