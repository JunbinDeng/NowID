package com.nowid.safe.data.di

import com.nowid.safe.common.Dispatcher
import com.nowid.safe.common.NowIDDispatchers.Default
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

/**
 * Dagger Hilt module that provides application-scoped CoroutineScope instances.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CoroutineScopesModule {
    /**
     * Creates and provides an application-scoped [CoroutineScope].
     *
     * @param dispatcher the [CoroutineDispatcher] used for coroutine execution
     * @return a [CoroutineScope] combining a [SupervisorJob] (allowing child
     *         coroutines to fail independently) with the given dispatcher
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun providesCoroutineScope(
        @Dispatcher(Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}