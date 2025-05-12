package com.nowid.safe.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.nowid.safe.common.Dispatcher
import com.nowid.safe.common.NowIDDispatchers.IO
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import com.nowid.safe.datastore.PasswordStoreSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides a secure DataStore for encrypted password storage.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    /**
     * Creates and provides a [DataStore]<[PasswordStore]> for encrypted password data.
     *
     * @param context the application [Context] used for file storage
     * @param ioDispatcher the [CoroutineDispatcher] for IO-bound operations
     * @param scope the application-scoped [CoroutineScope] for DataStore
     * @param serializer the [PasswordStoreSerializer] for (de)serialization
     * @return a configured DataStore<PasswordStore> instance
     */
    @Provides
    @Singleton
    internal fun providesPasswordStore(
        @ApplicationContext context: Context,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
        serializer: PasswordStoreSerializer,
    ): DataStore<PasswordStore> =
        DataStoreFactory.create(
            serializer = serializer,
            scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        ) {
            context.dataStoreFile("encrypted_password.pb")
        }
}