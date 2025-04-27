package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeMapSnippet {
    fun onMapSnippetOpen(openType: AmplitudePropertyMapSnippetOpenType, snippetType: AmplitudePropertyMapSnippetType)
    fun onMapSnippetClose(closeType: AmplitudePropertyMapSnippetCloseType, snippetType: AmplitudePropertyMapSnippetType)
}

class AmplitudeMapSnippetImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMapSnippet {

    override fun onMapSnippetOpen(
        openType: AmplitudePropertyMapSnippetOpenType,
        snippetType: AmplitudePropertyMapSnippetType
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapUserSnippetEventName.MAP_USER_SNIPPET_OPEN,
            properties = {
                it.apply {
                    addProperty(openType)
                    addProperty(snippetType)
                }
            }
        )
    }

    override fun onMapSnippetClose(
        closeType: AmplitudePropertyMapSnippetCloseType,
        snippetType: AmplitudePropertyMapSnippetType
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapUserSnippetEventName.MAP_USER_SNIPPET_CLOSE,
            properties = {
                it.apply {
                    addProperty(closeType)
                    addProperty(snippetType)
                }
            }
        )
    }
}
