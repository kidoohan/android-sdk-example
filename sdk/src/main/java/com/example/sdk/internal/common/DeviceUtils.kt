package com.example.sdk.internal.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.Locale

object DeviceUtils {
    private val LOG_TAG = DeviceUtils::class.java.simpleName

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
}
