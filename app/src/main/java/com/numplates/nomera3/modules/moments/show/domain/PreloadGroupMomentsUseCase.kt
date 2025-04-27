package com.numplates.nomera3.modules.moments.show.domain

import android.content.Context
import com.numplates.nomera3.modules.moments.util.MomentPreloadItem
import com.numplates.nomera3.modules.moments.util.MomentsPreloadUtil
import javax.inject.Inject

class PreloadGroupMomentsUseCase @Inject constructor(
    applicationContext: Context
) {
    private val momentsPreloadUtil = MomentsPreloadUtil(applicationContext)

    fun invoke(momentsToPreload: List<MomentPreloadItem>) {
        momentsPreloadUtil.preload(momentsToPreload)
    }
}
