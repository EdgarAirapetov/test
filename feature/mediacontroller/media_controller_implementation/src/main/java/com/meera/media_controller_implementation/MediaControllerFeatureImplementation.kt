package com.meera.media_controller_implementation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.parcelableArrayList
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_common.MediaEditorResult
import com.meera.media_controller_implementation.di.MediaControllerInternalComponent
import com.meera.media_controller_implementation.domain.analytic.MediaControllerAmplitudePropertyVideoAlertActionType
import com.meera.media_controller_implementation.presentation.ACTIVITY_OPEN_STICKERS_KEY
import com.meera.media_controller_implementation.presentation.ACTIVITY_WRAPPER_MEDIA_PLACE_KEY
import com.meera.media_controller_implementation.presentation.ACTIVITY_WRAPPER_NMR_RESULT_KEY
import com.meera.media_controller_implementation.presentation.ACTIVITY_WRAPPER_URI_KEY
import com.meera.media_controller_implementation.presentation.MOMENTS_WRAPPER_NMR_RESULT_KEY
import com.meera.media_controller_implementation.presentation.MediaControllerWrapperActivity
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRResult
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import java.util.concurrent.TimeUnit

internal class MediaControllerFeatureImplementation(
    private val rootActivity: AppCompatActivity,
    private val component: MediaControllerInternalComponent
) : MediaControllerFeatureApi {

    init {
        internalComponent = component
    }

    private val openMediaEditorResultLauncher =
        rootActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    handleMediaResult(data)
                    handleMomentsMediaResult(data)
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                mediaEditorCallback?.onCanceled()
            }

            mediaEditorCallback = null
        }

    private var mediaEditorCallback: MediaControllerCallback? = null

    override fun open(
        uri: Uri?,
        openPlace: MediaControllerOpenPlace,
        callback: MediaControllerCallback,
        openStickers: Boolean
    ) {
        if (rootActivity.isFinishing || rootActivity.isDestroyed) return
        mediaEditorCallback = callback

        val intent = Intent(rootActivity, MediaControllerWrapperActivity::class.java)
        intent.putExtra(ACTIVITY_WRAPPER_URI_KEY, uri)
        intent.putExtra(ACTIVITY_WRAPPER_MEDIA_PLACE_KEY, openPlace)
        intent.putExtra(ACTIVITY_OPEN_STICKERS_KEY, openStickers)
        openMediaEditorResultLauncher.launch(intent)
    }

    override fun showVideoTooLongDialog(
        openPlace: MediaControllerOpenPlace,
        needEditResponse: MediaControllerNeedEditResponse.VideoTooLong,
        showInMinutes: Boolean,
        openEditorCallback: () -> Unit
    ) {
        val title = if (showInMinutes) {
            rootActivity.getString(R.string.max_video_duration_min, getMaxVideoLengthMin(openPlace))
        } else {
            rootActivity.getString(R.string.max_video_duration_sec, getMaxVideoLengthSec(openPlace))
        }

        showVideoTooLongDialog(
            currentVideoDurationSec = needEditResponse.currentDurationSec,
            maxVideoDurationSec = needEditResponse.maxDurationSec,
            fragmentManager = rootActivity.supportFragmentManager,
            openEditorCallback = openEditorCallback,
            title = title
        )
    }

    override fun needEditMedia(uri: Uri?, openPlace: MediaControllerOpenPlace): MediaControllerNeedEditResponse {
        if (uri == null) error("media uri shouldn't be null")

        return isNeedEditMedia(uri, openPlace)
    }

    private fun handleMediaResult(data: Intent) {
        if (!data.hasExtra(ACTIVITY_WRAPPER_NMR_RESULT_KEY)) return
        val mediaEditorResult =
            data.getParcelableExtra(ACTIVITY_WRAPPER_NMR_RESULT_KEY) as? NMRResult ?: return

        when {
            mediaEditorResult.isVideo -> {
                mediaEditorCallback?.onVideoReady(
                    mediaEditorResult.uri,
                    mediaEditorResult.nmrAmplitude as? NMRVideoAmplitude
                )
            }

            else -> {
                mediaEditorCallback?.onPhotoReady(
                    mediaEditorResult.uri,
                    mediaEditorResult.nmrAmplitude as? NMRPhotoAmplitude
                )
            }
        }
    }

    private fun handleMomentsMediaResult(data: Intent) {
        if (!data.hasExtra(MOMENTS_WRAPPER_NMR_RESULT_KEY)) return
        val results = data.parcelableArrayList<MediaEditorResult>(MOMENTS_WRAPPER_NMR_RESULT_KEY) ?: return

        mediaEditorCallback?.onMediaListReady(results)
    }

    private fun showVideoTooLongDialog(
        currentVideoDurationSec: Int,
        maxVideoDurationSec: Int,
        fragmentManager: FragmentManager,
        openEditorCallback: () -> Unit,
        title: String
    ) {
        ConfirmDialogBuilder()
            .setHeader(title)
            .setDescription(rootActivity.getString(R.string.video_could_be_cut_in_editor))
            .setLeftBtnText(rootActivity.getString(R.string.cancel_caps))
            .setRightBtnText(rootActivity.getString(R.string.to_editor))
            .setCancelable(false)
            .setRightClickListener {
                logVideoAlertAction(
                    MediaControllerAmplitudePropertyVideoAlertActionType.EDITOR,
                    currentVideoDurationSec,
                    maxVideoDurationSec
                )
                openEditorCallback.invoke()
            }
            .setLeftClickListener {
                logVideoAlertAction(
                    MediaControllerAmplitudePropertyVideoAlertActionType.CANCEL,
                    currentVideoDurationSec,
                    maxVideoDurationSec
                )
            }
            .show(fragmentManager)
    }

    private fun isNeedEditMedia(uri: Uri, openPlace: MediaControllerOpenPlace): MediaControllerNeedEditResponse =
        component.getMediaControllerNewPostNeedEditUtil().needToEditMedia(uri, openPlace)

    private fun getMaxVideoLengthMin(openPlace: MediaControllerOpenPlace): Int {
        val maxVideoLengthSec =
            component.getMediaControllerNewPostNeedEditUtil().getMaxVideoLengthSec(openPlace).toLong()
        return TimeUnit.SECONDS.toMinutes(maxVideoLengthSec).toInt()
    }

    private fun getMaxVideoLengthSec(openPlace: MediaControllerOpenPlace): Int {
        return component.getMediaControllerNewPostNeedEditUtil().getMaxVideoLengthSec(openPlace)
    }

    private fun logVideoAlertAction(
        actionType: MediaControllerAmplitudePropertyVideoAlertActionType,
        videoDurationSec: Int,
        maxVideoDurationSec: Int
    ) = component.getMediaControllerAmplitude().logVideoAlertAction(
        actionType = actionType,
        videoDurationSec = videoDurationSec,
        maxVideoDurationSec = maxVideoDurationSec
    )

    companion object {
        private var internalComponent: MediaControllerInternalComponent? = null

        fun isCreated(): Boolean {
            return internalComponent != null
        }

        fun getComponent(): MediaControllerInternalComponent {
            return internalComponent ?: error("component should be initialized")
        }
    }
}
