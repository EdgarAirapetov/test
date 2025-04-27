package com.numplates.nomera3.modules.maps.ui.snippet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.core.extensions.onMeasured
import com.numplates.nomera3.databinding.FragmentUserSnippetLoaderBinding
import com.numplates.nomera3.modules.userprofile.ui.ProfileUiUtils
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class UserSnippetLoaderFragment : BaseFragmentNew<FragmentUserSnippetLoaderBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserSnippetLoaderBinding
        get() = FragmentUserSnippetLoaderBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.onMeasured {
            val snippetHeight = ProfileUiUtils.getSnippetHeight(context)
            val loaderContainerParams = binding?.vgLoaderContainer?.layoutParams
            loaderContainerParams?.height = snippetHeight
            binding?.vgLoaderContainer?.layoutParams = loaderContainerParams
        }
    }
}
