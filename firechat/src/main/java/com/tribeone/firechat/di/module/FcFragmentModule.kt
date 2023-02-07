package com.tribeone.firechat.di.module

import androidx.lifecycle.ViewModelProviders
import com.tribeone.firechat.di.network.FcNetworkService
import com.tribeone.firechat.utils.ViewModelProviderFactory
import com.tribeone.firechat.ui.base.FcBaseFragment
import com.tribeone.firechat.ui.message.FcChatViewModelFc
import dagger.Module
import dagger.Provides


@Module
internal class FcFragmentModule(private val fragmentFc: FcBaseFragment<*>, private val buildvariant: String) {

    @Provides
    fun provideChatViewModel(
        fcNetworkService: FcNetworkService
    ): FcChatViewModelFc =
        ViewModelProviders.of(
            fragmentFc,
            ViewModelProviderFactory(FcChatViewModelFc::class) {
                FcChatViewModelFc(
                    buildvariant,
                    fcNetworkService
                )
            })[FcChatViewModelFc::class.java]

}