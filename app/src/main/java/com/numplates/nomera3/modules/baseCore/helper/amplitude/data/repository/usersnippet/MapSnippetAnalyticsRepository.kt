package com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.usersnippet

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod

interface MapSnippetAnalyticsRepository {

    fun setCloseMethod(method: MapSnippetCloseMethod)

    fun logCloseEvent(snippetType: AmplitudePropertyMapSnippetType)
}
