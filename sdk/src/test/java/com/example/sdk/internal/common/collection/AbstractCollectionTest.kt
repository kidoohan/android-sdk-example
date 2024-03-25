package com.example.sdk.internal.common.collection

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serializable
import java.util.Collections
import java.util.Objects

/** Abstract test class for [Collection] methods and contracts. */
abstract class AbstractCollectionTest<E> {
    //
    // NOTE:
    //
    // Collection doesn't define any semantics for equals, and recommends you
    // use reference-based default behavior of Object.equals.  (And a test for
    // that already exists in AbstractTestObject).  Tests for equality of lists, sets
    // and bags will have to be written in test subclasses.  Thus, there is no
    // tests on Collection.equals nor any for Collection.hashCode.
    //

    //
    // NOTE:
    //
    // Collection doesn't define any semantics for equals, and recommends you
    // use reference-based default behavior of Object.equals.  (And a test for
    // that already exists in AbstractTestObject).  Tests for equality of lists, sets
    // and bags will have to be written in test subclasses.  Thus, there is no
    // tests on Collection.equals nor any for Collection.hashCode.
    //

    // These fields are used by reset() and verify(), and any test
    // method that tests a modification.

    // These fields are used by reset() and verify(), and any test
    // method that tests a modification.
    /**
     * A collection instance that will be used for testing.
     */
    private var collection: MutableCollection<E?>? = null

    /**
     * Confirmed collection.  This is an instance of a collection that is
     * confirmed to conform exactly to the java.util.Collection contract.
     * Modification operations are tested by performing a mod on your
     * collection, performing the exact same mod on an equivalent confirmed
     * collection, and then calling verify() to make sure your collection
     * still matches the confirmed collection.
     */
    private var confirmed: MutableCollection<E?>? = null

    /**
     * Specifies whether equal elements in the collection are, in fact,
     * distinguishable with information not readily available.  That is, if a
     * particular value is to be removed from the collection, then there is
     * one and only one value that can be removed, even if there are other
     * elements which are equal to it.
     *
     * In most collection cases, elements are not distinguishable (equal is
     * equal), thus this method defaults to return false.  In some cases,
     * however, they are.  For example, the collection returned from the map's
     * values() collection view are backed by the map, so while there may be
     * two values that are equal, their associated keys are not.  Since the
     * keys are distinguishable, the values are.
     *
     * This flag is used to skip some verifications for iterator.remove()
     * where it is impossible to perform an equivalent modification on the
     * confirmed collection because it is not possible to determine which
     * value in the confirmed collection to actually remove.  Tests that
     * override the default (i.e. where equal elements are distinguishable),
     * should provide additional tests on iterator.remove() to make sure the
     * proper elements are removed when remove() is called on the iterator.
     */
    open fun areEqualElementsDistinguishable(): Boolean {
        return false
    }

    /**
     * Returns true if the collections produced by
     * [makeObject] and [makeFullCollection]
     * support the `add` and `addAll`
     * operations.
     *
     * Default implementation returns true.  Override if your collection
     * class does not support add or addAll.
     */
    open fun isAddSupported(): Boolean {
        return true
    }

    /**
     * Returns true if the collections produced by
     * [makeObject] and [makeFullCollection]
     * support the `remove`, `removeAll`,
     * `retainAll`, `clear` and
     * `iterator().remove()` methods.
     * Default implementation returns true.  Override if your collection
     * class does not support removal operations.
     */
    open fun isRemoveSupported(): Boolean {
        return true
    }

    /**
     * Returns true to indicate that the collection supports holding null.
     * The default implementation returns true;
     */
    open fun isNullSupported(): Boolean {
        return true
    }

    /**
     * Returns true to indicate that the collection supports fail fast iterators.
     * The default implementation returns true;
     */
    open fun isFailFastSupported(): Boolean {
        return false
    }

    /**
     * Returns true to indicate that the collection supports [equals] comparisons.
     * This implementation returns false;
     */
    open fun isEqualsCheckable(): Boolean {
        return false
    }

    /**
     * Verifies that [collection] and [confirmed] have
     * identical state.
     */
    open fun verify() {
        val confirmedSize = getConfirmed().size
        assertThat(getCollection().size).isEqualTo(confirmedSize)
        assertThat(getCollection().isEmpty()).isEqualTo(getConfirmed().isEmpty())

        // verify the collections are the same by attempting to match each
        // object in the collection and confirmed collection.  To account for
        // duplicates and differing orders, each confirmed element is copied
        // into an array and a flag is maintained for each element to determine
        // whether it has been matched once and only once.  If all elements in
        // the confirmed collection are matched once and only once and there
        // aren't any elements left to be matched in the collection,
        // verification is a success.

        // copy each collection value into an array
        val confirmedValues = arrayOfNulls<Any>(confirmedSize)
        var iterator: Iterator<E?>
        iterator = getConfirmed().iterator()
        var pos = 0
        while (iterator.hasNext()) {
            confirmedValues[pos++] = iterator.next()
        }

        // allocate an array of boolean flags for tracking values that have
        // been matched once and only once.
        val matched = BooleanArray(confirmedSize)

        // now iterate through the values of the collection and try to match
        // the value with one in the confirmed array.
        iterator = getCollection().iterator()
        while (iterator.hasNext()) {
            val o: Any? = iterator.next()
            var match = false
            for (i in 0 until confirmedSize) {
                if (matched[i]) {
                    // skip values already matched
                    continue
                }
                if (Objects.equals(o, confirmedValues[i])) {
                    // values matched
                    matched[i] = true
                    match = true
                    break
                }
            }
            // no match found!
            if (!match) {
                fail(
                    "Collection should not contain a value that the " +
                        "confirmed collection does not have: " + o + "\nTest: " + getCollection() +
                        "\nReal: " + getConfirmed(),
                )
            }
        }

        // make sure there aren't any unmatched values
        for (i in 0 until confirmedSize) {
            if (!matched[i]) {
                // the collection didn't match all the confirmed values
                fail(
                    (
                        "Collection should contain all values that are in the confirmed collection" +
                            "\nTest: " + getCollection() + "\nReal: " + getConfirmed()
                        ),
                )
            }
        }
    }

    /**
     * Resets the [collection] and [confirmed] fields to empty
     * collections.  Invoke this method before performing a modification
     * test.
     */
    open fun resetEmpty() {
        setCollection(makeObject())
        setConfirmed(makeConfirmedCollection())
    }

    /**
     * Resets the [collection] and [confirmed] fields to full
     * collections.  Invoke this method before performing a modification
     * test.
     */
    open fun resetFull() {
        setCollection(makeFullCollection())
        setConfirmed(makeConfirmedFullCollection())
    }

    /**
     * Returns a confirmed empty collection.
     * For instance, an [java.util.ArrayList] for lists or a
     * [java.util.HashSet] for sets.
     *
     * @return a confirmed empty collection
     */
    abstract fun makeConfirmedCollection(): MutableCollection<E?>?

    /**
     * Returns a confirmed full collection.
     * For instance, an [java.util.ArrayList] for lists or a
     * [java.util.HashSet] for sets.  The returned collection
     * should contain the elements returned by [getFullElements].
     *
     * @return a confirmed full collection
     */
    abstract fun makeConfirmedFullCollection(): MutableCollection<E?>?

    /**
     * Return a new, empty [Collection] to be used for testing.
     */
    abstract fun makeObject(): MutableCollection<E?>

    /**
     * Returns a full collection to be used for testing.  The collection
     * returned by this method should contain every element returned by
     * [getFullElements].  The default implementation, in fact,
     * simply invokes `addAll` on an empty collection with
     * the results of [getFullElements].  Override this default
     * if your collection doesn't support addAll.
     */
    open fun makeFullCollection(): MutableCollection<E?>? {
        val c = makeObject()
        c.addAll(getFullElements().asList())
        return c
    }

    /**
     * Creates a new Map Entry that is independent of the first and the map.
     */
    open fun cloneMapEntry(entry: Map.Entry<E, E>): Map.Entry<E, E> {
        val map: HashMap<E, E> = HashMap()
        map[entry.key] = entry.value
        return map.entries.iterator().next()
    }

    /**
     * Returns an array of objects that are contained in a collection
     * produced by [makeFullCollection].  Every element in the
     * returned array `must` be an element in a full collection.
     * The default implementation returns a heterogeneous array of
     * objects with some duplicates. null is added if allowed.
     * Override if you require specific testing elements.  Note that if you
     * override [makeFullCollection], you `must` override
     * this method to reflect the contents of a full collection.
     */
    @Suppress("UNCHECKED_CAST")
    open fun getFullElements(): Array<E?> {
        if (isNullSupported()) {
            val list: ArrayList<E?> = ArrayList(getFullNonNullElements().asList())
            list.add(4, null)
            return list.toArray() as Array<E?>
        }
        return getFullNonNullElements().clone()
    }

    /**
     * Returns an array of elements that are `not` contained in a
     * full collection.  Every element in the returned array must
     * not exist in a collection returned by [makeFullCollection].
     * The default implementation returns a heterogeneous array of elements
     * without null.  Note that some of the tests add these elements
     * to an empty or full collection, so if your collection restricts
     * certain kinds of elements, you should override this method.
     */
    open fun getOtherElements(): Array<E?> {
        return getOtherNonNullElements()
    }

    /**
     * Returns a list of elements suitable for return by
     * [getFullElements].  The array returned by this method
     * does not include null, but does include a variety of objects
     * of different types.  Override getFullElements to return
     * the results of this method if your collection does not support
     * the null element.
     */
    @Suppress("UNCHECKED_CAST")
    open fun getFullNonNullElements(): Array<E?> {
        return arrayOf<Any>(
            "",
            "One",
            Integer.valueOf(2),
            "Three",
            Integer.valueOf(4),
            "One",
            java.lang.Double.valueOf(5.0),
            java.lang.Float.valueOf(6f),
            "Seven",
            "Eight",
            "Nine",
            Integer.valueOf(10), 11.toShort(),
            java.lang.Long.valueOf(12),
            "Thirteen",
            "14",
            "15",
            java.lang.Byte.valueOf(16.toByte()),
        ) as Array<E?>
    }

    /**
     * Returns the default list of objects returned by
     * [getOtherElements].  Includes many objects of different types.
     */
    @Suppress("UNCHECKED_CAST")
    open fun getOtherNonNullElements(): Array<E?> {
        return arrayOf<Any>(
            Integer.valueOf(0),
            java.lang.Float.valueOf(0f),
            java.lang.Double.valueOf(0.0),
            "Zero", 0.toShort(),
            java.lang.Byte.valueOf(0.toByte()),
            java.lang.Long.valueOf(0),
            Character.valueOf('\u0000'),
            "0",
        ) as Array<E?>
    }

    /**
     * Returns a list of string elements suitable for return by
     * [getFullElements].  Override getFullElements to return
     * the results of this method if your collection does not support
     * heterogeneous elements or the null element.
     */
    open fun getFullNonNullStringElements(): Array<Any>? {
        return arrayOf(
            "If", "the", "dull", "substance", "of", "my", "flesh", "were",
            "thought", "Injurious", "distance", "could", "not", "stop", "my", "way",
        )
    }

    /**
     * Returns a list of string elements suitable for return by
     * [getOtherElements].  Override getOtherElements to return
     * the results of this method if your collection does not support
     * heterogeneous elements or the null element.
     */
    open fun getOtherNonNullStringElements(): Array<Any>? {
        return arrayOf(
            "For", "then", "despite", /* of */"space", "I", "would", "be",
            "brought", "From", "limits", "far", "remote", "where", "thou", "dost", "stay",
        )
    }

    /**
     * Return a flag specifying the iteration behavior of the collection.
     * This is used to change the assertions used by specific tests.
     * The default implementation returns 0 which indicates ordered iteration behavior.
     *
     * @return the iteration behavior
     * @see .UNORDERED
     */
    protected open fun getIterationBehaviour(): Int {
        return 0
    }

    // Tests
    @Test
    open fun testObjectEqualsSelf() {
        val obj = makeObject()
        assertThat(obj).isEqualTo(obj)
    }

    @Test
    open fun testEqualsNull() {
        val obj = makeObject()
        assertThat(obj == null).isFalse()
    }

    @Test
    open fun testObjectHashCodeEqualsSelfHashCode() {
        val obj = makeObject()
        assertThat(obj.hashCode()).isEqualTo(obj.hashCode())
    }

    @Test
    open fun testObjectHashCodeEqualsContract() {
        val obj1: Any = makeObject()
        val obj2: Any = makeObject()
        if (obj1 == obj2) {
            assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode())
            assertThat(obj1).isEqualTo(obj2)
        }
    }

    /**
     * Tests [MutableCollection.add].
     */
    @Test
    open fun testCollectionAdd() {
        if (!isAddSupported()) {
            return
        }
        val elements = getFullElements()
        for (element: E? in elements) {
            resetEmpty()
            val r = getCollection().add(element)
            getConfirmed().add(element)
            verify()
            assertThat(r).isTrue()
            assertThat(getCollection()).hasSize(1)
        }
        resetEmpty()
        var size = 0
        for (element: E? in elements) {
            val r = getCollection().add(element)
            getConfirmed().add(element)
            verify()
            if (r) {
                size++
            }
            assertThat(getCollection()).hasSize(size)
            assertThat(getCollection()).contains(element)
        }
    }

    /** Tests [MutableCollection.addAll]. */
    @Test
    open fun testCollectionAddAll() {
        if (!isAddSupported()) {
            return
        }
        resetEmpty()
        var elements = getFullElements()

        var r = getCollection().addAll(elements)
        getConfirmed().addAll(elements)
        verify()
        assertThat(r).isTrue()
        for (element in elements) {
            assertThat(getCollection()).contains(element)
        }
        resetFull()
        var size = getCollection().size
        elements = getOtherElements()
        r = getCollection().addAll(elements)
        getConfirmed().addAll(elements)
        verify()
        assertThat(r).isTrue()
        for (element in elements) {
            assertThat(getCollection()).contains(element)
        }
        assertThat(getCollection()).hasSize(size + elements.size)
        resetFull()
        size = getCollection().size
        r = getCollection().addAll(getFullElements())
        getConfirmed().addAll(getFullElements())
        verify()
        if (r) {
            assertThat(size < getCollection().size).isTrue()
        } else {
            assertThat(getCollection()).hasSize(size)
        }
    }

    /**
     * If [.isAddSupported] returns false, tests that add operations
     * raise `UnsupportedOperationException.
     ` */
    @Test
    open fun testUnsupportedAdd() {
        if (isAddSupported()) {
            return
        }
        resetEmpty()
        assertThrows(UnsupportedOperationException::class.java) {
            getCollection().add(
                getFullNonNullElements()[0],
            )
        }
        // make sure things didn't change even if the expected exception was
        // thrown.
        verify()
        assertThrows(UnsupportedOperationException::class.java) {
            getCollection().addAll(getFullElements().asList())
        }

        // make sure things didn't change even if the expected exception was
        // thrown.
        verify()
        resetFull()
        assertThrows(UnsupportedOperationException::class.java) {
            getCollection().add(
                getFullNonNullElements()[0],
            )
        }
        // make sure things didn't change even if the expected exception was
        // thrown.
        verify()
        assertThrows(UnsupportedOperationException::class.java) {
            getCollection().addAll(
                getOtherElements().asList(),
            )
        }
        // make sure things didn't change even if the expected exception was
        // thrown.
        verify()
    }

    /**
     * Test [MutableCollection.clear].
     */
    @Test
    open fun testCollectionClear() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        getCollection().clear() // just to make sure it doesn't raise anything
        verify()
        resetFull()
        getCollection().clear()
        getConfirmed().clear()
        verify()
    }

    /**
     * Tests [Collection.contains].
     */
    @Test
    open fun testCollectionContains() {
        resetEmpty()
        var elements: Array<E?> = getFullElements()
        for (i in elements.indices) {
            assertThat(
                getCollection().contains(
                    elements[i],
                ),
            ).isFalse()
        }
        // make sure calls to "contains" don't change anything
        verify()
        elements = getOtherElements()
        for (i in elements.indices) {
            assertThat(
                getCollection().contains(
                    elements[i],
                ),
            ).isFalse()
        }
        // make sure calls to "contains" don't change anything
        verify()
        resetFull()
        elements = getFullElements()
        for (i in elements.indices) {
            assertThat(getCollection().contains(elements[i])).isTrue()
        }
        // make sure calls to "contains" don't change anything
        verify()
        resetFull()
        elements = getOtherElements()
        for (element: Any? in elements) {
            assertThat(getCollection().contains(element)).isFalse()
        }
    }

    /**
     * Tests [Collection.containsAll].
     */
    @Test
    open fun testCollectionContainsAll() {
        resetEmpty()
        var col: MutableCollection<E?> = HashSet()
        assertThat(getCollection().containsAll(col)).isTrue()
        col.addAll(getOtherElements().asList())
        assertThat(getCollection().containsAll(col)).isFalse()
        // make sure calls to "containsAll" don't change anything
        verify()
        resetFull()
        assertThat(getCollection().containsAll(col)).isFalse()
        col.clear()
        col.addAll(getFullElements().asList())
        assertThat(getCollection().containsAll(col)).isTrue()
        // make sure calls to "containsAll" don't change anything
        verify()
        val min = if (getFullElements().size < 4) 0 else 2
        val max =
            if (getFullElements().size == 1) 1 else if (getFullElements().size <= 5) getFullElements().size - 1 else 5
        col = getFullElements().asList().toMutableList().subList(min, max)
        assertThat(getCollection().containsAll(col)).isTrue()
        assertThat(getCollection().containsAll((getCollection()))).isTrue()
        // make sure calls to "containsAll" don't change anything
        verify()
        col = ArrayList(getFullElements().asList())
        col.addAll(getFullElements().asList())
        assertThat(getCollection().containsAll(col)).isTrue()

        // make sure calls to "containsAll" don't change anything
        verify()
    }

    /**
     * Tests [Collection.isEmpty].
     */
    @Test
    open fun testCollectionIsEmpty() {
        resetEmpty()
        assertThat(getCollection()).isEmpty()
        // make sure calls to "isEmpty() don't change anything
        verify()
        resetFull()
        assertThat(getCollection()).isNotEmpty()
        // make sure calls to "isEmpty() don't change anything
        verify()
    }

    /**
     * Tests the read-only functionality of [Collection.iterator].
     */
    @Test
    open fun testCollectionIterator() {
        resetEmpty()
        var it1: Iterator<E?> = getCollection().iterator()
        assertThat(it1.hasNext()).isFalse()
        val finalIt1 = it1
        assertThrows(NoSuchElementException::class.java) { finalIt1.next() }
        // make sure nothing has changed after non-modification
        verify()
        resetFull()
        it1 = getCollection().iterator()
        for (element: E? in getCollection()) {
            assertThat(it1.hasNext()).isTrue()
            it1.next()
        }
        assertThat(it1.hasNext()).isFalse()
        val list: ArrayList<E?> = ArrayList()
        it1 = getCollection().iterator()
        for (i in getCollection().indices) {
            val next = it1.next()
            assertThat(getCollection().contains(next)).isTrue()
            list.add(next)
        }
        val finalIt2 = it1
        assertThrows(NoSuchElementException::class.java) { finalIt2.next() }

        // make sure nothing has changed after non-modification
        verify()
    }

    /**
     * Tests removals from [Collection.iterator].
     */
    @Test
    @Suppress("UNCHECKED_CAST")
    open fun testCollectionIteratorRemove() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        assertThrows(IllegalStateException::class.java) { getCollection().iterator().remove() }

        verify()
        val iterator0 = getCollection().iterator()
        iterator0.hasNext()
        assertThrows(IllegalStateException::class.java) { iterator0.remove() }
        verify()
        resetFull()
        var size = getCollection().size
        var iterator = getCollection().iterator()
        while (iterator.hasNext()) {
            var o: Any? = iterator.next()
            // TreeMap reuses the Map Entry, so the verify below fails
            // Clone it here if necessary
            if (o is Map.Entry<*, *>) {
                o = cloneMapEntry(o as Map.Entry<E, E>)
            }
            iterator.remove()

            // if the elements aren't distinguishable, we can just remove a
            // matching element from the confirmed collection and verify
            // contents are still the same.  Otherwise, we don't have the
            // ability to distinguish the elements and determine which to
            // remove from the confirmed collection (in which case, we don't
            // verify because we don't know how).
            //
            // see areEqualElementsDistinguishable()
            if (!areEqualElementsDistinguishable()) {
                getConfirmed().remove(o)
                verify()
            }
            size--
            assertThat(getCollection()).hasSize(size)
        }
        assertThat(getCollection()).isEmpty()
        resetFull()
        iterator = getCollection().iterator()
        iterator.next()
        iterator.remove()
        val finalIterator = iterator
        assertThrows(IllegalStateException::class.java) { finalIterator.remove() }
    }

    /**
     * Tests [MutableCollection.remove].
     */
    @Test
    open fun testCollectionRemove() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        val elements = getFullElements()
        for (element in elements) {
            assertThat(getCollection().remove(element)).isFalse()
            verify()
        }
        val other = getOtherElements()
        resetFull()
        for (element in other) {
            assertThat(getCollection().remove(element)).isFalse()
            verify()
        }
        val size = getCollection().size
        for (element in elements) {
            resetFull()
            assertThat(getCollection().remove(element)).isTrue()

            // if the elements aren't distinguishable, we can just remove a
            // matching element from the confirmed collection and verify
            // contents are still the same.  Otherwise, we don't have the
            // ability to distinguish the elements and determine which to
            // remove from the confirmed collection (in which case, we don't
            // verify because we don't know how).
            //
            // see areEqualElementsDistinguishable()
            if (!areEqualElementsDistinguishable()) {
                getConfirmed().remove(element)
                verify()
            }
            assertThat(getCollection()).hasSize(size - 1)
        }
    }

    /**
     * Tests [MutableCollection.removeAll].
     */
    @Test
    open fun testCollectionRemoveAll() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        assertThat(getCollection().removeAll(emptySet())).isFalse()
        verify()
        assertThat(getCollection().removeAll(ArrayList(getCollection()).toSet())).isFalse()
        verify()
        resetFull()
        assertThat(getCollection().removeAll(emptySet())).isFalse()
        verify()
        assertThat(getCollection().removeAll(getOtherElements().asList().toSet())).isFalse()
        verify()
        assertThat(getCollection().removeAll(HashSet(getCollection()))).isTrue()
        getConfirmed().removeAll(HashSet(getConfirmed()))
        verify()
        resetFull()
        val size = getCollection().size
        val min = if (getFullElements().size < 4) 0 else 2
        val max =
            if (getFullElements().size == 1) 1 else if (getFullElements().size <= 5) getFullElements().size - 1 else 5
        val all: Collection<E?> = getFullElements().asList().subList(min, max)
        assertThat(getCollection().removeAll(all.toSet())).isTrue()
        getConfirmed().removeAll(all)
        verify()
        assertThat(getCollection().size < size).isTrue()
        for (element: E? in all) {
            assertThat(getCollection().contains(element)).isFalse()
        }
    }

    /**
     * Tests [MutableCollection.retainAll].
     */
    @Test
    open fun testCollectionRetainAll() {
        if (!isRemoveSupported()) {
            return
        }
        resetEmpty()
        val elements: List<E?> = getFullElements().asList()
        val other: List<E?> = getOtherElements().asList()
        assertThat(getCollection().retainAll(Collections.EMPTY_SET)).isFalse()
        verify()
        assertThat(getCollection().retainAll(elements)).isFalse()
        verify()
        resetFull()
        assertThat(getCollection().retainAll(Collections.EMPTY_SET)).isTrue()
        getConfirmed().retainAll(Collections.EMPTY_SET)
        verify()
        resetFull()
        assertThat(getCollection().retainAll(other)).isTrue()
        getConfirmed().retainAll(other)
        verify()
        resetFull()
        var size = getCollection().size
        assertThat(getCollection().retainAll(elements)).isFalse()
        verify()
        assertThat(getCollection()).hasSize(size)
        if (getFullElements().size > 1) {
            resetFull()
            size = getCollection().size
            val min = if (getFullElements().size < 4) 0 else 2
            val max = if (getFullElements().size <= 5) getFullElements().size - 1 else 5
            assertThat(getCollection().retainAll(elements.subList(min, max).toSet())).isTrue()
            getConfirmed().retainAll(elements.subList(min, max).toSet())
            verify()
            for (element: E? in getCollection()) {
                assertThat(elements.subList(min, max).contains(element)).isTrue()
            }
        }
        resetFull()
        val set = HashSet(elements)
        size = getCollection().size
        assertThat(getCollection().retainAll(set)).isFalse()
        verify()
        assertThat(getCollection()).hasSize(size)
    }

    /**
     * Tests [Collection.size].
     */
    @Test
    open fun testCollectionSize() {
        resetEmpty()
        assertThat(getCollection()).hasSize(0)
        resetFull()
        assertThat(getCollection()).isNotEmpty()
    }

    /**
     * Assert the arrays contain the same elements, ignoring the order.
     *
     *
     * Note this does not test the arrays are deeply equal. Array elements are compared
     * using [Object.equals].
     *
     * @param a1 First array
     * @param a2 Second array
     * @param msg Failure message prefix
     */
    private fun assertUnorderedArrayEquals(a1: Array<Any>, a2: Array<Any?>, msg: String) {
        assertThat(a1.size).isEqualTo(a2.size)
        val size = a1.size
        // Track values that have been matched once (and only once)
        val matched = BooleanArray(size)
        NEXT_OBJECT@ for (o: Any in a1) {
            for (i in 0 until size) {
                if (matched[i]) {
                    // skip values already matched
                    continue
                }
                if (Objects.equals(o, a2[i])) {
                    // values matched
                    matched[i] = true
                    // continue to the outer loop
                    continue@NEXT_OBJECT
                }
            }
            fail("$msg: array 2 does not have object: $o")
        }
    }

    /**
     * Tests `toString` on a collection.
     */
    @Test
    open fun testCollectionToString() {
        resetEmpty()
        assertThat(getCollection().toString()).isNotNull()
        resetFull()
        assertThat(getCollection().toString()).isNotNull()
    }

    /**
     * If isRemoveSupported() returns false, tests to see that remove
     * operations raise an UnsupportedOperationException.
     */
    @Test
    open fun testUnsupportedRemove() {
        if (isRemoveSupported()) {
            return
        }
        resetEmpty()
        assertThrows(UnsupportedOperationException::class.java) { getCollection().clear() }
        verify()
        assertThrows(UnsupportedOperationException::class.java) { getCollection().remove(null) }
        verify()
        assertThrows(UnsupportedOperationException::class.java) { getCollection().removeIf { true } }
        verify()
        assertThrows(UnsupportedOperationException::class.java) {
            getCollection().removeAll(
                emptyList(),
            )
        }
        verify()
        assertThrows(UnsupportedOperationException::class.java) {
            getCollection().retainAll(
                emptyList(),
            )
        }
        verify()
        resetFull()
        val iterator = getCollection().iterator()
        iterator.next()
        assertThrows(UnsupportedOperationException::class.java) { iterator.remove() }

        verify()
    }

    /**
     * Tests that the collection's iterator is fail-fast.
     */
    @Test
    open fun testCollectionIteratorFailFast() {
        if (!isFailFastSupported()) {
            return
        }
        if (isAddSupported()) {
            resetFull()
            val iterator0: Iterator<E?> = getCollection().iterator()
            val o = getOtherElements()[0]
            getCollection().add(o)
            getConfirmed().add(o)
            assertThrows(ConcurrentModificationException::class.java) {
                iterator0.next()
            }
            verify()
            resetFull()
            val iterator: Iterator<E?> = getCollection().iterator()
            getCollection().addAll(getOtherElements().asList())
            getConfirmed().addAll(getOtherElements().asList())
            assertThrows(ConcurrentModificationException::class.java) {
                iterator.next()
            }
            verify()
        }
        if (!isRemoveSupported()) {
            return
        }
        resetFull()
        try {
            val iterator: Iterator<E?> = getCollection().iterator()
            getCollection().clear()
            iterator.next()
            fail("next after clear should raise ConcurrentModification")
        } catch (e: ConcurrentModificationException) {
            // ConcurrentModificationException: expected
            // NoSuchElementException: (also legal given spec)
        } catch (_: NoSuchElementException) {
        }
        resetFull()
        val iterator0: Iterator<E?> = getCollection().iterator()
        getCollection().remove(getFullElements()[0])
        assertThrows(ConcurrentModificationException::class.java) {
            iterator0.next()
        }
        resetFull()
        val iterator1: Iterator<E?> = getCollection().iterator()
        getCollection().removeIf { false }
        assertThrows(ConcurrentModificationException::class.java) {
            iterator1.next()
        }
        resetFull()
        val iterator2: Iterator<E?> = getCollection().iterator()
        val sublist: List<E?> = getFullElements().asList().subList(2, 5)
        getCollection().removeAll(sublist.toSet())
        assertThrows(ConcurrentModificationException::class.java) {
            iterator2.next()
        }
        resetFull()
        val iterator3: Iterator<E?> = getCollection().iterator()
        val sublist3: List<E?> = getFullElements().asList().subList(2, 5)
        getCollection().retainAll(sublist3.toSet())
        assertThrows(ConcurrentModificationException::class.java) {
            iterator3.next()
        }
    }

    open fun getCollection(): MutableCollection<E?> {
        return collection!!
    }

    /**
     * Set the collection.
     * @param collection the Collection<E> to set
     </E> */
    open fun setCollection(collection: MutableCollection<E?>?) {
        this.collection = collection
    }

    open fun getConfirmed(): MutableCollection<E?> {
        return confirmed!!
    }

    /**
     * Set the confirmed.
     * @param confirmed the Collection<E> to set
     </E> */
    open fun setConfirmed(confirmed: MutableCollection<E?>?) {
        this.confirmed = confirmed
    }

    /**
     * Handle the optional exceptions declared by [Collection.contains]
     * @param coll
     * @param element
     */
    protected open fun assertNotCollectionContains(coll: Collection<*>, element: Any?) {
        try {
            assertThat(coll.contains(element)).isFalse()
        } catch (e: ClassCastException) {
            // apparently not
        } catch (_: NullPointerException) {
        }
    }

    /**
     * Handle the optional exceptions declared by [Collection.containsAll]
     * @param coll
     * @param sub
     */
    protected open fun assertNotCollectionContainsAll(coll: Collection<*>, sub: Collection<*>) {
        try {
            assertThat(coll.containsAll(sub)).isFalse()
        } catch (e: ClassCastException) {
            // apparently not
        } catch (_: NullPointerException) {
        }
    }

    /**
     * Handle optional exceptions of [MutableCollection.remove]
     * @param coll
     * @param element
     */
    protected open fun assertNotRemoveFromCollection(coll: MutableCollection<*>, element: Any) {
        try {
            assertThat(coll.remove(element)).isFalse()
        } catch (e: ClassCastException) {
            // apparently not
        } catch (_: NullPointerException) {
        }
    }

    /**
     * Handle optional exceptions of [MutableCollection.removeAll]
     * @param coll
     * @param sub
     */
    protected open fun assertNotRemoveAllFromCollection(
        coll: MutableCollection<*>,
        sub: Collection<*>,
    ) {
        try {
            assertThat(coll.removeAll(sub.toSet())).isFalse()
        } catch (e: ClassCastException) {
            // apparently not
        } catch (_: NullPointerException) {
        }
    }

    /**
     * Converts a Serializable or Externalizable object to [ByteArray].
     * Useful for in-memory tests of serialization.
     *
     * @param o the object to convert to bytes.
     * @return the serialized form of the Object.
     */
    protected open fun writeExternalFormToBytes(o: Serializable): ByteArray {
        val stream = ByteArrayOutputStream()
        writeExternalFormToStream(o, stream)
        return stream.toByteArray()
    }

    /**
     * Read a Serialized or Externalized Object from bytes.
     * Useful for verifying serialization in memory.
     *
     * @param b the [ByteArray] containing a serialized object.
     * @return the object contained in the bytes.
     */
    protected open fun readExternalFormFormBytes(b: ByteArray): Any {
        val stream = ByteArrayInputStream(b)
        return readExternalFormFormStream(stream)
    }

    private fun readExternalFormFormStream(stream: InputStream): Any {
        return ObjectInputStream(stream).readObject()
    }

    private fun writeExternalFormToStream(o: Serializable, stream: OutputStream) {
        ObjectOutputStream(stream).writeObject(o)
    }
}
