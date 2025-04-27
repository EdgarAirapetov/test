package com.meera.core.base

import android.view.View
import androidx.annotation.StringRes
import com.meera.core.utils.NToast

interface BaseInfoMessagesDelegate {

    fun showSuccessMessage(view: View?, message: String)

    fun showSuccessMessage(view: View?, @StringRes messageRes: Int)

    fun showErrorMessage(view: View?, @StringRes messageRes: Int)

}

class BaseInfoMessagesDelegateImpl : BaseInfoMessagesDelegate {

    override fun showSuccessMessage(view: View?, message: String) {
        NToast.with(view)
            .text(message)
            .typeSuccess()
            .show()
    }

    override fun showSuccessMessage(view: View?, @StringRes messageRes: Int) {
        val message = view?.context?.getString(messageRes)
        NToast.with(view)
            .text(message)
            .typeSuccess()
            .show()
    }

    override fun showErrorMessage(view: View?, @StringRes messageRes: Int) {
        val message = view?.context?.getString(messageRes)
        NToast.with(view)
            .text(message)
            .typeError()
            .show()
    }

}
