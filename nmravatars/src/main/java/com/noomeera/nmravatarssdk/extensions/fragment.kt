package com.noomeera.nmravatarssdk.extensions

import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
internal fun Fragment.hideKeyboard() {
    (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { inputMethodManager ->
        val token = activity?.currentFocus?.windowToken ?: activity?.window?.decorView?.windowToken
        inputMethodManager.hideSoftInputFromWindow(token, 0)
    }
}
