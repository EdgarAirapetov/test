package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import android.os.Bundle
import android.view.View
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.onMeasured
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserSnippetLoaderBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.userprofile.ui.ProfileUiUtils

class MeeraUserSnippetLoaderFragment : MeeraBaseDialogFragment(
    layout = R.layout.fragment_user_snippet_loader ,
    behaviourConfigState = ScreenBehaviourState.Snippet(percentHeight = 0.95f)
) {
    private val binding by viewBinding(FragmentUserSnippetLoaderBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_first_container_view

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
