package com.numplates.nomera3.modules.appInfo.ui.mapper

import com.numplates.nomera3.modules.appInfo.data.entity.UpdateRecommendations
import com.numplates.nomera3.modules.appInfo.ui.entity.ForceUpdateDialogEntity

fun UpdateRecommendations.toDialogEntity(appVersion: String?): ForceUpdateDialogEntity {
    val data =  ForceUpdateDialogEntity(
        this.title,
        this.subtitle,
        this.isForceUpdate ?: true,
        this.btnText
    )
    data.appVersion = appVersion
    return data
}