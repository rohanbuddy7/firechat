package com.tribeone.firechat.utils

import android.content.Context
import android.widget.Toast

internal class Toaster {

    companion object{
        fun show(context: Context, message: String){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

}