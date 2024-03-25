package com.example.sdk.internal.inspector

/** The interface implemented by classes to participate in serialization to [Map]. */
fun interface MapSerializable<T> {
    /** Returns a [Map] containing the values of the members of this instance. */
    fun toMap(): Map<String, T>
}
