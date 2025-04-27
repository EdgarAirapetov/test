package com.numplates.nomera3.modules.redesign.fragments.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager

private const val AMOUNT_FRAGMENT_IN_STACK = 2

class MeeraEmptyMapFragment : MeeraBaseDialogFragment(behaviourConfigState = ScreenBehaviourState.MapTransparent) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override val isBottomNavBarVisibility: UiKitNavigationBarViewVisibilityState
        get() = checkMapViewState()

    override fun onStart() {
        super.onStart()
        if (NavigationManager.getManager().mainMapFragment.getStateEventView() !is EventConfigurationState.Configuration) {
            NavigationManager.getManager().isMapMode = true
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isFromService = arguments?.getBoolean(ARG_MEERA_FROM_SERVICE) ?: false

        if (isFromService) {
            NavigationManager.getManager().mainMapFragment.openEventList(true)
            arguments?.remove(ARG_MEERA_FROM_SERVICE)
        }

        // TODO: FIXED AFTER REDESIGN MAP FRAGMENT !!!
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (NavigationManager.getManager().mainMapFragment.getStateOfFriendListView() != STATE_HIDDEN) {
                NavigationManager.getManager().mainMapFragment.closeFriendList()
            } else {
                val stack = findNavController().currentBackStack.value
                if (stack.size > AMOUNT_FRAGMENT_IN_STACK) {
                    val prevPosition = stack[stack.size - AMOUNT_FRAGMENT_IN_STACK]
                    if (prevPosition.destination.id == R.id.mainRoadFragment) {
                        handleBackPressedOnEmptyMap()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun handleBackPressedOnEmptyMap() {
        val mapFragment = NavigationManager.getManager().mainMapFragment
        val eventViewState = mapFragment.getStateEventView()
        if (eventViewState == EventConfigurationState.Closed || eventViewState == EventConfigurationState.Empty) {
            requireActivity().finish()
        } else {
            mapFragment.handleEventConfigurationEvent(EventConfigurationEvent.UiCloseInitiated)
        }
    }

    override fun onResume() {
        super.onResume()
        NavigationManager.getManager().mainMapFragment.initNavigationButtonsListeners(fromMap = true)
    }

    private fun checkMapViewState(): UiKitNavigationBarViewVisibilityState {
        return if (NavigationManager.getManager().mainMapFragment.getStateOfFriendListView() != STATE_HIDDEN
            || !NavigationManager.getManager().mainMapFragment.getShouldShowBottomNavBarInEmptyFragment()) {
            UiKitNavigationBarViewVisibilityState.GONE
        } else {
            UiKitNavigationBarViewVisibilityState.VISIBLE
        }
    }


    companion object {
        const val ARG_MEERA_FROM_SERVICE = "ARG_MEERA_FROM_SERVICE"
    }
}
