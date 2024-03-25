package com.example.sdk

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.Sdk
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.Validate

class SdkInitProvider : ContentProvider() {
    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        // super.attachInfo calls onCreate. Fail as early as possible.
        checkContentProviderAuthority(
            Validate.checkNotNull(info, "SdkInitProvider ProviderInfo cannot be null."),
        )
        super.attachInfo(context, info)
    }

    override fun onCreate(): Boolean {
        Validate.checkNotNull(
            context,
            "Context cannot be null.",
        ).applicationContext?.let { applicationContext ->
            Sdk.onCreate(applicationContext)
        } ?: run {
            // This typically happens when `android:sharedUid` is used. In such cases, we postpone
            // initialization altogether, and rely on lazy init.
            SdkLogger.e(LOG_TAG, "Deferring initialization because `applicationContext` is null.")
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        return 0
    }

    companion object {
        private val LOG_TAG = SdkInitProvider::class.java.simpleName

        /** Should match the [SdkInitProvider] authority if `$androidId` is empty. */
        @VisibleForTesting
        internal const val EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY =
            "com.example.sdk.sdkinitprovider"

        /**
         * Check that the content provider's authority does not use sdk package name. If it
         * does, crash in order to alert the developer of the problem before they distribute the app.
         */
        private fun checkContentProviderAuthority(info: ProviderInfo) {
            if (EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY == info.authority) {
                throw IllegalStateException(
                    "Incorrect provider authority in manifest. Most likely " +
                        "due to a missing applicationId variable in application's build.gradle.",
                )
            }
        }
    }
}
