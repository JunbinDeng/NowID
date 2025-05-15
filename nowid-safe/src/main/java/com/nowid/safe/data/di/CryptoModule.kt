package com.nowid.safe.data.di

import com.nowid.safe.datastore.AesGcmKeyProvider
import com.nowid.safe.datastore.KeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {
    @Provides
    fun provideKeyProvider(): KeyProvider {
        return AesGcmKeyProvider()
    }
}