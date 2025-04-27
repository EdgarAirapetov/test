package com.numplates.nomera3.modules.devtools_bridge.presentation

import com.mera.bridge.devtools.IDevToolsBridge
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.devtools_bridge.data.DevToolsBridgeRepository
import javax.inject.Inject

class DevToolsBridgeInteractor : IDevToolsBridge {

    @Inject
    lateinit var repository: DevToolsBridgeRepository

    override fun initDevToolsBridge() {
        App.component.inject(this)
    }

    override fun onPostViewCollisionHighlightEnable(enable: Boolean) {
        repository.setPostViewCollisionHighlightEnable(enable)
    }
}