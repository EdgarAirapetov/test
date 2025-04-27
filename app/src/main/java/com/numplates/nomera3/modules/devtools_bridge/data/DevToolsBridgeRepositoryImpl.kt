package com.numplates.nomera3.modules.devtools_bridge.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.meera.core.di.scopes.AppScope
import javax.inject.Inject


@AppScope
class DevToolsBridgeRepositoryImpl @Inject constructor() : DevToolsBridgeRepository {

    private var enablePostViewCollisionHighlightLiveData = MutableLiveData<Boolean>()

    override fun getPostViewCollisionHighlightLiveData(): LiveData<Boolean> {
        return enablePostViewCollisionHighlightLiveData
    }

    override fun setPostViewCollisionHighlightEnable(enable: Boolean) {
        enablePostViewCollisionHighlightLiveData.value = enable
    }
}
