package com.example.sdk.internal.common

import android.content.Context
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset

internal class RootChecker(
    private val context: Context,
    private val rootFiles: Array<String>,
    private val rootPackages: Array<String>,
    private val runtime: Runtime,
) {
    constructor(context: Context) : this(
        context,
        arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/su/bin",
            "/system/xbin/daemonsu",
        ),
        arrayOf(
            "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu", // SuperSU
            "com.noshufou.android.su", // superuser
        ),
        Runtime.getRuntime(),
    )

    /**
     * Check if the device is rooted or not
     * https://medium.com/@thehimanshugoel/10-best-security-practices-in-android-applications-that-every-developer-must-know-99c8cd07c0bb
     *
     * @return whether the device is rooted or not
     */
    fun isDeviceRooted(): Boolean {
        return checkTestKeys() || checkRootFiles() || checkSUExist() || checkRootPackages()
    }

    /**
     * Android Roms from Google are build with release-key tags. If test-keys are present, this can
     * mean that the Android build on the device is either a developer build or an unofficial Google
     * build.
     *
     * @return whether if it contains test keys or not
     */
    private fun checkTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") ?: false
    }

    /**
     * Often the rooted device have the following files . This method will check whether the device is
     * having these files or not
     *
     * @return whether if the root files exist or not
     */
    private fun checkRootFiles(): Boolean {
        for (path in rootFiles) {
            try {
                if (File(path).exists()) {
                    return true
                }
            } catch (_: RuntimeException) {
                // do nothing
            }
        }
        return false
    }

    /**
     * this will check if SU(Super User) exist or not
     *
     * @return whether su exists or not
     */
    private fun checkSUExist(): Boolean {
        var process: Process? = null
        val su = arrayOf("/system/xbin/which", "su")

        try {
            process = runtime.exec(su)

            BufferedReader(
                InputStreamReader(
                    process.inputStream,
                    Charset.forName("UTF-8"),
                ),
            ).use { reader ->
                return reader.readLine() != null
            }
        } catch (_: Throwable) {
            // do nothing
        } finally {
            process?.destroy()
        }
        return false
    }

    /**
     * some application hide the root status of the android device. This will check for those files
     *
     * @return whether the root packages exist or not
     */
    private fun checkRootPackages(): Boolean {
        for (pkg in rootPackages) {
            if (DeviceUtils.getPackageInfo(context, packageName = pkg) != null) {
                return true
            }
        }
        return false
    }
}
