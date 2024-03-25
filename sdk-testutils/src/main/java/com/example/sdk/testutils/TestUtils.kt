package com.example.sdk.testutils

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

object TestUtils {
    /**
     * Converts the entirety of an {@link InputStream} to a byte array.
     *
     * @param inputStream the [InputStream] to be read. the input stream is not closed by this method.
     * @return a byte array containing all of the inputStream's bytes.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(inputStream: InputStream): ByteArray {
        val buffer = ByteArray(1024 * 4)
        val outputStream = ByteArrayOutputStream()
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        return outputStream.toByteArray()
    }

    /**
     * Returns a new [String] constructed by decoding UTF-8 encoded bytes.
     *
     * @param bytes The UTF-8 encoded bytes to decode.
     * @return The string.
     */
    @JvmStatic
    fun fromUtf8Bytes(bytes: ByteArray): String {
        return String(bytes, StandardCharsets.UTF_8)
    }

    /** Returns the bytes of an asset file.  */
    @JvmStatic
    @Throws(IOException::class)
    fun getByteArray(context: Context, fileName: String): ByteArray {
        return toByteArray(getInputStream(context, fileName))
    }

    /** Returns an [InputStream] for reading from an asset file.  */
    @JvmStatic
    @Throws(IOException::class)
    fun getInputStream(context: Context, fileName: String): InputStream {
        return context.resources.assets.open(fileName)
    }

    /** Returns a [String] read from an asset file.  */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun getString(
        context: Context,
        fileName: String,
        replaceWiths: List<Pair<String, String>> = emptyList(),
    ): String {
        var ret = fromUtf8Bytes(getByteArray(context, fileName))
        replaceWiths.forEach { replaceWith ->
            ret = ret.replace(replaceWith.first, replaceWith.second)
        }
        return ret
    }
}
