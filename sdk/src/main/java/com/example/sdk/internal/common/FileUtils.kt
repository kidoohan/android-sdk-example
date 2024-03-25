package com.example.sdk.internal.common

import com.example.sdk.internal.Sdk
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.util.regex.Pattern

internal object FileUtils {
    private const val DIR = "SdkExample"

    /** Returns the directory of SdkExample */
    @JvmStatic
    fun getDir(): File? {
        return Sdk.safeGetApplicationContext()?.cacheDir?.let { cacheDir ->
            File(cacheDir, DIR).takeIf { it.exists() || it.mkdirs() }
        }
    }

    /**
     * Returns the file. if does not exist or is not a file, null is returned.
     *
     * @param fileName the fileName
     * @return the file that associates [fileName].
     */
    @JvmStatic
    fun getFile(fileName: String): File? {
        return runCatching {
            File(getDir(), fileName).takeIf { it.exists() && !it.isDirectory }
        }.getOrDefault(null)
    }

    /**
     * Writes a String to a file.
     *
     * @param fileName the fileName to write.
     * @param content the content to write.
     * @return `true` if the file is successfully wrote, `false` otherwise.
     */
    @JvmStatic
    fun writeFile(fileName: String, content: String): Boolean {
        return runCatching {
            getDir()?.let { dir ->
                val file = File(dir, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                true
            } ?: false
        }.getOrDefault(false)
    }

    /**
     * Deletes the file.
     *
     * @param fileName the fileName to delete.
     * @return `true` if the file is successfully deleted, `false` otherwise.
     */
    @JvmStatic
    fun deleteFile(fileName: String): Boolean {
        return runCatching {
            getFile(fileName)?.delete() ?: false
        }.getOrDefault(false)
    }

    /**
     * Reads the content of a File into a String. If the file does not exist or is not a file,
     * null is returned.
     *
     * @param fileName the fileName to read.
     * @return a String containing all the content of the file, or null if it doesn't exists.
     */
    @JvmStatic
    fun readFile(fileName: String): String? {
        return runCatching {
            getFile(fileName)?.let { file ->
                readFile(file)
            }
        }.getOrDefault(null)
    }

    /**
     * Reads the content of a File into a String. If the file does not exist or is not a file,
     * null is returned.
     *
     * @param file the file to read.
     * @return a String containing all the content of the file, or null if it doesn't exists.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFile(file: File): String? {
        return if (!file.exists() || !file.isFile || !file.canRead()) {
            null
        } else {
            val sb = StringBuilder()
            BufferedReader(FileReader(file)).use { br ->
                var line: String?
                // The first line doesn't need the leading \n
                if (br.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                while (br.readLine().also { line = it } != null) {
                    sb.append("\n").append(line)
                }
            }
            sb.toString()
        }
    }

    /**
     * Reads the content of a File into a [JSONObject]. If the file does not exist or is not a file,
     * null is returned.
     *
     * @param fileName the fileName to read.
     * @return a [JSONObject] containing all the content of the file, or null if it doesn't exists.
     */
    fun readFileAsJSONObject(fileName: String): JSONObject? {
        return runCatching {
            readFile(fileName)?.let { content ->
                JSONObject(content)
            }
        }.getOrDefault(null)
    }

    @JvmStatic
    internal fun getWithTimestampJsonFilesByPrefix(
        prefix: String,
        remainedTimeSeconds: Int,
    ): Array<File> {
        return getDir()?.listFiles { _, name ->
            val matcher = Pattern.compile("^$prefix(\\d+).json").matcher(name)
            if (matcher.find()) {
                matcher.group(1)?.toIntOrNull()?.let { timestamp ->
                    (timestamp > (System.currentTimeMillis() / 1000 - remainedTimeSeconds)).also { isValid ->
                        if (!isValid) {
                            deleteFile(name)
                        }
                    }
                } ?: run {
                    deleteFile(name)
                    false
                }
            } else {
                false
            }
        } ?: arrayOf()
    }
}
