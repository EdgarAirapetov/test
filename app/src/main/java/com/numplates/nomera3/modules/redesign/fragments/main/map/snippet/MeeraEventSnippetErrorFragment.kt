package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.applyRoundedOutline
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentSnippetErrorBinding
import com.numplates.nomera3.modules.maps.ui.events.snippet.PageBottomSheetBehavior
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventSnippetViewController

private const val RADIUS_SMALL = 12
private const val RADIUS_MEDIUM = 18
private const val SNACK_MARGIN = 32

class MeeraEventSnippetErrorFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_snippet_error,
    behaviourConfigState = ScreenBehaviourState.Empty
), MapSnippetPage {
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentSnippetErrorBinding::bind)

    private var behavior: BottomSheetBehavior<*>? = null
    private var bottomSheetCallback: PageBottomSheetCallback? = null

    private var eventSnippetViewController: MeeraEventSnippetViewController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        setupController()
        setupBottomSheetBehavior()
        setupPageUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetCallback?.let {
            behavior?.removeBottomSheetCallback(it)
        }
        bottomSheetCallback = null
    }

    override fun onSelectPage() = Unit

    override fun onDestroyPage() = Unit

    override fun setSnippetState(snippetState: SnippetState.StableSnippetState) {
        behavior?.state = snippetState.behaviorValue
    }

    override fun setSnippetHeight(height: Int) = Unit

    override fun getSnippetState(): SnippetState = behavior?.state
        ?.let(SnippetState::fromBehaviorValue)
        ?: SnippetState.Closed

    override fun getPageIndex(): Int = 0

    override fun getSnippetHeight(): Int = binding.root.height

    override fun getSnippetView(): View? = view

    private fun setupController() {
        eventSnippetViewController = (parentFragment as? MainMapFragment)?.getEventSnippetViewController()
    }

    private fun setupBottomSheetBehavior() {
        behavior = PageBottomSheetBehavior<View>(requireContext()).apply {
            isHideable = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
            PageBottomSheetCallback().let {
                bottomSheetCallback = it
                addBottomSheetCallback(it)
            }
        }
        val params = binding.vgBottomSheetSnippetError.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = behavior
        binding.root.post { behavior?.state = BottomSheetBehavior.STATE_EXPANDED }
    }

    private fun setupPageUi() {
        binding.apply {
            viewShimmerMedia.applyRoundedOutline(RADIUS_SMALL.dp.toFloat())
            viewShimmerTitle.applyRoundedOutline(RADIUS_SMALL.dp.toFloat())
            viewShimmerDescription.applyRoundedOutline(RADIUS_SMALL.dp.toFloat())
            viewShimmerParticipantOne.applyRoundedOutline(RADIUS_MEDIUM.dp.toFloat())
            viewShimmerParticipantTwo.applyRoundedOutline(RADIUS_MEDIUM.dp.toFloat())
            viewShimmerParticipantThree.applyRoundedOutline(RADIUS_MEDIUM.dp.toFloat())
            viewShimmerUserpic.applyRoundedOutline(RADIUS_MEDIUM.dp.toFloat())
            viewShimmerButton.applyRoundedOutline(RADIUS_MEDIUM.dp.toFloat())
        }
        val snackView = parentFragment?.view ?: return
        UiKitSnackBar.makeError(
            view = snackView,
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(R.string.no_internet)
                )
            )
        ).apply {
            val layoutParams = view.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.gravity = Gravity.TOP
            layoutParams.topMargin = SNACK_MARGIN.dp
            view.layoutParams = layoutParams
        }.show()
    }

    private inner class PageBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val snippetState = SnippetState.fromBehaviorValue(newState)
            eventSnippetViewController?.onErrorSnippetState(snippetState)
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
    }
}
