package com.numplates.nomera3.modules.devtools_bridge.domain

import androidx.lifecycle.LiveData
import com.numplates.nomera3.modules.devtools_bridge.data.DevToolsBridgeRepository
import javax.inject.Inject

class GetPostViewCollisionHighlightEnableUseCase @Inject constructor(
    private val repository: DevToolsBridgeRepository
) {
    fun execute(): LiveData<Boolean> {
        return repository.getPostViewCollisionHighlightLiveData()
    }
}