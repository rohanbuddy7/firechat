package com.tribeone.firechat.di.component

import com.tribeone.firechat.di.module.FcFragmentModule
import com.tribeone.firechat.di.network.FcNetworkService
import com.tribeone.firechat.ui.message.FcMessageFragment
import com.tribeone.firechat.ui.chatlist.FcChatlistFragment
import dagger.Component

@Component(
    //dependencies = [FcApplicationComponent::class],
    modules = [FcFragmentModule::class])
internal interface FcFragmentComponent {

    fun inject(fragmentFcMessageFragment: FcMessageFragment)

    fun inject(fragmentFcChatlistFragment: FcChatlistFragment)

    fun getNetworkService(): FcNetworkService

}