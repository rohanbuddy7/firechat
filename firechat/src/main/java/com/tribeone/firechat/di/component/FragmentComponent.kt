package com.tribeone.firechat.di.component

import com.tribeone.firechat.di.module.FragmentModule
import com.tribeone.firechat.ui.message.MessageFragment
import com.tribeone.firechat.ui.chatlist.ChatlistFragment
import dagger.Component

@Component(
    dependencies = [ApplicationComponent::class],
    modules = [FragmentModule::class])
internal interface FragmentComponent {

    fun inject(fragment: MessageFragment)

    fun inject(fragment: ChatlistFragment)

}