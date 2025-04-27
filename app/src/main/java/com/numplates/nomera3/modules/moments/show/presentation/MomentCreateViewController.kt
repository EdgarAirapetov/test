package com.numplates.nomera3.modules.moments.show.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.pluralString
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_common.MediaEditorResult
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.show.data.MomentGpsPosition
import com.numplates.nomera3.modules.moments.show.data.mapToMoments
import com.numplates.nomera3.modules.moments.show.presentation.dialog.MomentConfirmCallback
import com.numplates.nomera3.modules.moments.show.presentation.dialog.MomentsConfirmDialogBuilder
import com.numplates.nomera3.modules.moments.util.GetLocationUtil
import com.numplates.nomera3.modules.moments.wrapper.IMomentsCallback
import kotlinx.coroutines.launch

class MomentCreateViewController(
    private val activity: AppCompatActivity,
    private val viewModel: MomentCreateViewModel,
    private val mediaController: MediaControllerFeatureApi?
) {

    private var callback: IMomentsCallback? = null

    private val locationUtil = GetLocationUtil(activity)

    init {
        viewModel.eventStream.observe(activity) {
            onMomentsEvent()
        }
    }

    fun open(callback: IMomentsCallback? = null) = activity.lifecycleScope.launch {
        when (val warningEvent = viewModel.getLimitWarning()) {
            is MomentCreateViewModel.LimitWarningType.LimitSoon -> {
                showAlert(
                    title = activity.pluralString(R.plurals.plural_moments, warningEvent.momentsLeft),
                    message = activity.getString(R.string.moments_limit_message),
                    callback = callback
                )
            }

            is MomentCreateViewModel.LimitWarningType.LimitOver -> {
                showAlert(
                    title = activity.getString(R.string.moments_limit_title),
                    message = activity.getString(R.string.moments_limit_message),
                    callback = callback
                )
            }

            null -> {
                openMomentCreate(callback)
            }
        }
    }

    private fun showAlert(title: String, message: String, callback: IMomentsCallback?) {
        MomentsConfirmDialogBuilder()
            .setHeader(title)
            .setDescription(message)
            .setConfirmButtonText(activity.getString(R.string.moments_continue_create))
            .setCancelButtonText(activity.getString(R.string.moments_cancel_create))
            .setMomentConfirmCallback(object : MomentConfirmCallback {

                override fun onConfirmButtonClicked() {
                    openMomentCreate(callback)
                }

                override fun onCancelButtonClicked() = Unit
            })
            .show(activity.supportFragmentManager)
    }

    private fun openMomentCreate(callback: IMomentsCallback? = null) {
        this.callback = callback

        mediaController?.open(
            openPlace = MediaControllerOpenPlace.Moments,
            callback = object : MediaControllerCallback {
                override fun onMediaListReady(results: List<MediaEditorResult>) {
                    activity.lifecycleScope.launch {
                        uploadMoments(results)
                    }
                }
            }
        )
    }

    private suspend fun uploadMoments(momentList: List<MediaEditorResult>) {
        val position = locationUtil.getLocation()
        val momentGps = MomentGpsPosition(position.x, position.y)
        val moments = momentList.mapToMoments(momentGps)
        viewModel.uploadMoments(moments)
        callback?.onReady(moments)
        callback = null
    }

    private fun onMomentsEvent() = Unit
}
