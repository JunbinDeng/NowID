package com.nowid.safe.data.di

import com.nowid.safe.data.repository.DefaultPasswordRepository
import com.nowid.safe.data.repository.PasswordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger Hilt module that binds repository implementations to their abstractions.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindPasswordRepository(passwordRepository: DefaultPasswordRepository): PasswordRepository
}