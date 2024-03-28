package com.example.sdk.internal.image.fetch

import java.io.InputStream

sealed class FetchResult

data class InputStreamResult(
    val body: InputStream,
) : FetchResult()
