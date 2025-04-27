package com.numplates.nomera3.modules.baseCore.helper.amplitude.photo

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import javax.inject.Inject

interface AmplitudePhotoAnalytic {
    fun logPhotoScreenOpen(
        userId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        actionType: AmplitudePropertyMenuAction?
    )
}

class AmplitudePhotoAnalyticImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudePhotoAnalytic {

    override fun logPhotoScreenOpen(
        userId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        actionType: AmplitudePropertyMenuAction?,
    ) {
        delegate.logEvent(
            eventName = AmplitudePhotoEventName.PHOTO_SCREEN_OPEN,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    actionType?.let {
                        addProperty(AmplitudePropertyNameConst.ACTION_TYPE, actionType)
                    }
                    addProperty(where)
                }
            }
        )
    }

}
