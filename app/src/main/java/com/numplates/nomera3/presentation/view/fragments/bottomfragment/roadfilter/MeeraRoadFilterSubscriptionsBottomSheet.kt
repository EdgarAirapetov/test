package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.meera.core.extensions.doDelayed
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogSubscriptionsRoadFilterBinding

interface MeeraRoadFilterSubscriptionsCallback {
    fun onApply()
}

private const val DELAY_FILTERS_APPLYING = 100L

class MeeraRoadFilterSubscriptionsBottomSheet : UiKitBottomSheetDialog<MeeraDialogSubscriptionsRoadFilterBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogSubscriptionsRoadFilterBinding
        get() = MeeraDialogSubscriptionsRoadFilterBinding::inflate

    var callback: MeeraRoadFilterSubscriptionsCallback? = null

    private val viewModel by viewModels<RoadFilterSubscriptionsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        handleSwitchMyGroups()
        subscribeEvent()
        subscribeStateSubscribed()
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        labelText = getString(R.string.road_filter_subscriptions_group),
        dialogStyle = R.style.BottomSheetDialogTheme
    )

    private fun initButton() {
        contentBinding?.btnApplySubscriptionsFilters?.setThrottledClickListener {
            handleCheckSwitchMyGroups()
            doDelayed(DELAY_FILTERS_APPLYING) {
                callback?.onApply()
                dismiss()
            }
        }
    }

    private fun subscribeStateSubscribed() {
        viewModel.isGroupSubscribedState.observe(viewLifecycleOwner) { isSubscribed ->
            contentBinding?.cellMyCommunities?.setCellRightElementChecked(isSubscribed)
        }
    }

    private fun subscribeEvent() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RoadFilterSubscriptionEvent.Error -> {
                    showError()
                }
            }
        }
    }

    private fun showError() {
        UiKitSnackBar.make(
            requireView(),
            SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(R.string.community_subscribe_filter_change_error_text)
                )
            )
        )
    }

    private fun handleSwitchMyGroups() {
        contentBinding?.apply {
            cellMyCommunities.setRightElementContainerClickable(false)
            cellMyCommunities.setCellRightElementClickable(false)
            cellMyCommunities.setThrottledClickListener {
                cellMyCommunities.setCellRightElementChecked(!cellMyCommunities.getCellRightElementChecked())
            }
        }
    }

    private fun handleCheckSwitchMyGroups() {
        viewModel.setFilterMyGroups(contentBinding?.cellMyCommunities?.getCellRightElementChecked() == true)
    }

    companion object {
        const val TAG = "MeeraRoadFilterSubscriptionsBottomSheet"
    }

}
