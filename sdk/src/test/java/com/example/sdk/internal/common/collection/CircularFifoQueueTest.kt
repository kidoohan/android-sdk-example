package com.example.sdk.internal.common.collection

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Queue

/** Tests for [CircularFifoQueue]  */
class CircularFifoQueueTest<E> : AbstractQueueTest<E>() {
    /**
     *  Runs through the regular verifications, but also verifies that
     *  the buffer contains the same elements in the same sequence as the
     *  list.
     */
    override fun verify() {
        super.verify()
        val iterator1: MutableIterator<E?> = getCollection().iterator()
        for (e in getConfirmed()) {
            assertThat(iterator1.hasNext()).isTrue()
            val o1: E? = iterator1.next()
            val o2: E? = e
            assertThat(o1).isEqualTo(o2)
        }
    }

    /**
     * Overridden because CircularFifoQueue doesn't allow null elements.
     * @return false
     */
    override fun isNullSupported(): Boolean {
        return false
    }

    /**
     * Overridden because CircularFifoQueue isn't fail fast.
     * @return false
     */
    override fun isFailFastSupported(): Boolean {
        return false
    }

    /**
     * Returns an empty ArrayList.
     *
     * @return an empty ArrayList
     */
    override fun makeConfirmedCollection(): MutableCollection<E?> {
        return ArrayList()
    }

    /**
     * Returns a full ArrayList.
     *
     * @return a full ArrayList
     */
    override fun makeConfirmedFullCollection(): MutableCollection<E?> {
        val c = makeConfirmedCollection()
        c.addAll(getFullElements().asList())
        return c
    }

    /**
     * Returns an empty CircularFifoQueue that won't overflow.
     *
     * @return an empty CircularFifoQueue
     */
    override fun makeObject(): Queue<E?> {
        return CircularFifoQueue(100)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getCollection(): CircularFifoQueue<E?> {
        return super.getCollection() as CircularFifoQueue<E?>
    }

    /**
     * Tests that the removal operation actually removes the first element.
     */
    @Test
    @Suppress("UNCHECKED_CAST")
    fun testCircularFifoQueueCircular() {
        val list: MutableList<E?> = ArrayList()
        list.add("A" as E)
        list.add("B" as E)
        list.add("C" as E)
        val queue: Queue<E?> = CircularFifoQueue(list)
        assertThat(queue).containsExactly("A", "B", "C")
        queue.add("D" as E)
        assertThat(queue).containsExactly("B", "C", "D")
        assertThat(queue.peek()).isEqualTo("B")
        assertThat(queue.remove()).isEqualTo("B")
        assertThat(queue.remove()).isEqualTo("C")
        assertThat(queue.remove()).isEqualTo("D")
    }

    /**
     * Tests that the removal operation actually removes the first element.
     */
    @Test
    fun testCircularFifoQueueRemove() {
        resetFull()
        val size = getConfirmed().size
        for (i in 0 until size) {
            val o1: Any? = getCollection().remove()
            val o2: Any? = (getConfirmed() as MutableList<*>).removeAt(0)
            assertThat(o1).isEqualTo(o2)
            verify()
        }
        assertThrows(NoSuchElementException::class.java) { getCollection().remove() }
    }

    /**
     * Tests that the constructor correctly throws an exception.
     */
    @Test
    fun testConstructorException1() {
        assertThrows(IllegalArgumentException::class.java) { CircularFifoQueue<E?>(0) }
    }

    /**
     * Tests that the constructor correctly throws an exception.
     */
    @Test
    fun testConstructorException2() {
        assertThrows(IllegalArgumentException::class.java) { CircularFifoQueue<E?>(-20) }
    }

    @Test
    fun testRemove1() {
        // based on bug 33071
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5")
        assertThat(fifo.toString()).isEqualTo("[1, 2, 3, 4, 5]")
        fifo.remove("3")
        assertThat(fifo.toString()).isEqualTo("[1, 2, 4, 5]")
        fifo.remove("4")
        assertThat(fifo.toString()).isEqualTo("[1, 2, 5]")
    }

    @Test
    fun testRemove2() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5")
        fifo.add("6")
        assertThat(fifo).hasSize(5)
        assertThat(fifo.toString()).isEqualTo("[2, 3, 4, 5, 6]")
        fifo.remove("3")
        assertThat(fifo.toString()).isEqualTo("[2, 4, 5, 6]")
        fifo.remove("4")
        assertThat(fifo.toString()).isEqualTo("[2, 5, 6]")
    }

    @Test
    fun testRemove3() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5")
        assertThat(fifo.toString()).isEqualTo("[1, 2, 3, 4, 5]")
        fifo.remove("3")
        assertThat(fifo.toString()).isEqualTo("[1, 2, 4, 5]")
        fifo.add("6")
        fifo.add("7")
        assertThat(fifo.toString()).isEqualTo("[2, 4, 5, 6, 7]")
        fifo.remove("4")
        assertThat(fifo.toString()).isEqualTo("[2, 5, 6, 7]")
    }

    @Test
    fun testRemove4() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5") // end=0
        fifo.add("6") // end=1
        fifo.add("7") // end=2
        assertThat(fifo.toString()).isEqualTo("[3, 4, 5, 6, 7]")
        fifo.remove("4") // remove element in middle of array, after start
        assertThat(fifo.toString()).isEqualTo("[3, 5, 6, 7]")
    }

    @Test
    fun testRemove5() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5") // end=0
        fifo.add("6") // end=1
        fifo.add("7") // end=2
        assertThat(fifo.toString()).isEqualTo("[3, 4, 5, 6, 7]")
        fifo.remove("5") // remove element at last pos in array
        assertThat(fifo.toString()).isEqualTo("[3, 4, 6, 7]")
    }

    @Test
    fun testRemove6() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5") // end=0
        fifo.add("6") // end=1
        fifo.add("7") // end=2
        assertThat(fifo.toString()).isEqualTo("[3, 4, 5, 6, 7]")
        fifo.remove("6") // remove element at position zero in array
        assertThat(fifo.toString()).isEqualTo("[3, 4, 5, 7]")
    }

    @Test
    fun testRemove7() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5") // end=0
        fifo.add("6") // end=1
        fifo.add("7") // end=2
        assertThat(fifo.toString()).isEqualTo("[3, 4, 5, 6, 7]")
        fifo.remove("7") // remove element at position one in array
        assertThat(fifo.toString()).isEqualTo("[3, 4, 5, 6]")
    }

    @Test
    fun testRemove8() {
        val fifo = CircularFifoQueue<String>(5)
        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5") // end=0
        fifo.add("6") // end=1
        fifo.add("7") // end=2
        fifo.add("8") // end=3
        assertThat(fifo.toString()).isEqualTo("[4, 5, 6, 7, 8]")
        fifo.remove("7") // remove element at position one in array, need to shift 8
        assertThat(fifo.toString()).isEqualTo("[4, 5, 6, 8]")
    }

    @Test
    fun testRemove9() {
        val fifo = CircularFifoQueue<String>(5)
        assertThat(fifo.isFull()).isFalse()

        fifo.add("1")
        fifo.add("2")
        fifo.add("3")
        fifo.add("4")
        fifo.add("5") // end=0
        fifo.add("6") // end=1
        fifo.add("7") // end=2
        fifo.add("8") // end=3

        assertThat(fifo.isFull()).isFalse()
        assertThat(fifo.toString()).isEqualTo("[4, 5, 6, 7, 8]")
        fifo.remove("8") // remove element at position two in array
        assertThat(fifo.toString()).isEqualTo("[4, 5, 6, 7]")
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun testGetIndex() {
        resetFull()
        val queue = getCollection()
        val confirmed = getConfirmed() as List<E?>
        for (i in confirmed.indices) {
            assertThat(queue.get(i)).isEqualTo(confirmed[i])
        }

        // remove the first two elements and check again
        queue.remove()
        queue.remove()
        for (i in queue.indices) {
            assertThat(queue.get(i)).isEqualTo(confirmed[i + 2])
        }
    }

    @Test
    fun testAddNull() {
        val b = CircularFifoQueue<E>(2)
        assertThrows(NullPointerException::class.java) { b.add(null) }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun testDefaultSizeAndGetError1() {
        val fifo = CircularFifoQueue<E>()
        assertThat(fifo.maxSize()).isEqualTo(32)
        fifo.add("1" as E)
        fifo.add("2" as E)
        fifo.add("3" as E)
        fifo.add("4" as E)
        fifo.add("5" as E)
        assertThat(fifo).hasSize(5)
        assertThrows(NoSuchElementException::class.java) { fifo.get(5) }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun testDefaultSizeAndGetError2() {
        val fifo = CircularFifoQueue<E>()
        assertThat(fifo.maxSize()).isEqualTo(32)
        fifo.add("1" as E)
        fifo.add("2" as E)
        fifo.add("3" as E)
        fifo.add("4" as E)
        fifo.add("5" as E)
        assertThat(fifo).hasSize(5)
        assertThrows(NoSuchElementException::class.java) { fifo.get(-2) }
    }
}
