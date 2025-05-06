package com.nowid.sdk.testutil

import timber.log.Timber

/**
 * A Timber Tree that logs messages to the console using println().
 * Safe to use in local JVM unit tests where android.util.Log is not available.
 */
class ConsoleTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("[${tag ?: "Timber"}] $message")
    }
}