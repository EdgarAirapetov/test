package com.numplates.nomera3.modules.devtools_bridge.data

import androidx.lifecycle.LiveData

interface DevToolsBridgeRepository {
    fun getPostViewCollisionHighlightLiveData(): LiveData<Boolean>
    fun setPostViewCollisionHighlightEnable(enable: Boolean)
}