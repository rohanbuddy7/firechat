package com.tribeone.firechat.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

internal object ModUtils {

    fun forceCloseKeyboard(activity: Activity) {
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun closeKeyboardInstantly(context: Context, view: View?) {
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun closeKeyboard(context: Context, view: View?) {
        if (view != null) {
            Handler().postDelayed({
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }, 200)
        }
    }

    fun getBuildVariantMessage(buildVariant: String?): String{
        return buildVariant+"Message"
    }

    fun getBuildVariantUsers(buildVariant: String?): String{
        return buildVariant+"Users"
    }


}