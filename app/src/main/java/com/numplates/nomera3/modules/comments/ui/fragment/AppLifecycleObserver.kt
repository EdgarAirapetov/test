package com.numplates.nomera3.modules.comments.ui.fragment

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(private val onAppBackgrounded: () -> Unit) : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        onAppBackgrounded()
    }
}
