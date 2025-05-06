package com.nowid.sdk.testutil

import org.junit.Before
import timber.log.Timber

/**
 * Base class for unit tests that need Timber logging.
 *
 * This class initializes a safe Timber tree (ConsoleTree) for use in
 * local unit tests. Any test class that extends this will automatically
 * get consistent logging without depending on Android's Log system.
 *
 * To use:
 * ```
 * class MyTest : BaseUnitTest() {
 *     @Test fun `example`() { ... }
 * }
 * ```
 */
abstract class BaseUnitTest {
    @Before
    fun setupLogging() {
        if (Timber.forest().isEmpty()) {
            Timber.plant(ConsoleTree())
        }
    }
}