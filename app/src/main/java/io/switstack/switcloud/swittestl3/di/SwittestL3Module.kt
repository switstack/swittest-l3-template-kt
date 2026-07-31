package io.switstack.switcloud.swittestl3.di

import io.switstack.switcloud.switcloudclt.di.switcloudClientModule
import io.switstack.switcloud.switcloudclt.domain.SwitcloudTestClient
import io.switstack.switcloud.switcloudclt.internal.SwitcloudTestClientImpl
import io.switstack.switcloud.switcloudl2.ISwitcloudL2
import io.switstack.switcloud.switcloudl2.SwitcloudL2
import io.switstack.switcloud.swittestl3.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val swittestL3Module = module {

/*
    If the selected L2 implementation inherits ISwitcloudL2Test,
    the dependency switcloud-l2-kt should be replaced by switcloud-l2-domain-kt (version 2.2.0 or above),
    the dependency switcloud-clt-kt should be updated to version 2.4.0 or above
    and the injection should bind to the 2 interfaces like the following line :
    single { CustomL2.getInstance() } binds arrayOf(ISwitcloudL2::class, ISwitcloudL2Test::class)
 */

    single<ISwitcloudL2> { SwitcloudL2.getInstance() }

    includes(switcloudClientModule)
    factory<SwitcloudTestClient> { (serverUrl: String) ->
        SwitcloudTestClientImpl(serverUrl)
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .also {
                if (BuildConfig.DEBUG) {
                    it.addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        }
                    )
                }
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
    }
}