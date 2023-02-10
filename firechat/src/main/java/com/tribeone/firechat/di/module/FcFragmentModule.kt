package com.tribeone.firechat.di.module

import androidx.lifecycle.ViewModelProviders
import com.tribeone.firechat.di.network.FcNetworkService
import com.tribeone.firechat.di.network.FcNetworking
import com.tribeone.firechat.utils.FcViewModelProviderFactory
import com.tribeone.firechat.ui.base.FcBaseFragment
import com.tribeone.firechat.ui.message.FcChatViewModel
import dagger.Module
import dagger.Provides


@Module
internal class FcFragmentModule(private val fragmentFc: FcBaseFragment<*>, private val buildvariant: String) {

    @Provides
    fun networkService(): FcNetworkService = FcNetworking.getInstance().create(FcNetworkService::class.java)

    @Provides
    fun provideChatViewModel(
        fcNetworkService: FcNetworkService
    ): FcChatViewModel =
        ViewModelProviders.of(
            fragmentFc,
            FcViewModelProviderFactory(FcChatViewModel::class) {
                FcChatViewModel(
                    buildvariant,
                    fcNetworkService
                )
            })[FcChatViewModel::class.java]

}