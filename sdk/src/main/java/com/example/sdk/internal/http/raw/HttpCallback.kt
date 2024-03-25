package com.example.sdk.internal.http.raw

import java.lang.Exception

/**
 * the callback type to notify the result of an http requests.
 */
interface HttpCallback {
    /** Called when the request starts. */
    fun onStart()

    /**
     * Called when the [HttpResponse] is returned by the HTTP server.
     *
     * @param request the http request
     * @param response the response for the http request
     */
    fun onResponse(request: HttpRequest, response: HttpResponse)

    /**
     * Called when the [HttpRequest] call could not be executed due an error.
     *
     * @param request the http request
     * @param exception the reason for request failure.
     */
    fun onFailure(request: HttpRequest, exception: Exception)
}
