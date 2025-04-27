package com.numplates.nomera3.modules.upload.ui

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.stringNullable
import com.meera.db.models.UploadType
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.mapper.UploadBundleMapper
import com.numplates.nomera3.modules.upload.ui.model.StatusToastEvent
import com.numplates.nomera3.modules.upload.ui.model.StatusToastState
import com.numplates.nomera3.modules.upload.ui.model.StatusToastUiModel
import com.numplates.nomera3.modules.upload.ui.view.UploadStatusToast
import com.numplates.nomera3.modules.upload.ui.viewmodel.UploadStatusViewModel

class MeeraStatusToastViewController(
    private var act: MeeraAct?,
    private var statusToast: UploadStatusToast?,
    private var uploadStatusViewModel: UploadStatusViewModel
) {
    private var undoSnackbar: UiKitSnackBar? = null
    private var onRepeatAction: ((type: UploadType, postStringEntity: String) -> Unit)? = null

    private val DEFAULT_BOTTOM_PADDING = 16.dp

    init {
        act?.lifecycleScope?.launchWhenStarted {
            uploadStatusViewModel.statusToastEventFlow
                .collect(this@MeeraStatusToastViewController::handleUploadStatusEvent)
        }
        statusToast?.apply {
            actionListener = { action ->
                uploadStatusViewModel.onStatusAction(action)
            }
            onDismiss = {
                uploadStatusViewModel.onToastDismiss()
            }
        }
    }

    fun restoreStatusToast() = uploadStatusViewModel.restoreStatusToast()

    fun hideStatusToast() = uploadStatusViewModel.hideStatusToast()

    fun observeStatusToastActions() = uploadStatusViewModel.statusToastActionFlow

    fun setOnToastControllerRepeatListener(listener: (type: UploadType, postStringEntity: String) -> Unit) {
        onRepeatAction = listener
    }

    fun showSuccess(message: String? = null, imageUrl: String? = null) {
        statusToast?.show(
            StatusToastUiModel(
                state = StatusToastState.Success(message),
                imageUrl = imageUrl,
                canPlayContent = false,
                action = null
            )
        )
        act?.doDelayed(1500) { statusToast?.hide() }
    }

    fun showProgress(message: String? = null, imageUrl: String? = null) {
        statusToast?.show(
            StatusToastUiModel(
                state = StatusToastState.Progress(message),
                imageUrl = imageUrl,
                canPlayContent = false,
                action = null
            )
        )
    }

    private fun handleUploadStatusEvent(event: StatusToastEvent) {
        when (event) {
            is StatusToastEvent.HideToast -> {
                statusToast?.hide()
            }

            is StatusToastEvent.ShowBottomToast -> {
                val avatarUiState = if (event.isError) {
                    AvatarUiState.ErrorIconState
                } else {
                    AvatarUiState.SuccessIconState
                }
                val rootView = act?.findViewById<View>(R.id.meera_root_layout_activity) ?: return
                undoSnackbar = UiKitSnackBar.make(
                    view = rootView,
                    params = SnackBarParams(
                        snackBarViewState = SnackBarContainerUiState(
                            messageText = act.stringNullable(event.message),
                            avatarUiState = avatarUiState,
                        ),
                        duration = BaseTransientBottomBar.LENGTH_SHORT,
                        dismissOnClick = true,
                        paddingState = PaddingState(
                            bottom = countToastBottomPadding()
                        )
                    )
                )
                undoSnackbar?.show()

            }

            is StatusToastEvent.ShowMediaDownloadSuccessToast -> {
                val rootView = act?.findViewById<View>(R.id.meera_root_layout_activity) ?: return
                rootView.post {
                    undoSnackbar = UiKitSnackBar.make(
                        view = rootView,
                        params = SnackBarParams(
                            snackBarViewState = SnackBarContainerUiState(
                                messageText = act.stringNullable(R.string.video_save_to_gallery),
                                avatarUiState = AvatarUiState.SuccessIconState,
                            ),
                            duration = BaseTransientBottomBar.LENGTH_SHORT,
                            dismissOnClick = true,
                            paddingState = PaddingState(
                                bottom = countToastBottomPadding()
                            )
                        )
                    )
                    undoSnackbar?.show()
                }
            }

            is StatusToastEvent.ShowMediaDownloadErrorBottomToast -> {
                val rootView = act?.findViewById<View>(R.id.meera_root_layout_activity) ?: return
                UiKitSnackBar.make(
                    view = rootView,
                    params = SnackBarParams(
                        snackBarViewState = SnackBarContainerUiState(
                            messageText = rootView.context.getText(R.string.media_download_error_description),
                            buttonActionText = rootView.context.getText(R.string.general_retry),
                            buttonActionListener = {
                                uploadStatusViewModel.retryPostMediaDownload(event.postId, event.assetId)
                            },
                            avatarUiState = AvatarUiState.ErrorIconState
                        ),
                        duration = BaseTransientBottomBar.LENGTH_LONG,
                        paddingState = PaddingState(
                            bottom = countToastBottomPadding()
                        )
                    )
                ).show()
            }

            is StatusToastEvent.ShowToast -> {
                statusToast?.show(event.statusToastUiModel)
            }

            is StatusToastEvent.ShowUploadError -> {
                val activity = act ?: return
                val resources = activity.resources
                val titleResId = when (event.uploadItem.type) {
                    UploadType.Post, UploadType.EditPost -> R.string.post_publish_connection_error
                    UploadType.EventPost -> R.string.post_event_publish_connection_error
                    UploadType.Moment -> R.string.moment_publish_connection_error
                }
                val closeResId = when (event.uploadItem.type) {
                    UploadType.EditPost, UploadType.EventPost, UploadType.Post -> R.string.meera_post_event_publish_close
                    UploadType.Moment -> R.string.general_close
                }
                val returnToPostResId = when (event.uploadItem.type) {
                    UploadType.Post, UploadType.EditPost -> R.string.post_publish_back_to_the_post
                    UploadType.EventPost -> R.string.map_events_creation_cancel_negative
                    UploadType.Moment -> R.string.map_events_info_okay
                    else -> null
                }

                MeeraConfirmDialogBuilder()
                    .setHeader(resources.getString(titleResId))
                    .setDescription(resources.getString(R.string.post_publish_error))
                    .setTopBtnText(returnToPostResId?.let(resources::getString).orEmpty())
                    .setTopBtnType(ButtonType.FILLED)
                    .setTopClickListener {
                        when (event.uploadItem.type) {
                            UploadType.Post, UploadType.EditPost -> {
                                onRepeatAction?.invoke(event.uploadItem.type, event.uploadItem.uploadBundleStringify)
                            }

                            UploadType.EventPost -> {
                                onRepeatAction?.invoke(event.uploadItem.type, event.uploadItem.uploadBundleStringify)
                            }

                            else -> Unit
                        }

                    }
                    .setBottomClickListener {
                        when (event.uploadItem.type) {
                            UploadType.EditPost -> {
                                val uploadPostEntity = UploadBundleMapper.map(
                                    UploadType.Post,
                                    event.uploadItem.uploadBundleStringify
                                ) as? UploadPostBundle ?: return@setBottomClickListener
                                uploadStatusViewModel.abortEditingPost(
                                    uploadPostEntity.postId ?: return@setBottomClickListener
                                )
                            }

                            else -> Unit
                        }
                    }
                    .setBottomBtnText(resources.getString(closeResId))
                    .setCancelable(false)
                    .show(activity.supportFragmentManager)
            }
        }
    }

    private fun countToastBottomPadding(): Int {
        val bottomNavigationBarHeight =
            NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().height
        val systemNavBarHeight = act.getNavigationBarHeight()
        return bottomNavigationBarHeight + systemNavBarHeight + DEFAULT_BOTTOM_PADDING
    }

    fun initAdditionalMargin(padding: Int) {
        statusToast?.setAdditionalBottomMargin(padding)
    }

    fun release() {
        act = null
        statusToast = null
        onRepeatAction = null
    }
}
