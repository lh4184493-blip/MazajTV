package com.mazajtv

import android.app.Application

/**
 * Application entry point.
 * Holds no channel data by default — all channels come from a user-supplied
 * M3U playlist URL (see SettingsActivity) or the small demo playlist bundled
 * for testing purposes only.
 */
class MazajApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
