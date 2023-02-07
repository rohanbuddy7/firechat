package com.tribeone.firechat.di.module

import com.tribeone.firechat.di.network.FcNetworkService
import com.tribeone.firechat.di.network.FcNetworking
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
internal class FcApplicationModule @Inject constructor() {

    @Provides
    fun networkService(): FcNetworkService = FcNetworking.getInstance().create(FcNetworkService::class.java)

}