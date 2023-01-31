package com.tribeone.firechat.di.component

import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.di.module.ApplicationModule
import com.tribeone.firechat.di.network.NetworkService
import dagger.Component

@Component(modules = [ApplicationModule::class])
internal interface ApplicationComponent {

    fun inject(application: MyApplication)

    fun getNetworkService(): NetworkService

}