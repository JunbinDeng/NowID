package com.nowid.safe.datastore

import androidx.datastore.core.DataStore
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestDataStore(
    initial: PasswordStore = PasswordStore.getDefaultInstance()
) : DataStore<PasswordStore> {
    private val _state = MutableStateFlow(initial)
    override val data: StateFlow<PasswordStore> get() = _state

    override suspend fun updateData(transform: suspend (PasswordStore) -> PasswordStore): PasswordStore {
        val newValue = transform(_state.value)
        _state.value = newValue
        return newValue
    }
}