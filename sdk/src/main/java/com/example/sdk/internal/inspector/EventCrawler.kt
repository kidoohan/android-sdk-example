package com.example.sdk.internal.inspector

/**
 * The interface that provides middlewares, bindings or hooks into certain frameworks or
 * environments, along with code that inserts those bindings and activates them.
 */
fun interface EventCrawler {
    /**
     * Registers an [EventHub]
     *
     * @param hub the [EventHub].
     */
    fun register(hub: EventHub)
}
