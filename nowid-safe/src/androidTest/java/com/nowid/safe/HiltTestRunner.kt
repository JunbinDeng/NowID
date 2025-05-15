package com.nowid.safe

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(
            cl,
            "com.nowid.safe.NowIDSafeTestApplication_Application",
            context
        )
    }
}