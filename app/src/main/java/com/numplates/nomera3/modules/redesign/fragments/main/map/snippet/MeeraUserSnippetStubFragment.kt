package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState

class MeeraUserSnippetStubFragment : MeeraBaseDialogFragment(
    R.layout.fragment_user_snippet_stub,
    ScreenBehaviourState.Snippet()
)  {
    override val containerId: Int
        get() = R.id.fragment_first_container_view
}
