package com.tribeone.firechat.utils

import timber.log.Timber

internal object Logger {

    fun debug(text: String){
        Timber.d(text)
    }

    fun error(text: String){
        Timber.e(text)
    }

}