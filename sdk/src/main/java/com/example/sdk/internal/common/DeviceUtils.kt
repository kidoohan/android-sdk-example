package com.example.sdk.internal.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import java.util.Locale

object DeviceUtils {
    /**
     * Check whether the application is running in an emulator.
     *
     * https://github.com/getsentry/sentry-java/blob/d81684e2d1825564da8d0e52454c0e3ede5cd514/sentry-android-core/src/main/java/io/sentry/android/core/BuildInfoProvider.java#L51
     *
     * @return true if the application is running in an emulator, false otherwise
     */
    @JvmStatic
    fun isEmulator(): Boolean {
        return runCatching {
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.PRODUCT.contains("sdk_google") ||
                Build.PRODUCT.contains("google_sdk") ||
                Build.PRODUCT.contains("sdk_x86") ||
                Build.PRODUCT.contains("vbox86p") ||
                Build.PRODUCT.contains("emulator") ||
                Build.PRODUCT.contains("simulator")
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getNetworkCarrierName(context: Context): String? {
        return getTelephonyManager(context)?.networkOperatorName
    }

    @JvmStatic
    fun getCountry(context: Context): String {
        return getLocale(context).country
    }

    @JvmStatic
    fun getLanguage(context: Context): String {
        return getLocale(context).language
    }

    @JvmStatic
    fun getDisplayMetricsDensity(context: Context): Float {
        return getDisplayMetrics(context).density
    }

    @JvmStatic
    fun getDisplayWidthInPixels(context: Context): Int {
        return getDisplayMetrics(context).widthPixels
    }

    @JvmStatic
    fun getDisplayHeightInPixels(context: Context): Int {
        return getDisplayMetrics(context).heightPixels
    }

    @SuppressWarnings("kotlin:S1874")
    @Suppress("DEPRECATION")
    @JvmStatic
    fun getLocale(context: Context): Locale {
        return getResources(context).configuration.let { configuration ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.locales.get(0)
            } else {
                configuration.locale
            }
        }
    }

    @Suppress("DEPRECATION", "kotlin:S1874")
    @JvmStatic
    fun getTelephonyManager(context: Context): TelephonyManager? {
        return context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }

    @JvmStatic
    fun getConnectivityManager(context: Context): ConnectivityManager? {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    @JvmStatic
    fun getWindowManager(context: Context): WindowManager? {
        return context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    @Suppress("DEPRECATION", "kotlin:S1874")
    @SuppressLint("QueryPermissionsNeeded")
    @JvmStatic
    fun canHandleIntent(context: Context, intent: Intent): Boolean {
        return runCatching {
            context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        }.getOrDefault(false)
    }

    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return getResources(context).displayMetrics
    }

    fun getResources(context: Context): Resources {
        return context.resources
    }

    @JvmStatic
    fun getApplicationName(context: Context): String {
        val packageManager = context.packageManager
        val applicationInfo = context.applicationInfo
        return applicationInfo.loadLabel(packageManager).toString()
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION", "kotlin:S1874")
    @JvmOverloads
    @JvmStatic
    fun getPackageInfo(
        context: Context,
        flags: Number = 0,
        packageName: String = context.packageName,
    ): PackageInfo? {
        val packageManager = context.packageManager
        return runCatching {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(flags.toLong()),
                )
            } else {
                packageManager.getPackageInfo(packageName, flags.toInt())
            }
        }.getOrNull()
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION", "kotlin:S1874")
    @JvmStatic
    @Throws(PackageManager.NameNotFoundException::class)
    fun getApplicationInfo(context: Context, flags: Number): ApplicationInfo {
        val packageManager = context.packageManager
        val packageName = context.packageName

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(flags.toLong()),
            )
        } else {
            packageManager.getApplicationInfo(packageName, flags.toInt())
        }
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION", "kotlin:S1874")
    @JvmStatic
    fun getInstallerPackageName(context: Context): String? {
        val packageManager = context.packageManager
        val packageName = context.packageName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } else {
            packageManager.getInstallerPackageName(packageName)
        }
    }

    @JvmStatic
    fun getRequestedOrientation(context: Context): Int? {
        return if (context is Activity) {
            context.requestedOrientation
        } else {
            null
        }
    }

    @JvmStatic
    fun setRequestedOrientation(context: Context, activityOrientation: Int) {
        if (context is Activity) {
            context.requestedOrientation = activityOrientation
        }
    }

    @JvmStatic
    fun getActivityInfoOrientation(context: Context): Int? {
        return getRotation(context)?.let { rotation ->
            val resources = getResources(context)
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    when (rotation) {
                        Surface.ROTATION_90,
                        Surface.ROTATION_180,
                        -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    when (rotation) {
                        Surface.ROTATION_180,
                        Surface.ROTATION_270,
                        -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
                else -> null
            }
        }
    }

    @Suppress("DEPRECATION", "kotlin:S1874")
    @JvmStatic
    fun getRotation(context: Context): Int? {
        return (context as? Activity)?.let { activity ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display?.rotation
            } else {
                activity.windowManager.defaultDisplay.rotation
            }
        }
    }
}
