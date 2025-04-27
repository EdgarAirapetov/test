package com.numplates.nomera3.modules.redesign.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.meera.core.R
import timber.log.Timber

private const val TAG = "MEERA_BASE_FRAGMENT"

// Класс для независимых экранов(авторизация, создание поста и тд)
open class MeeraBaseFragment(@LayoutRes layout: Int = R.layout.empty_layout) : Fragment(layout) {

    private val childClass = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("$TAG ON_CREATE: $childClass")
    }

    open fun onHideHints() {}

    open fun onAppHidden() {
        Timber.d("$TAG ON_APP_HIDDEN: $childClass")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("$TAG ON_VIEW_CREATED: $childClass")
    }

    override fun onStart() {
        super.onStart()
        Timber.d("$TAG ON_START: $childClass")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("$TAG ON_RESUME: $childClass")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("$TAG ON_PAUSE: $childClass")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("$TAG ON_STOP: $childClass")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("$TAG ON_DESTROY_VIEW: $childClass")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("$TAG ON_DESTROY: $childClass")
    }
}
