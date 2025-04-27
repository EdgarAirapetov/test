package com.numplates.nomera3.modules.maps.ui.events.snippet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.FragmentSnippetErrorBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetHost
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class EventSnippetErrorFragment : BaseFragmentNew<FragmentSnippetErrorBinding>(), MapSnippetPage {

    private var behavior: BottomSheetBehavior<*>? = null
    private var bottomSheetCallback: PageBottomSheetCallback? = null

    private var eventSnippetViewController: EventSnippetViewController? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSnippetErrorBinding
        get() = FragmentSnippetErrorBinding::inflate

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

    override fun getSnippetHeight(): Int = binding?.layoutSnippetError?.root?.height ?: 0

    override fun getSnippetView(): View? = view

    private fun setupController() {
        eventSnippetViewController = (parentFragment as? MapSnippetHost)?.getEventSnippetViewController()
    }

    private fun setupBottomSheetBehavior() {
        val params = binding?.layoutSnippetError?.root?.layoutParams as CoordinatorLayout.LayoutParams
        val needToAnimateSnippetEnter = true
        behavior = PageBottomSheetBehavior<View>(requireContext()).apply {
            isHideable = true
            skipCollapsed = true
            state = if (needToAnimateSnippetEnter) {
                BottomSheetBehavior.STATE_HIDDEN
            } else {
                BottomSheetBehavior.STATE_EXPANDED
            }
            PageBottomSheetCallback().let {
                bottomSheetCallback = it
                addBottomSheetCallback(it)
            }
        }
        params.behavior = behavior
        binding?.layoutSnippetError?.root?.layoutParams = params
        if (needToAnimateSnippetEnter) {
            binding?.layoutSnippetError?.root?.post { behavior?.state = BottomSheetBehavior.STATE_EXPANDED }
        }
    }

    private fun setupPageUi() {
        binding?.layoutSnippetError?.apply {
            ibSnippetErrorClose.setThrottledClickListener {
                eventSnippetViewController?.setMapEventSnippetCloseMethod(MapSnippetCloseMethod.CLOSE_BUTTON)
                eventSnippetViewController?.closeSnippet()
            }
            tvSnippetErrorAction.setThrottledClickListener {
                eventSnippetViewController?.retryDataLoading()
            }
        }
    }

    private inner class PageBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val snippetState = SnippetState.fromBehaviorValue(newState)
            eventSnippetViewController?.onErrorSnippetState(snippetState)
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
    }
}
