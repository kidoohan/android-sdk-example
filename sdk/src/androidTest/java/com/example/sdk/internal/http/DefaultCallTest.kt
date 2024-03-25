package com.example.sdk.internal.http

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sdk.internal.http.raw.HttpRequestProperties
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for [DefaultCall] */
@RunWith(AndroidJUnit4::class)
class DefaultCallTest {
    private lateinit var context: Context
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockWebServer = MockWebServer().apply {
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testExecute_withoutError() {
        // given
        mockWebServer.enqueue(
            MockResponse().setHeader("header", "value").setBody("foo").setResponseCode(200),
        )
        val requestProperties = HttpRequestProperties.Builder()
            .uri(Uri.parse(mockWebServer.url("/").toString()))
            .build()
        val caller = DefaultCall.create(requestProperties)

        // when
        val response = caller.execute()

        // then
        assertThat(caller.isCancellationRequested()).isFalse()
        with(response) {
            assertThat(rawResponse.statusCode).isEqualTo(200)
            assertThat(rawResponse.headers.getValue("header")).isEqualTo("value")
            assertThat(rawResponse.getBodyAsString()).isEqualTo("foo")
            assertThat(body.rawBody).isEqualTo("foo")
        }
    }
}
