package com.tribeone.firechat.ui.base

import androidx.lifecycle.ViewModel

internal abstract class BaseViewModel() : ViewModel() {

    override fun onCleared() {
        super.onCleared()
    }

}