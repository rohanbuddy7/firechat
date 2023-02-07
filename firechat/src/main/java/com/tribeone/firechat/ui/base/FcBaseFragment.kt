package com.tribeone.firechat.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.di.component.DaggerFcFragmentComponent
import com.tribeone.firechat.di.component.FcFragmentComponent
import com.tribeone.firechat.di.module.FcFragmentModule
import javax.inject.Inject

internal abstract class FcBaseFragment<VM: FcBaseViewModel>: Fragment() {

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
        DaggerFcFragmentComponent
            .builder()
            .fcApplicationComponent((requireContext().applicationContext as MyApplication).fcApplicationComponent)
            .fcFragmentModule(FcFragmentModule(this, setBuildVariant()))
            .build()


    protected abstract fun setBuildVariant(): String

    //protected abstract fun provideLayoutId(): Int

    protected abstract fun setupView()

    protected abstract fun injectDependencies(fcFragment: FcFragmentComponent)

    protected abstract fun setupObservers()


}