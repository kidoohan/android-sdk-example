package com.example.sdk.internal.common.collection

/**
 * Decorates another [MutableCollection] to synchronize its behavior
 * for a multithreaded environment.
 *
 * Iterators must be manually synchronized:
 *
 * ```java
 * synchronized (coll) {
 *   Iterator it = coll.iterator();
 *   // do stuff with iterator
 * }
 * ```
 *
 * @param E the type of the elements in the collection
 * @constructor
 * Constructor that wraps (not copies).
 *
 * @property collection the collection to decorate, must not be null
 * @property lock the lock object to use, must not be null
 */
open class SynchronizedCollection<E>
@JvmOverloads
constructor(
    private val collection: MutableCollection<E?>,
    protected val lock: Any = Any(),
) : MutableCollection<E?> {
    override val size: Int
        get() {
            synchronized(lock) {
                return decorated().size
            }
        }

    /**
     * Gets the collection being decorated.
     *
     * @return the decorated collection
     */
    protected open fun decorated(): MutableCollection<E?> {
        return collection
    }

    override fun add(element: E?): Boolean {
        synchronized(lock) {
            return decorated().add(element)
        }
    }

    override fun addAll(elements: Collection<E?>): Boolean {
        synchronized(lock) {
            return decorated().addAll(elements)
        }
    }

    override fun clear() {
        synchronized(lock) {
            decorated().clear()
        }
    }

    override fun contains(element: E?): Boolean {
        synchronized(lock) {
            return decorated().contains(element)
        }
    }

    override fun containsAll(elements: Collection<E?>): Boolean {
        synchronized(lock) {
            return decorated().containsAll(elements)
        }
    }

    override fun isEmpty(): Boolean {
        synchronized(lock) {
            return decorated().isEmpty()
        }
    }

    /**
     * Iterators must be manually synchronized.
     *
     * ```java
     * synchronized (coll) {
     *   Iterator it = coll.iterator();
     *   // do stuff with iterator
     * }
     * ```
     *
     * @return an iterator that must be manually synchronized on the collection
     */
    override fun iterator(): MutableIterator<E?> {
        return decorated().iterator()
    }

    override fun remove(element: E?): Boolean {
        synchronized(lock) {
            return decorated().remove(element)
        }
    }

    override fun removeAll(elements: Collection<E?>): Boolean {
        synchronized(lock) {
            return decorated().removeAll(elements.toSet())
        }
    }

    override fun retainAll(elements: Collection<E?>): Boolean {
        synchronized(lock) {
            return decorated().retainAll(elements.toSet())
        }
    }

    override fun equals(other: Any?): Boolean {
        synchronized(lock) {
            return if (other === this) {
                true
            } else {
                decorated() === other
            }
        }
    }

    override fun hashCode(): Int {
        synchronized(lock) {
            return decorated().hashCode()
        }
    }

    override fun toString(): String {
        synchronized(lock) {
            return decorated().toString()
        }
    }
}
