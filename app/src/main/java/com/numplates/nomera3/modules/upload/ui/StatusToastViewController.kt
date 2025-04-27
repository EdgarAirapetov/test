package com.numplates.nomera3.modules.upload.ui

import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.stringNullable
import com.meera.db.models.UploadType
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.ui.util.PostMediaDownloadControllerUtil
import com.numplates.nomera3.modules.feed.ui.util.ShowBottomSnackBarUtil
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.mapper.UploadBundleMapper
import com.numplates.nomera3.modules.upload.ui.model.StatusToastEvent
import com.numplates.nomera3.modules.upload.ui.model.StatusToastState
import com.numplates.nomera3.modules.upload.ui.model.StatusToastUiModel
import com.numplates.nomera3.modules.upload.ui.view.UploadStatusToast
import com.numplates.nomera3.modules.upload.ui.viewmodel.UploadStatusViewModel
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder


// TODO: Убрать после редизайна - https://nomera.atlassian.net/browse/BR-30863
class StatusToastViewController(
    private val act: Act,
    private val statusToast: UploadStatusToast,
    private val uploadStatusViewModel: UploadStatusViewModel
) {
    private val showBottomSnackBarUtil = ShowBottomSnackBarUtil(act, act.findViewById(R.id.root_layout_activity))

    init {
        act.lifecycleScope.launchWhenStarted {
            uploadStatusViewModel.statusToastEventFlow
                .collect(this@StatusToastViewController::handleUploadStatusEvent)
        }
        statusToast.apply {
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

    fun showSuccess(message: String? = null, imageUrl: String? = null) {
        statusToast.show(
            StatusToastUiModel(
                state = StatusToastState.Success(message),
                imageUrl = imageUrl,
                canPlayContent = false,
                action = null
            )
        )
        act.doDelayed(1500) { statusToast.hide() }
    }

    fun showProgress(message: String? = null, imageUrl: String? = null) {
        statusToast.show(
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
                statusToast.hide()
            }
            is StatusToastEvent.ShowBottomToast -> {
                showBottomSnackBarUtil.show(
                    message = act.stringNullable(event.message),
                    isError = event.isError
                )

            }
            is StatusToastEvent.ShowMediaDownloadSuccessToast -> {
                val canShowPostMediaDownloadSuccessToast =
                    PostMediaDownloadControllerUtil.needToShowSuccessDownloadToast(
                        act = act,
                        postMediaDownloadType = event.postMediaDownloadType
                    )
                if (canShowPostMediaDownloadSuccessToast) {
                    showBottomSnackBarUtil.show(
                        message = act.stringNullable(R.string.video_save_to_gallery),
                        isError = false,
                        bottomMargin = 0
                    )
                }
            }
            is StatusToastEvent.ShowMediaDownloadErrorBottomToast -> {
                showBottomSnackBarUtil.showMediaDownloadError {
                    uploadStatusViewModel.retryPostMediaDownload(event.postId, event.assetId)
                }
            }
            is StatusToastEvent.ShowToast -> {
                statusToast.show(event.statusToastUiModel)
            }
            is StatusToastEvent.ShowUploadError -> {
                val resources = act.resources
                val titleResId = when (event.uploadItem.type) {
                    UploadType.Post -> R.string.post_publish_connection_error
                    UploadType.EditPost -> R.string.road_upload_post_error_text
                    UploadType.EventPost -> R.string.post_event_publish_connection_error
                    UploadType.Moment -> R.string.moment_publish_connection_error
                }
                val closeResId = when (event.uploadItem.type) {
                    UploadType.Post -> R.string.post_publish_close
                    UploadType.EditPost -> R.string.post_event_publish_close
                    UploadType.EventPost -> R.string.post_event_publish_close
                    UploadType.Moment -> R.string.post_publish_close
                }
                val returnToPostResId = when (event.uploadItem.type) {
                    UploadType.Post -> R.string.post_publish_back_to_the_post
                    UploadType.EditPost -> R.string.post_publish_back_to_the_post
                    UploadType.EventPost -> R.string.post_event_publish_back_to_the_post
                    else -> null
                }
                ConfirmDialogBuilder()
                    .setHeader(resources.getString(titleResId))
                    .setDescription(resources.getString(R.string.post_publish_error))
                    .setHorizontal(true)
                    .setNeedTopButton(false)
                    .apply {
                        if (returnToPostResId != null) {
                            setMiddleBtnText(resources.getString(returnToPostResId))
                        }
                    }
                    .setBottomBtnText(resources.getString(closeResId))
                    .setMiddleClickListener {
                        when (event.uploadItem.type) {
                            UploadType.Post -> act.addFragment(
                                AddMultipleMediaPostFragment(),
                                Arg(UPLOAD_BUNDLE_KEY, event.uploadItem.uploadBundleStringify)
                            )
                            UploadType.EditPost -> act.addFragment(
                                AddMultipleMediaPostFragment(),
                                Arg(UPLOAD_BUNDLE_KEY, event.uploadItem.uploadBundleStringify)
                            )
                            UploadType.EventPost -> act.addFragment(
                                MapFragment(),
                                Arg(MapFragment.ARG_LOG_MAP_OPEN_WHERE, AmplitudePropertyWhere.OTHER),
                                Arg(UPLOAD_BUNDLE_KEY, event.uploadItem.uploadBundleStringify)
                            )
                            else -> {}
                        }

                    }
                    .setBottomClickListener {
                        when (event.uploadItem.type) {
                            UploadType.EditPost -> {
                                val uploadPostEntity = UploadBundleMapper.map(
                                    UploadType.Post,
                                    event.uploadItem.uploadBundleStringify
                                ) as? UploadPostBundle?: return@setBottomClickListener
                                uploadStatusViewModel.abortEditingPost(uploadPostEntity.postId?: return@setBottomClickListener)
                            }
                            else -> Unit
                        }
                    }
                    .setCancelable(false)
                    .show(act.supportFragmentManager)
            }
        }
    }
}
