package com.example.sdk.internal.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sdk.internal.image.ImageLoader.enqueue
import com.example.sdk.internal.image.ImageLoader.execute
import com.example.sdk.testutils.TestUtils
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Tests for [com.example.sdk.internal.image.ImageLoader] */
@RunWith(AndroidJUnit4::class)
class ImageLoaderTest {
    private lateinit var context: Context
    private lateinit var mockWebServer: MockWebServer
    private lateinit var baseUrl: String

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockWebServer = MockWebServer().apply {
            start()
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return when (request.path) {
                        "/success.png" -> {
                            MockResponse().setResponseCode(200)
                                .addHeader("Content-Type:image/png")
                                .setBody(
                                    Buffer().apply {
                                        write(
                                            TestUtils.getByteArray(
                                                context,
                                                "image/png_651_502.png",
                                            ),
                                        )
                                    },
                                )
                        }
                        else -> {
                            MockResponse().setResponseCode(400)
                        }
                    }
                }
            }
        }
        baseUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testExecuteTwice_withTimeoutMillis() {
        // first
        run {
            // given
            val fakeImageRequest = ImageRequest(Uri.parse("${baseUrl}success.png"))
            val responseRef = AtomicReference<Bitmap>()
            val latch = CountDownLatch(1)

            // when
            TaskUtils.callInBackgroundThread {
                fakeImageRequest.execute()
            }.addOnCompleteListener {
                responseRef.set(it.result)
                latch.countDown()
            }

            // then
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
            with(responseRef.get()) {
                assertThat(width).isEqualTo(651)
                assertThat(height).isEqualTo(502)
            }
        }

        // second
        run {
            // given
            val fakeImageRequest = ImageRequest(Uri.parse("${baseUrl}success.png"))
            val responseRef = AtomicReference<Bitmap>()
            val latch = CountDownLatch(1)

            // when
            TaskUtils.callInBackgroundThread {
                fakeImageRequest.execute()
            }.addOnCompleteListener {
                responseRef.set(it.result)
                latch.countDown()
            }

            // then
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
            with(responseRef.get()) {
                assertThat(width).isEqualTo(651)
                assertThat(height).isEqualTo(502)
            }
        }
    }

    @Test
    fun testExecuteTwice_withoutException() {
        // given
        val fakeImageRequest1 = ImageRequest(Uri.parse("${baseUrl}success.png"))
        val fakeImageRequest2 = ImageRequest(Uri.parse("${baseUrl}success.png"))

        val responseRef1 = AtomicReference<Bitmap>()
        val responseRef2 = AtomicReference<Bitmap>()
        val latch = CountDownLatch(2)

        // when
        TaskUtils.callInBackgroundThread {
            fakeImageRequest1.execute()
        }.addOnCompleteListener {
            responseRef1.set(it.result)
            latch.countDown()
        }

        TaskUtils.callInBackgroundThread {
            fakeImageRequest2.execute()
        }.addOnCompleteListener {
            responseRef2.set(it.result)
            latch.countDown()
        }

        // then
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
        with(responseRef1.get()) {
            assertThat(width).isEqualTo(651)
            assertThat(height).isEqualTo(502)
        }
        with(responseRef2.get()) {
            assertThat(width).isEqualTo(651)
            assertThat(height).isEqualTo(502)
        }
    }

    @Test
    fun testExecute_withException() {
        val fakeImageRequest = ImageRequest(Uri.parse("${baseUrl}failure.png"))
        val exceptionRef = AtomicReference<Exception>()
        val latch = CountDownLatch(1)

        TaskUtils.callInBackgroundThread {
            fakeImageRequest.execute()
        }.addOnCompleteListener {
            exceptionRef.set(it.exception)
            latch.countDown()
        }

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
        assertThat(exceptionRef.get()).isNotNull()
    }

    @Test
    fun testEnqueue_withAnImageRequest() {
        // given
        val fakeImageRequest = ImageRequest(Uri.parse("${baseUrl}success.png"))

        val responseRef = AtomicReference<Bitmap>()
        val latch = CountDownLatch(1)

        // when
        fakeImageRequest.enqueue(object : ImageCallback {
            override fun onResponse(request: ImageRequest, response: Bitmap) {
                responseRef.set(response)
                latch.countDown()
            }

            override fun onFailure(request: ImageRequest, e: Exception) {
                e.printStackTrace()
                latch.countDown()
            }
        })

        // then
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
        with(responseRef.get()) {
            assertThat(width).isEqualTo(651)
            assertThat(height).isEqualTo(502)
        }
    }

    @Test
    fun testEnqueue_withAListOfImageRequest() {
        // given
        val fakeImageRequest1 = ImageRequest(Uri.parse("${baseUrl}success.png"))
        val fakeImageRequest2 = ImageRequest(Uri.parse("${baseUrl}failure.png"))

        val responseRef = AtomicReference<Bitmap>()
        val exceptionRef = AtomicReference<Exception>()
        val latch = CountDownLatch(2)

        // when
        listOf(fakeImageRequest1, fakeImageRequest2).enqueue(object : ImageCallback {
            override fun onResponse(request: ImageRequest, response: Bitmap) {
                assertThat(request).isEqualTo(fakeImageRequest1)
                if (responseRef.get() == null) {
                    responseRef.set(response)
                } else {
                    Assert.fail()
                }
                latch.countDown()
            }

            override fun onFailure(request: ImageRequest, e: Exception) {
                assertThat(request).isEqualTo(fakeImageRequest2)
                if (exceptionRef.get() == null) {
                    exceptionRef.set(e)
                } else {
                    Assert.fail()
                }
                latch.countDown()
            }
        })

        // then
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
        with(responseRef.get()) {
            assertThat(width).isEqualTo(651)
            assertThat(height).isEqualTo(502)
        }
        assertThat(exceptionRef.get()).isNotNull()

        // using memory cache
        run {
            // given
            val latch2 = CountDownLatch(1)

            // when
            listOf(fakeImageRequest1).enqueue(object : ImageCallback {
                override fun onResponse(request: ImageRequest, response: Bitmap) {
                    assertThat(request).isEqualTo(fakeImageRequest1)
                    responseRef.set(response)
                    latch2.countDown()
                }

                override fun onFailure(request: ImageRequest, e: Exception) {
                    Assert.fail()
                    latch2.countDown()
                }
            })

            // then
            assertThat(latch2.await(10, TimeUnit.SECONDS)).isTrue()
            with(responseRef.get()) {
                assertThat(width).isEqualTo(651)
                assertThat(height).isEqualTo(502)
            }
        }
    }
}
