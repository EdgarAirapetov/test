package com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.usersnippet

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudeMapSnippet
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.toAmplitudePropertyHow
import javax.inject.Inject

@AppScope
class MapSnippetAnalyticsRepositoryImpl @Inject constructor(
    private val amplitudeMapSnippet: AmplitudeMapSnippet
) : MapSnippetAnalyticsRepository {

    private var closeMethod: MapSnippetCloseMethod? = null

    override fun setCloseMethod(method: MapSnippetCloseMethod) {
        closeMethod = method
    }

    override fun logCloseEvent(snippetType: AmplitudePropertyMapSnippetType) {
        closeMethod
            ?.toAmplitudePropertyHow()
            ?.let { closeType ->
                amplitudeMapSnippet.onMapSnippetClose(
                    closeType = closeType,
                    snippetType = snippetType
                )
            }
        closeMethod = null
    }
}
