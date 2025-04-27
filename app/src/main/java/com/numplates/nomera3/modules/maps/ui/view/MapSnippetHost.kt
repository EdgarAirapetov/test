package com.numplates.nomera3.modules.maps.ui.view

import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetViewController

interface MapSnippetHost {
    fun getEventSnippetViewController(): EventSnippetViewController?
}
