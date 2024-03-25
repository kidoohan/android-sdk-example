package com.example.sdk.internal

import android.content.Context
import com.example.sdk.internal.common.DeviceUtils

data class ApplicationProperties internal constructor(
    val name: String? = null,
    val version: String? = null,
    val packageName: String? = null,
    val installerPackageName: String? = null,
) {
    companion object {
        @JvmStatic
        internal fun create(context: Context): ApplicationProperties {
            val name = DeviceUtils.getApplicationName(context)
            val (version, packageName) = DeviceUtils.getPackageInfo(context)?.let { pkgInfo ->
                pkgInfo.versionName to pkgInfo.packageName
            } ?: (null to null)
            val installerPackageName = DeviceUtils.getInstallerPackageName(context)
            return ApplicationProperties(
                name = name,
                version = version,
                packageName = packageName,
                installerPackageName = installerPackageName,
            )
        }
    }
}
