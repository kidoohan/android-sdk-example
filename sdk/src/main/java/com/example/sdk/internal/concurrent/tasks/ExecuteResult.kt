package com.example.sdk.internal.concurrent.tasks

interface ExecuteResult<TResult> {
    fun onComplete(task: Task<TResult>)
    fun cancel()
}
