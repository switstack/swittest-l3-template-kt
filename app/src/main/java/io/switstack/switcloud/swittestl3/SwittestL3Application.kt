package io.switstack.switcloud.swittestl3

import android.app.Application
import io.switstack.switcloud.swittestl3.common.TimberInfoTree
import io.switstack.switcloud.swittestl3.data.settings.DataStoreManager
import io.switstack.switcloud.swittestl3.data.settings.SettingsRepository
import io.switstack.switcloud.swittestl3.di.swittestL3Module
import io.switstack.switcloud.swittestl3.ui.settings.SettingsViewModel
import org.koin.core.context.startKoin
import timber.log.Timber

class SwittestL3Application : Application() {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(DataStoreManager(this))
    }

    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(settingsRepository)
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(swittestL3Module)
        }

        Timber.plant(
            if (BuildConfig.DEBUG) {
                Timber.DebugTree()
            } else {
                TimberInfoTree()
            }
        )
    }
}