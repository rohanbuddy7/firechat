package com.tribeone.firechat.di.module

import com.tribeone.firechat.di.network.NetworkService
import com.tribeone.firechat.di.network.Networking
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
internal class ApplicationModule @Inject constructor() {

    @Provides
    fun networkService(): NetworkService = Networking.getInstance().create(NetworkService::class.java)

}