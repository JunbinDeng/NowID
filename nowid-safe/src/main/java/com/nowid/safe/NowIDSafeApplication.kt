package com.nowid.safe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
open class NowIDSafeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // Print all logs
            Timber.plant(Timber.DebugTree())
        } else {
            // Report to crashlytics
        }
    }
}