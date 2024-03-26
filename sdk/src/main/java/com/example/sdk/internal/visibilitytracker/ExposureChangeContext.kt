package com.example.sdk.internal.visibilitytracker

class ExposureChangeContext(
    callback: VisibilityObserverCallback,
) : VisibilityObserverContext(true, callback) {
    override fun internalCheck(entry: VisibilityObserverEntry) {
        if (entry != oldEntry) {
            fire(entry)
        }
    }
}
