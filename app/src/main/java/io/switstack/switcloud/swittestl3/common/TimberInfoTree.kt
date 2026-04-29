package io.switstack.switcloud.swittestl3.common

import android.util.Log
import timber.log.Timber

class TimberInfoTree() : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.INFO) {
            super.log(priority, tag, message, t)
        }
    }
}