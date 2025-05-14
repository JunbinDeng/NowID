package com.nowid.safe.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.nowid.safe.data.PasswordStoreOuterClass.PasswordStore
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * An [androidx.datastore.core.Serializer] for the [PasswordStore] proto.
 */
class PasswordStoreSerializer @Inject constructor() : Serializer<PasswordStore> {
    override val defaultValue: PasswordStore = PasswordStore.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PasswordStore =
        try {
            // readFrom is already called on the data store background thread
            PasswordStore.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: PasswordStore, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}