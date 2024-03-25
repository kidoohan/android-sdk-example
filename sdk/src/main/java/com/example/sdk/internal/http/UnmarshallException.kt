package com.example.sdk.internal.http

import java.lang.RuntimeException

/**
 * This exception will be thrown when unmarshalling an object fails.
 *
 * @constructor Constructs a new [UnmarshallException] instance.
 */
class UnmarshallException(
    th: Throwable
) : RuntimeException(th)
