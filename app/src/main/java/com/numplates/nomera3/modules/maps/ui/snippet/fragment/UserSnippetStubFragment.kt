package com.numplates.nomera3.modules.maps.ui.snippet.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.numplates.nomera3.databinding.FragmentUserSnippetStubBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class UserSnippetStubFragment : BaseFragmentNew<FragmentUserSnippetStubBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserSnippetStubBinding
        get() = FragmentUserSnippetStubBinding::inflate
}
