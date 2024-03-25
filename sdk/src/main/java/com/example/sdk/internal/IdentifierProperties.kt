package com.example.sdk.internal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import com.example.sdk.internal.common.ReflectionUtils
import com.example.sdk.internal.common.TaskUtils
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class IdentifierProperties private constructor(
    val advertisingId: String? = null,
    val isLimitAdTracking: Boolean = false,
    val appSetId: String? = null,
) {
    internal var fetchTime: Long = 0
        private set

    private class AdvertisingIdServiceConnection : ServiceConnection {
        private val consumed = AtomicBoolean(false)
        private val queue: BlockingQueue<IBinder> = LinkedBlockingQueue()

        @get:Throws(InterruptedException::class)
        val binder: IBinder
            get() {
                check(!consumed.compareAndSet(true, true)) { "Binder already consumed" }
                return queue.take()
            }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                if (service != null) {
                    queue.put(service)
                }
            } catch (_: InterruptedException) {
                // do nothing
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    private class AdvertisingIdInfoBinder(private val binder: IBinder) : IInterface {
        @get:Throws(RemoteException::class)
        val advertisingId: String?
            get() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(
                        "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService",
                    )
                    binder.transact(FIRST_TRANSACTION_CODE, data, reply, 0)
                    reply.readException()
                    reply.readString()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

        @get:Throws(RemoteException::class)
        val isLimitAdTracking: Boolean
            get() {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(
                        "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService",
                    )
                    data.writeInt(1)
                    binder.transact(SECOND_TRANSACTION_CODE, data, reply, 0)
                    reply.readException()
                    0 != reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

        override fun asBinder(): IBinder {
            return binder
        }

        companion object {
            private const val FIRST_TRANSACTION_CODE = Binder.FIRST_CALL_TRANSACTION
            private const val SECOND_TRANSACTION_CODE = FIRST_TRANSACTION_CODE + 1
        }
    }

    companion object {
        private val LOG_TAG = IdentifierProperties::class.java.simpleName

        private const val RESULT_SUCCESS = 0
        private const val REFRESH_INTERVAL_MILLIS = (3600 * 1000).toLong()
        private const val ADVERTISING_ID_TIMEOUT_SECONDS = 10L

        private var cachedIdentifierProperties: IdentifierProperties? = null

        @JvmStatic
        internal val EMPTY_IDENTIFIER_PROPERTIES = IdentifierProperties()

        @JvmStatic
        fun getIdentifierProperties(context: Context): Task<IdentifierProperties> {
            return cachedIdentifierProperties?.takeIf {
                System.currentTimeMillis() - it.fetchTime < REFRESH_INTERVAL_MILLIS
            }?.run {
                Tasks.forResult(cachedIdentifierProperties)
            } ?: TaskUtils.callInBackgroundThread {
                cacheAndReturnIdentifierProperties(internalGetIdentifierProperties(context))
            }
        }

        private fun cacheAndReturnIdentifierProperties(
            identifierProperties: IdentifierProperties,
        ): IdentifierProperties {
            identifierProperties.fetchTime = System.currentTimeMillis()
            cachedIdentifierProperties = identifierProperties
            return identifierProperties
        }

        private fun internalGetIdentifierProperties(context: Context): IdentifierProperties {
            val pair: Pair<Boolean, String?> = getAdvertisingIdViaReflection(context)
                ?: getAdvertisingIdViaService(context)
                ?: Pair(false, null)
            return IdentifierProperties(pair.second, pair.first, getAppSetId(context))
        }

        private fun getAdvertisingIdViaReflection(context: Context): Pair<Boolean, String?>? {
            try {
                if (!isGooglePlayServicesAvailable(context)) {
                    return null
                }

                val advertisingIdInfo = ReflectionUtils.callStaticMethod<Any>(
                    "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                    "getAdvertisingIdInfo",
                    ReflectionUtils.ClassParameter.from(Context::class.java, context),
                )
                return Pair(
                    ReflectionUtils.callInstanceMethod(
                        advertisingIdInfo,
                        "isLimitAdTrackingEnabled",
                    ),
                    ReflectionUtils.callInstanceMethod(
                        advertisingIdInfo,
                        "getId",
                    ),
                )
            } catch (e: Exception) {
                SdkLogger.w(LOG_TAG, "Failed to advertising id info by reflection: $e")
            }
            return null
        }

        private fun isGooglePlayServicesAvailable(context: Context): Boolean {
            return RESULT_SUCCESS == ReflectionUtils.callStaticMethod<Int>(
                "com.google.android.gms.common.GooglePlayServicesUtil",
                "isGooglePlayServicesAvailable",
                ReflectionUtils.ClassParameter.from(Context::class.java, context),
            )
        }

        private fun getAdvertisingIdViaService(context: Context): Pair<Boolean, String?>? {
            val connection = AdvertisingIdServiceConnection()
            val intent = Intent("com.google.android.gms.ads.identifier.service.START")
            intent.setPackage("com.google.android.gms")
            val didBind = try {
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            } catch (_: Exception) {
                return null
            }
            if (didBind) {
                try {
                    val advertisingInfo = AdvertisingIdInfoBinder(connection.binder)
                    return Pair(
                        advertisingInfo.isLimitAdTracking,
                        advertisingInfo.advertisingId,
                    )
                } catch (e: Exception) {
                    SdkLogger.w(LOG_TAG, "Failed to get advertising id info by service: $e")
                } finally {
                    context.unbindService(connection)
                }
            }
            return null
        }

        private fun getAppSetId(context: Context): String? {
            return if (ReflectionUtils.isClassAvailable("com.google.android.gms.appset.AppSet")) {
                try {
                    val appSetIdClient: AppSetIdClient = AppSet.getClient(context)
                    val task: Task<AppSetIdInfo> = appSetIdClient.appSetIdInfo
                    val appSetIdInfo =
                        Tasks.await(task, ADVERTISING_ID_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    appSetIdInfo.id
                } catch (e: Exception) {
                    SdkLogger.w(LOG_TAG, "Failed to get AppSetId. ${e.message}")
                    null
                }
            } else {
                null
            }
        }
    }
}
