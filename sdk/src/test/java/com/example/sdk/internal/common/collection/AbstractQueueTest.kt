package com.example.sdk.internal.common.collection

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.Serializable
import java.util.Queue

/**
 * Abstract test class for [Queue] methods and contracts.
 *
 * To use, simply extend this class, and implement
 * the [makeObject] method.
 *
 * If your [Queue] fails one of these tests by design,
 * you may still use this base set of cases.  Simply override the
 * test case (method) your [Queue] fails or override one of the
 * protected methods from AbstractCollectionTest.
 */
abstract class AbstractQueueTest<E> : AbstractCollectionTest<E>() {
    /**
     *  Returns true if the collections produced by
     *  [makeObject] and [makeFullCollection()]
     *  support the <code>set operation.
     *  Default implementation returns true.  Override if your collection
     *  class does not support set.
     */
    fun isSetSupported(): Boolean {
        return true
    }

    /** Returns an empty [ArrayList]. */
    override fun makeConfirmedCollection(): MutableCollection<E?>? {
        return ArrayList()
    }

    /**
     * Returns a full [ArrayList].
     */
    override fun makeConfirmedFullCollection(): MutableCollection<E?>? {
        return ArrayList(getFullElements().asList())
    }

    /**
     * Returns [makeObject]
     *
     * @return an empty queue to be used for testing
     */
    abstract override fun makeObject(): Queue<E?>

    override fun makeFullCollection(): Queue<E?> {
        val queue = makeObject()
        queue.addAll(getFullElements().asList())
        return queue
    }

    /**
     * Returns the [collection] field cast to a [Queue].
     *
     * @return the collection field as a Queue
     */
    override fun getCollection(): Queue<E?> {
        return super.getCollection() as Queue<E?>
    }

    /**
     *  Verifies that the test queue implementation matches the confirmed queue
     *  implementation.
     */
    override fun verify() {
        super.verify()
        val iterator1: MutableIterator<E?> = getCollection().iterator()
        for (e in getConfirmed()) {
            assertThat(iterator1.hasNext()).isTrue()
            val o1: Any? = iterator1.next()
            val o2: Any? = e
            assertThat(o1).isEqualTo(o2)
        }
    }

    /**
     * Tests [Queue.offer].
     */
    @Test
    open fun testQueueOffer() {
        if (!isAddSupported()) {
            return
        }
        val elements = getFullElements()
        for (element in elements) {
            resetEmpty()
            val r = getCollection().offer(element)
            getConfirmed().add(element)
            verify()
            assertThat(r).isTrue()
            assertThat(getCollection()).hasSize(1)
        }
        resetEmpty()
        var size = 0
        for (element in elements) {
            val r = getCollection().offer(element)
            getConfirmed().add(element)
            verify()
            if (r) {
                size++
            }
            assertThat(getCollection()).hasSize(size)
            assertThat(getCollection()).contains(element)
        }
    }

    /** Tests [Queue.element]. */
    @Test
    open fun testQueueElement() {
        resetEmpty()
        assertThrows(NoSuchElementException::class.java) { getCollection().element() }
        resetFull()
        assertThat(getConfirmed()).contains(getCollection().element())
        if (!isRemoveSupported()) {
            return
        }
        val max: Int = getFullElements().size
        for (i in 0 until max) {
            val element: E? = getCollection().element()
            if (!isNullSupported()) {
                assertThat(element).isNotNull()
            }
            assertThat(getConfirmed()).contains(element)
            getCollection().remove(element)
            getConfirmed().remove(element)
            verify()
        }
        assertThrows(NoSuchElementException::class.java) { getCollection().element() }
    }

    /**
     * Tests [Queue.peek].
     */
    @Test
    open fun testQueuePeek() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        var element: E? = getCollection().peek()
        assertThat(element).isNull()
        resetFull()
        val max: Int = getFullElements().size
        for (i in 0 until max) {
            element = getCollection().peek()
            if (!isNullSupported()) {
                assertThat(element).isNotNull()
            }
            assertThat(getConfirmed()).contains(element)
            getCollection().remove(element)
            getConfirmed().remove(element)
            verify()
        }
        element = getCollection().peek()
        assertThat(element).isNull()
    }

    /**
     * Tests [Queue.remove].
     */
    @Test
    open fun testQueueRemove() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        assertThrows(NoSuchElementException::class.java) { getCollection().remove() }
        resetFull()
        val max: Int = getFullElements().size
        for (i in 0 until max) {
            val element: E? = getCollection().remove()
            val success = getConfirmed().remove(element)
            assertThat(success).isTrue()
            verify()
        }
        assertThrows(NoSuchElementException::class.java) { getCollection().element() }
    }

    /**
     * Tests [Queue.poll].
     */
    @Test
    open fun testQueuePoll() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        var element: E? = getCollection().poll()
        assertThat(element).isNull()
        resetFull()
        val max: Int = getFullElements().size
        for (i in 0 until max) {
            element = getCollection().poll()
            assertThat(getConfirmed().remove(element)).isTrue()
            verify()
        }
        element = getCollection().poll()
        assertThat(element).isNull()
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    open fun testEmptyQueueSerialization() {
        val queue: Queue<E?> = makeObject()

        if (queue is Serializable) {
            val obj: ByteArray = writeExternalFormToBytes(queue as Serializable)
            val queue2 = readExternalFormFormBytes(obj) as Queue<E?>

            assertThat(queue).hasSize(0)
            assertThat(queue2).hasSize(0)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    open fun testFullQueueSerialization() {
        val queue: Queue<E?> = makeFullCollection()
        val size = getFullElements().size

        if (queue is Serializable) {
            val obj = writeExternalFormToBytes(queue as Serializable)
            val queue2 = readExternalFormFormBytes(obj) as Queue<E?>

            assertThat(queue).hasSize(size)
            assertThat(queue2).hasSize(size)
        }
    }
}
