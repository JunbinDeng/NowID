package com.nowid.safe.data.di

import com.nowid.safe.common.Dispatcher
import com.nowid.safe.common.NowIDDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Provides Hilt-injected [CoroutineDispatcher] instances for IO and Default contexts.
 *
 * Benefits:
 *  - Single Source of Truth: Ensures consistent use of IO vs. Default dispatchers.
 *  - Explicit Intention: Injection sites clearly state their concurrency requirements.
 *  - Testability: Allows replacing dispatchers in unit tests for deterministic behavior.
 *  - Consistency: Keeps dispatcher bindings in the same Hilt graph as other dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Dispatcher(NowIDDispatchers.IO)
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(NowIDDispatchers.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}