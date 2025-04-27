package com.numplates.nomera3.modules.communities.ui

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import timber.log.Timber

private const val DELAY_DELETE_COMMUNITY_SEC = 4L

class CommunityChangesViewController(
    private val view: View?,
    private val deleteCommunity: (Long) -> Unit,
    private val restoreCommunity: (Long) -> Unit
) {
    private var pendingDeleteSnackbar: UiKitSnackBar? = null

    fun handleCommunityListEvents(event: CommunityListEvents?) {
        when (event) {
            is CommunityListEvents.StartDeletion -> startDeletion(event.communityId)
            else -> Timber.d("Unsupported event")
        }
    }

    private fun startDeletion(communityId: Long) {
        showUndoCommunityDeletionView(communityId = communityId)
    }

    private fun showUndoCommunityDeletionView(communityId: Long) {
        view?.let {
            pendingDeleteSnackbar = UiKitSnackBar.make(
                view = view,
                params = SnackBarParams(
                    snackBarViewState = SnackBarContainerUiState(
                        messageText = it.context.getText(R.string.meera_snack_bar_nofitification_title_community_deleted),
                        loadingUiState = SnackLoadingUiState.DonutProgress(
                            timerStartSec = DELAY_DELETE_COMMUNITY_SEC,
                            onTimerFinished = {
                                onUndoCommunityDeletionViewResult(
                                    communityId = communityId,
                                    isCommunityShouldBeDeleted = true
                                )
                            }
                        ),
                        buttonActionText = it.context.getText(R.string.cancel),
                        buttonActionListener = {
                            pendingDeleteSnackbar?.dismiss()
                        }
                    ),
                    duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                    dismissOnClick = true,
                    dismissListeners = DismissListeners(
                        dismissListener = {
                            pendingDeleteSnackbar?.dismiss()
                        }
                    ),
                )
            )

            pendingDeleteSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
            pendingDeleteSnackbar?.show()
        }
    }

    private fun onUndoCommunityDeletionViewResult(
        communityId: Long,
        isCommunityShouldBeDeleted: Boolean,
    ) {
        Timber.e(isCommunityShouldBeDeleted.toString())
        if (isCommunityShouldBeDeleted) {
            deleteCommunity(communityId)
        } else {
            restoreCommunity(communityId)
        }
    }
}
