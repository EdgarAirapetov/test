package com.noomeera.nmravatarssdk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_BACK_PRESSED
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmravatarssdk.extensions.hideKeyboard

abstract class MeeraBaseFragment : Fragment() {

    abstract val layout: Int
    protected open val hideKeyboardAfterCreate = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(layout, null).also {
        if (hideKeyboardAfterCreate) hideKeyboard()
    }

    protected fun navigateToResult(resultJson: String) {
        setFragmentResult(
            REQUEST_NMR_KEY_AVATAR, bundleOf(NMR_AVATAR_STATE_JSON_KEY to resultJson)
        )
    }

    protected fun backToRoot() {
        setFragmentResult(
            REQUEST_NMR_BACK_PRESSED, bundleOf()
        )
    }

    open fun onBackPressed(): Boolean {
        return false
    }
}
