package com.tribeone.firechat.di.component

import com.tribeone.firechat.FcMyApplication
import com.tribeone.firechat.di.module.FcApplicationModule
import com.tribeone.firechat.di.network.FcNetworkService
import dagger.Component

@Component(modules = [FcApplicationModule::class])
internal interface FcApplicationComponent {

    fun inject(applicationFc: FcMyApplication)

    fun getNetworkService(): FcNetworkService

}