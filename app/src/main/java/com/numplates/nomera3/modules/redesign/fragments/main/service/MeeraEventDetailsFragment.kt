package com.numplates.nomera3.modules.redesign.fragments.main.service

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.findFragment
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.comments.ui.fragment.MeeraPostFragmentV2
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState

private const val SNIPPET_HEIGHT = 620
private const val SNIPPET_MARGIN = 0

class MeeraEventDetailsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_event_details,
    behaviourConfigState = ScreenBehaviourState.Snippet(height = SNIPPET_HEIGHT, horizontalMargin = SNIPPET_MARGIN)
) {
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentFragment = MeeraPostFragmentV2().apply {
            arguments = this@MeeraEventDetailsFragment.arguments
        }
        findFragment<Fragment>(view).childFragmentManager.beginTransaction()
            .replace(R.id.vg_event_post, contentFragment)
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.fade_out,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            .commit()
    }
}
