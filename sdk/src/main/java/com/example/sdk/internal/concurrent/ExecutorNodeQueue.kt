package com.example.sdk.internal.concurrent

import com.example.sdk.internal.Validate
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

/**
 * The queue holding the [ExecutorNode]s.
 *
 * As many [ExecutorNode]s as a given [maxConcurrent] can execute concurrently.
 */
class ExecutorNodeQueue(
    private val maxConcurrent: Int,
    private val executor: Executor,
) {
    private val pendingExecutorNodes = ArrayDeque<ExecutorNode>()
    private val runningExecutorNodes = ArrayDeque<ExecutorNode>()

    @Throws(Exception::class)
    fun enqueue(executorNode: ExecutorNode) {
        synchronized(this) {
            pendingExecutorNodes.add(executorNode)
        }
        dispatchExecutorNode()
    }

    fun enqueue(executorNodes: Collection<ExecutorNode>) {
        Validate.checkCollectionNotEmpty(executorNodes, "executorNodes")
        synchronized(this) {
            executorNodes.forEach { executorNode ->
                pendingExecutorNodes.add(executorNode)
            }
        }
        dispatchExecutorNode()
    }

    fun remove(executorNode: ExecutorNode) {
        synchronized(this) {
            while (true) {
                if (runningExecutorNodes.contains(executorNode)) {
                    runningExecutorNodes.remove(executorNode)
                } else {
                    break
                }
            }
        }
        // Attempt to dispatch other pending work node since this work node is no longer
        // running.
        dispatchExecutorNode()
    }

    private fun dispatchExecutorNode() {
        val executableExecutorNode = ArrayList<ExecutorNode>()
        synchronized(this) {
            while (runningExecutorNodes.size < maxConcurrent && !pendingExecutorNodes.isEmpty()) {
                pendingExecutorNodes.removeLastOrNull()?.run {
                    runningExecutorNodes.add(this)
                    executableExecutorNode.add(this)
                }
            }
        }

        for (executorNode: ExecutorNode in executableExecutorNode) {
            try {
                if (executorNode.isCompleted()) {
                    throw IllegalStateException(
                        "Cannot execute executor node: the executor node has already been executed. " +
                            "(a executor node can be executed only once)",
                    )
                }
                executor.execute(executorNode.getRunnable())
            } catch (e: RejectedExecutionException) {
                executorNode.handleError(InterruptedException("Executor rejected."))
            } catch (e: Exception) {
                executorNode.handleError(RuntimeException("ExecutorService: schedule failed.", e))
            }
        }
    }

    companion object {
        private const val IO_MAX_CONCURRENT = 64

        @JvmField
        val IO_QUEUE = ExecutorNodeQueue(IO_MAX_CONCURRENT, Executors.BACKGROUND_EXECUTOR_FOR_QUEUE)

        @JvmField
        val IMMEDIATE_QUEUE = ExecutorNodeQueue(Int.MAX_VALUE, Executors.IMMEDIATE_EXECUTOR)
    }
}
