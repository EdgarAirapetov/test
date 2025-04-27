package com.numplates.nomera3.modules.redesign.fragments.main.map

import com.numplates.nomera3.modules.maps.ui.model.MapMode

interface MeeraMapSnippetHost {

    fun getEventSnippetViewController(): MeeraEventSnippetViewController?
    fun setMapMode(mapMode: MapMode)
}
