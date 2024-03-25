package com.example.sdk.internal.concurrent

import android.os.Handler
import android.os.Looper
import com.example.sdk.internal.SdkLogger
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.FutureTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object Executors {
    private val LOG_TAG = Executors::class.java.simpleName

    private const val CORE_POOL_SIZE = 1
    private const val MAXIMUM_POOL_SIZE = Int.MAX_VALUE
    private const val BACKUP_POOL_SIZE = 5
    private const val KEEP_ALIVE_SECONDS = 60L

    // Used only for rejected executions.
    // Initialization protected by sRunOnSerialPolicy lock.
    private var backupExecutor: ThreadPoolExecutor? = null
    private lateinit var backupExecutorQueue: LinkedBlockingQueue<Runnable>

    private val threadFactory: ThreadFactory = object : ThreadFactory {
        private val count: AtomicInteger = AtomicInteger(1)

        override fun newThread(r: Runnable?): Thread {
            return Thread(r, "ExecutorNodeQueue Thread #${count.getAndIncrement()}")
        }
    }

    private val runOnSerialPolicy: RejectedExecutionHandler =
        RejectedExecutionHandler { r, _ ->
            SdkLogger.w(LOG_TAG, "Exceeded ThreadPoolExecutor pool size")
            // As a last ditch fallback, run it on an executor with an unbounded queue.
            // Create this executor lazily, hopefully almost never.
            synchronized(this) {
                if (backupExecutor == null) {
                    backupExecutorQueue = LinkedBlockingQueue()
                    backupExecutor = ThreadPoolExecutor(
                        BACKUP_POOL_SIZE,
                        BACKUP_POOL_SIZE,
                        KEEP_ALIVE_SECONDS,
                        TimeUnit.SECONDS,
                        backupExecutorQueue,
                        threadFactory,
                    ).apply {
                        allowCoreThreadTimeOut(true)
                    }
                }
            }
            backupExecutor?.execute(r)
        }

    val BACKGROUND_EXECUTOR_FOR_QUEUE by lazy {
        ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            threadFactory,
        ).apply {
            rejectedExecutionHandler = runOnSerialPolicy
        }
    }

    /** An [Executor] that executes [ExecutorNode]s on the UI thread. */
    @JvmStatic
    val UI_THREAD_EXECUTOR: Executor by lazy {
        UiThreadExecutor()
    }

    /** An [Executor] that executes [ExecutorNode]s immediately (synchronously) when it is submitted. */
    @JvmStatic
    val IMMEDIATE_EXECUTOR: Executor by lazy {
        ImmediateExecutor()
    }

    /** An [Executor] that executes [ExecutorNode]s in parallel. */
    @JvmStatic
    val BACKGROUND_EXECUTOR: Executor by lazy {
        BackgroundExecutor()
    }

    class UiThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            handler.post(command)
        }
    }

    /**
     * Executor that executes a runnable immediately (synchronously) when it is submitted
     * (when [execute] is called).
     */
    class ImmediateExecutor : Executor {
        /** [command] is exercised on the submitter's thread. */
        override fun execute(command: Runnable) {
            command.run()
        }
    }

    class BackgroundExecutorNode(
        private val executorNodeQueue: ExecutorNodeQueue,
        private val command: Runnable,
    ) : ExecutorNode, Callable<Any> {
        private val futureTask: FutureTask<Any> by lazy {
            object : FutureTask<Any>(this) {
                override fun done() {
                    try {
                        handleSuccess()
                    } catch (e: Exception) {
                        handleError(e)
                    }
                }
            }
        }
        private val isCompleted: AtomicBoolean = AtomicBoolean(false)

        override fun getRunnable(): Runnable {
            return futureTask
        }

        override fun handleError(exception: Exception) {
            executorNodeQueue.remove(this)
            isCompleted.set(true)
        }

        override fun isCompleted(): Boolean {
            return isCompleted.get()
        }

        private fun handleSuccess() {
            executorNodeQueue.remove(this)
            isCompleted.set(true)
        }

        override fun call(): Any? {
            command.run()
            return null
        }
    }

    class BackgroundExecutor : Executor {
        override fun execute(command: Runnable) {
            val deferredQueue = ExecutorNodeQueue.IO_QUEUE
            deferredQueue.enqueue(
                BackgroundExecutorNode(
                    deferredQueue,
                    command,
                ),
            )
        }
    }
}
