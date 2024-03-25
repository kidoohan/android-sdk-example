package com.example.sdk.internal.inspector.crashevent

import com.example.sdk.internal.http.BaseCall
import com.example.sdk.internal.http.DefaultResponse

internal class CrashEventCall(
    requestFactory: CrashEventRequest.Factory,
) : BaseCall<DefaultResponse>(requestFactory, null) {
    override fun unmarshalResponseBody(body: String): DefaultResponse? {
        return DefaultResponse(body)
    }

    companion object {
        @JvmStatic
        fun create(crashEvent: CrashEvent): CrashEventCall {
            return CrashEventCall(CrashEventRequest.Factory(crashEvent))
        }
    }
}
