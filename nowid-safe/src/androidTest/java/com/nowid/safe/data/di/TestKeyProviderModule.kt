package com.nowid.safe.data.di

import com.nowid.safe.datastore.KeyProvider
import com.nowid.safe.datastore.TestKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CryptoModule::class]
)
object TestKeyProviderModule {
    @Provides
    fun provideTestKeyProvider(): KeyProvider {
        return TestKeyProvider()
    }
}
