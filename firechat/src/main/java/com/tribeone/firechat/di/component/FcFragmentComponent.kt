package com.tribeone.firechat.di.component

import com.tribeone.firechat.di.module.FcFragmentModule
import com.tribeone.firechat.ui.message.FcMessageFragmentFc
import com.tribeone.firechat.ui.chatlist.FcChatlistFragmentFc
import dagger.Component

@Component(
    dependencies = [FcApplicationComponent::class],
    modules = [FcFragmentModule::class])
internal interface FcFragmentComponent {

    fun inject(fragmentFc: FcMessageFragmentFc)

    fun inject(fragmentFc: FcChatlistFragmentFc)

}