package com.tribeone.firechat.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.di.component.DaggerFragmentComponent
import com.tribeone.firechat.di.component.FragmentComponent
import com.tribeone.firechat.di.module.FragmentModule
import javax.inject.Inject

internal abstract class BaseFragment<VM: BaseViewModel>: Fragment() {

    @Inject
    lateinit var viewModel: VM

    /*override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(provideLayoutId(), container, false)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onViewCreated(view, savedInstanceState)
        injectDependencies(buildFragmentComponent())
        setupView()
        setupObservers()
    }

    private fun buildFragmentComponent() =
        DaggerFragmentComponent
            .builder()
            .applicationComponent((requireContext().applicationContext as MyApplication).applicationComponent)
            .fragmentModule(FragmentModule(this, setBuildVariant()))
            .build()


    protected abstract fun setBuildVariant(): String

    //protected abstract fun provideLayoutId(): Int

    protected abstract fun setupView()

    protected abstract fun injectDependencies(fragment: FragmentComponent)

    protected abstract fun setupObservers()


}