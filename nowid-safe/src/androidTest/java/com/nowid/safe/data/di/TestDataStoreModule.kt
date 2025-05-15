package com.nowid.safe.data.di

import androidx.datastore.core.DataStore
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import com.nowid.safe.datastore.TestDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class]
)
object TestDataStoreModule {
    @Provides
    fun provideTestDataStore(): DataStore<PasswordStore> = TestDataStore()
}