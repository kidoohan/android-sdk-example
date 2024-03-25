package com.example.sdk.internal.common.collection

import com.example.sdk.internal.Validate
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Arrays
import java.util.Queue

/**
 * CircularFifoQueue is a first-in first-out queue with a fixed size that
 * replaces its oldest element if full.
 *
 * The removal order of a [CircularFifoQueue] is based on the
 * insertion order; elements are removed in the same order in which they
 * were added.  The iteration order is the same as the removal order.
 *
 * The [add], [remove], [peek], [poll], [offer] operations all perform in constant time.
 * All other operations perform in linear time or worse.
 *
 * This queue prevents null objects from being added.
 *
 * @param E the type of elements in this collection
 */
class CircularFifoQueue<E>
@JvmOverloads
constructor(
    size: Int = 32
) : AbstractMutableCollection<E?>(), Queue<E?>, Serializable {
    init {
        Validate.checkGreaterThan(size, 0, "The size must be greater than 0.")
    }

    constructor(coll: Collection<E>) : this(coll.size) {
        addAll(coll)
    }

    /** Underlying storage array. */
    @Transient
    @Suppress("UNCHECKED_CAST")
    private var elements: Array<E?> = arrayOfNulls<Any>(size) as Array<E?>

    /** Array index of first (oldest) queue element.  */
    @Transient
    private var start = 0

    /**
     * Index mod maxElements of the array position following the last queue
     * element.  Queue elements start at elements[start] and "wrap around"
     * elements[maxElements-1], ending at elements[decrement(end)].
     * For example, elements = {c,a,b}, start=1, end=1 corresponds to
     * the queue [a,b,c].
     */
    @Transient
    private var end = 0

    /** Flag to indicate if the queue is currently full.  */
    @Transient
    private var full = false

    /** Capacity of the queue. */
    private val maxElements = elements.size

    /**
     * Write the queue out using a custom routine.
     *
     * @param outputStream the output stream
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    @Throws(IOException::class)
    private fun writeObject(outputStream: ObjectOutputStream) {
        outputStream.defaultWriteObject()
        outputStream.writeInt(size)
        for (e in this) {
            outputStream.writeObject(e)
        }
    }

    /**
     * Read the queue in using a custom routine.
     *
     * @param inputStream the input stream
     * @throws IOException if an I/O error occurs while writing to the output stream
     * @throws ClassNotFoundException if the class of a serialized object can not be found
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        elements = arrayOfNulls<Any>(maxElements) as Array<E?>
        val size = inputStream.readInt()
        for (i in 0 until size) {
            elements[i] = inputStream.readObject() as E?
        }
        start = 0
        full = size == maxElements
        end = if (full) {
            0
        } else {
            size
        }
    }

    /** this queue's size */
    override val size: Int
        /**
         * Returns the number of elements stored in the queue.
         */
        get() {
            return if (end < start) {
                maxElements - start + end
            } else if (end == start) {
                if (full) {
                    maxElements
                } else {
                    0
                }
            } else {
                end - start
            }
        }

    /**
     * Returns true if this queue is empty; false otherwise.
     *
     * @return true if this queue is empty
     */
    override fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * A [CircularFifoQueue] can never be full, thus this returns always false.
     *
     * @return always returns false
     */
    fun isFull(): Boolean {
        return false
    }

    /**
     * Returns true if the capacity limit of this queue has been reached,
     * i.e. the number of elements stored in the queue equals its maximum size.
     *
     * @return true if the capacity limit has been reached, false otherwise
     */
    fun isAtFullCapacity(): Boolean {
        return size == maxElements
    }

    /**
     * Gets the maximum size of the collection (the bound).
     *
     * @return the maximum number of elements the collection can hold
     */
    fun maxSize(): Int {
        return maxElements
    }

    /**
     * Clears this queue.
     */
    override fun clear() {
        full = false
        start = 0
        end = 0
        Arrays.fill(elements, null)
    }

    /**
     * Adds the given element to this queue. If the queue is full, the least recently added
     * element is discarded so that a new element can be inserted.
     *
     * @param element  the element to add
     * @return true, always
     * @throws NullPointerException  if the given element is null
     */
    override fun add(element: E?): Boolean {
        Validate.checkNotNull(element, "element is null.")

        if (isAtFullCapacity()) {
            remove()
        }

        elements[end++] = element
        if (end >= maxElements) {
            end = 0
        }

        if (end == start) {
            full = true
        }
        return true
    }

    /**
     * Returns the element at the specified position in this queue.
     *
     * @param index the position of the element in the queue
     * @return the element at position index
     * @throws NoSuchElementException if the requested position is outside the range [0, size)
     */
    fun get(index: Int): E? {
        if (index < 0 || index >= size) {
            throw NoSuchElementException("The specified index $index is outside the available range [0, $size)")
        }

        val idx = (start + index) % maxElements
        return elements[idx]
    }

    /**
     * Adds the given element to this queue. If the queue is full, the least recently added
     * element is discarded so that a new element can be inserted.
     *
     * @param e the element to add
     * @return true, always
     * @throws NullPointerException  if the given element is null
     */
    override fun offer(e: E?): Boolean {
        return add(e)
    }

    override fun poll(): E? {
        return if (isEmpty()) {
            null
        } else {
            remove()
        }
    }

    override fun element(): E? {
        if (isEmpty()) {
            throw NoSuchElementException("queue is empty")
        }
        return peek()
    }

    override fun peek(): E? {
        return if (isEmpty()) {
            null
        } else {
            elements[start]
        }
    }

    override fun remove(): E? {
        if (isEmpty()) {
            throw NoSuchElementException("queue is empty")
        }

        val element = elements[start]
        if (element != null) {
            elements[start++] = null

            if (start >= maxElements) {
                start = 0
            }
            full = false
        }
        return element
    }

    /**
     * Increments the internal index.
     *
     * @param index  the index to increment
     * @return the updated index
     */
    private fun increment(index: Int): Int {
        return if (index + 1 < maxElements) {
            index + 1
        } else {
            0
        }
    }

    /**
     * Decrements the internal index.
     *
     * @param index  the index to decrement
     * @return the updated index
     */
    private fun decrement(index: Int): Int {
        return if (index - 1 >= 0) {
            index - 1
        } else {
            maxElements - 1
        }
    }

    /**
     * Returns an iterator over this queue's elements.
     *
     * @return an iterator over this queue's elements
     */
    @Suppress("kotlin:S3776")
    override fun iterator(): MutableIterator<E?> {
        return object : MutableIterator<E?> {
            private var index = start
            private var lastReturnedIndex = -1
            private var isFirst = full

            override fun hasNext(): Boolean {
                return isFirst || index != end
            }

            override fun next(): E? {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                isFirst = false
                lastReturnedIndex = index
                index = increment(index)
                return elements[lastReturnedIndex]
            }

            override fun remove() {
                if (lastReturnedIndex == -1) {
                    throw IllegalStateException()
                }

                // First element can be removed quickly
                if (lastReturnedIndex == start) {
                    this@CircularFifoQueue.remove()
                    lastReturnedIndex = -1
                    return
                }

                var pos = lastReturnedIndex + 1
                if (start < lastReturnedIndex && pos < end) {
                    // shift in one part
                    System.arraycopy(elements, pos, elements, lastReturnedIndex, end - pos)
                } else {
                    // Other elements require us to shift the subsequent elements
                    while (pos != end) {
                        if (pos >= maxElements) {
                            elements[pos - 1] = elements[0]
                            pos = 0
                        } else {
                            elements[decrement(pos)] = elements[pos]
                            pos = increment(pos)
                        }
                    }
                }

                lastReturnedIndex = -1
                end = decrement(end)
                elements[end] = null
                full = false
                index = decrement(index)
            }
        }
    }

    companion object {
        /** Serialization version  */
        private const val serialVersionUID: Long = -8423413834657610406L
    }
}
