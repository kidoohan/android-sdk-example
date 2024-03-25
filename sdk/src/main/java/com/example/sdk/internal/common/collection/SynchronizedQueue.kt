package com.example.sdk.internal.common.collection

import java.util.Queue

/**
 * Decorates another [Queue] to synchronize its behavior for a multithreaded environment.
 *
 * Methods are synchronized, then forwarded to the decorated queue. Iterators must be separately synchronized around the
 * loop.
 *
 * @param E the type of the elements in the collection
 * @constructor Constructor that wraps (not copies).
 *
 * @param queue the queue to decorate, must not be null
 * @param lock the lock to use, must not be null
 */
open class SynchronizedQueue<E>
@JvmOverloads
constructor(
    queue: Queue<E?>,
    lock: Any = Any(),
) : SynchronizedCollection<E>(queue, lock), Queue<E> {
    /**
     * Gets the queue being decorated.
     *
     * @return the decorated queue
     */
    override fun decorated(): Queue<E?> {
        return super.decorated() as (Queue<E?>)
    }

    override fun element(): E? {
        synchronized(lock) {
            return decorated().element()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        synchronized(lock) {
            return decorated() == other
        }
    }

    override fun hashCode(): Int {
        synchronized(lock) {
            return decorated().hashCode()
        }
    }

    override fun offer(e: E?): Boolean {
        synchronized(lock) {
            return decorated().offer(e)
        }
    }

    override fun peek(): E? {
        synchronized(lock) {
            return decorated().peek()
        }
    }

    override fun poll(): E? {
        synchronized(lock) {
            return decorated().poll()
        }
    }

    override fun remove(): E? {
        synchronized(lock) {
            return decorated().remove()
        }
    }
}
