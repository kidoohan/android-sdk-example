package com.example.sdk.internal.common.collection

import java.util.LinkedList
import java.util.Queue

/** Tests for [SynchronizedQueue]  */
class SynchronizedQueueTest<E> : AbstractQueueTest<E>() {
    override fun makeObject(): Queue<E?> {
        return SynchronizedQueue(LinkedList<E?>())
    }
}
