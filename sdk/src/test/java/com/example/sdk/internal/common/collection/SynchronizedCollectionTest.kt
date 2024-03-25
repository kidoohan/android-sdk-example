package com.example.sdk.internal.common.collection

/** Tests for [SynchronizedCollection]  */
class SynchronizedCollectionTest<E> : AbstractCollectionTest<E>() {
    override fun makeConfirmedCollection(): MutableCollection<E?> {
        return ArrayList()
    }

    override fun makeConfirmedFullCollection(): MutableCollection<E?> {
        return ArrayList(getFullElements().asList())
    }

    override fun makeObject(): MutableCollection<E?> {
        return SynchronizedCollection(mutableListOf())
    }
}
