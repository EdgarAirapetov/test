package com.numplates.nomera3.modules.maps.domain.repository

import com.numplates.nomera3.modules.maps.domain.model.GetMapObjectsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.GetUserSnippetsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.MapObjectsModel
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel

interface MapDataRepository {

    suspend fun getMapObjects(params: GetMapObjectsParamsModel): MapObjectsModel

    suspend fun getUserSnippets(params: GetUserSnippetsParamsModel): List<UserSnippetModel>

    fun setUserSnippetOnboardingShown()
    fun needToShowUserSnippetOnboarding(): Boolean

    fun setGeoPopupShown()
    fun needToShowGeoPopup(): Boolean
    fun resetGeoPopupShownCount()
}
