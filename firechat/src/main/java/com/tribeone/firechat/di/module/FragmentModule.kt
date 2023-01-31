package com.tribeone.firechat.di.module

import androidx.lifecycle.ViewModelProviders
import com.tribeone.firechat.di.network.NetworkService
import com.tribeone.firechat.utils.ViewModelProviderFactory
import com.tribeone.firechat.ui.base.BaseFragment
import com.tribeone.firechat.ui.message.ChatViewModel
import dagger.Module
import dagger.Provides
import javax.annotation.Nullable


@Module
internal class FragmentModule(private val fragment: BaseFragment<*>, private val buildvariant: String) {

    @Provides
    fun provideChatViewModel(
        networkService: NetworkService
    ): ChatViewModel =
        ViewModelProviders.of(
            fragment,
            ViewModelProviderFactory(ChatViewModel::class) {
                ChatViewModel(
                    buildvariant,
                    networkService
                )
            })[ChatViewModel::class.java]

}