package com.example.sdk.internal.http

import java.lang.RuntimeException

/**
 * This exception will be thrown when a request is failed.
 *
 * @constructor Constructs an new [RequestException] instance.
 *
 * @property statusCode the HTTP response status code.
 */
class RequestException(
    val statusCode: Int,
) : RuntimeException()
