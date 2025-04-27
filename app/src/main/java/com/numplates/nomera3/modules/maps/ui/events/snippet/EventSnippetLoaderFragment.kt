package com.numplates.nomera3.modules.maps.ui.events.snippet

import android.view.LayoutInflater
import android.view.ViewGroup
import com.numplates.nomera3.databinding.FragmentEventSnippetLoaderBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class EventSnippetLoaderFragment : BaseFragmentNew<FragmentEventSnippetLoaderBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEventSnippetLoaderBinding
        get() = FragmentEventSnippetLoaderBinding::inflate
}
