package com.example.sdk.internal.concurrent.tasks

/**
 * Similar to ExecutionException only as an unchecked, rather than a checked, exception.
 *
 * @constructor Constructs a new [RuntimeExecutionException] instance.
 */
class RuntimeExecutionException(
    th: Throwable,
) : RuntimeException(th)
