package com.numplates.nomera3.modules.maps.domain.analytics

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.usersnippet.MapSnippetAnalyticsRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudeGeoPopup
import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudePropertyGeoPopupActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudePropertyGeoPopupWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map.AmplitudeMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudeMapSnippet
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import javax.inject.Inject

class MapAnalyticsInteractor @Inject constructor(
    private val fbAnalytic: FireBaseAnalytics,
    private val amplitudeMap: AmplitudeMap,
    private val amplitudeGeoPopup: AmplitudeGeoPopup,
    private val amplitudeMapSnippet: AmplitudeMapSnippet,
    private val mapSnippetAnalyticsRepository: MapSnippetAnalyticsRepository
) {
    fun logScreenForFragment(screenClass: String?) = fbAnalytic.logScreenForFragment(screenClass)

    fun logBackToMyLocation() = amplitudeMap.logBackToMyLocation()

    fun logOpenMap(where: AmplitudePropertyWhereOpenMap) = amplitudeMap.logOpenMap(where)

    fun logGeoPopupAction(actionType: AmplitudePropertyGeoPopupActionType, where: AmplitudePropertyGeoPopupWhere) =
        amplitudeGeoPopup.logGeoPopupAction(actionType = actionType, where = where)

    fun logMapSnippetOpen(
        openType: AmplitudePropertyMapSnippetOpenType,
        snippetType: AmplitudePropertyMapSnippetType
    ) = amplitudeMapSnippet.onMapSnippetOpen(openType = openType, snippetType = snippetType)

    fun setMapSnippetCloseMethod(closeMethod: MapSnippetCloseMethod) = mapSnippetAnalyticsRepository.setCloseMethod(closeMethod)

    fun logMapSnippetClose(snippetType: AmplitudePropertyMapSnippetType) =
        mapSnippetAnalyticsRepository.logCloseEvent(snippetType)
}
