package com.nowid.safe.common

import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Qualifier annotation for injecting a specific [CoroutineDispatcher] instance.
 *
 * Use this annotation to distinguish between different NowID coroutine dispatchers.
 *
 * @property dispatcher the [NowIDDispatchers] value specifying the dispatcher type
 */
@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val dispatcher: NowIDDispatchers)

enum class NowIDDispatchers {
    Default,
    IO,
}