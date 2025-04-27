package com.meera.media_controller_implementation.presentation

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.parcelable
import com.meera.core.extensions.parcelableArrayList
import com.meera.core.extensions.register
import com.meera.core.utils.MeeraNotificationController
import com.meera.core.utils.imagecompressor.Compressor
import com.meera.core.utils.imagecompressor.constraint.editorResize
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_implementation.MediaControllerFeatureImplementation
import com.meera.media_controller_implementation.R
import com.noomeera.nmrmediasdk.NMRMediaSDK
import com.noomeera.nmrmediasdk.ui.NMR_MEDIA_KEY
import com.noomeera.nmrmediasdk.ui.REQUEST_NMR_MEDIA_RESULT
import com.noomeera.nmrmediastories.ENABLE_DEEPAR
import com.noomeera.nmrmediastories.KEY_URIS
import com.noomeera.nmrmediastories.NMRStoriesSDK
import com.noomeera.nmrmediastories.REQUEST_NMR_KEY_URIS
import com.noomeera.nmrmediastories.ui.MeeraRootFragment
import com.noomeera.nmrmediatools.NMRResult
import com.noomeera.nmrmediatools.NMRStoryGifObject
import com.noomeera.nmrmediatools.NMRStoryImageObject
import com.noomeera.nmrmediatools.NMRStoryStickerObject
import com.noomeera.nmrmediatools.NMRStoryWidgetObject
import com.noomeera.nmrmediatools.utils.CropMode
import com.noomeera.nmrmediatools.utils.NMRMediaSettings
import com.noomeera.nmrmediatools.utils.Ratio
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

internal const val ACTIVITY_WRAPPER_URI_KEY = "ACTIVITY_WRAPPER_URI_KEY"
internal const val ACTIVITY_WRAPPER_MEDIA_PLACE_KEY = "ACTIVITY_WRAPPER_MEDIA_PLACE_KEY"
internal const val ACTIVITY_WRAPPER_NMR_RESULT_KEY = "ACTIVITY_WRAPPER_NMR_RESULT_KEY"
internal const val MOMENTS_WRAPPER_NMR_RESULT_KEY = "MOMENTS_WRAPPER_NMR_RESULT_KEY"
internal const val ACTIVITY_OPEN_STICKERS_KEY = "ACTIVITY_OPEN_STICKERS_KEY"

const val BROADCAST_MEDIA_ACTION = "ru.noomera.stories.MEDIA_ACTION"
const val BROADCAST_MUSIC_EXTRA_JSON = "ru.noomera.stories.MUSIC_EXTRA_JSON"
const val BROADCAST_WIDGET_EXTRA = "ru.noomera.stories.WIDGET_EXTRA"
const val BROADCAST_GIF_EXTRA = "ru.noomera.stories.GIF_EXTRA"
const val BROADCAST_IMAGE_EXTRA = "ru.noomera.stories.IMAGE_EXTRA"
const val BROADCAST_STICKER_EXTRA = "ru.noomera.stories.STICKER_EXTRA"
const val BROADCAST_MEDIA_DATA = "ru.noomera.stories.MEDIA_DATA"

private const val MAX_PIXELS_VIDEO_SIZE = 1920

internal class MediaControllerWrapperActivity : AppCompatActivity() {

    private val component = MediaControllerFeatureImplementation.getComponent()
    private val viewModel by viewModels<MediaControllerWrapperViewModel> { component.getViewModelFactory() }
    private val momentBroadcast = MomentBroadcast()

    private var dialogToDismissOnActivityFinish: ProgressDialog? = null

    private val openStickers: Boolean
        get() = intent?.getBooleanExtra(ACTIVITY_OPEN_STICKERS_KEY, false) ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_editor_wrapper)
        setStatusBar()
        getEditorEvent()

        momentBroadcast.register(
            context = this,
            filter = IntentFilter(BROADCAST_MEDIA_ACTION)
        )
    }

    private fun getEditorEvent() {
        val uri = intent.parcelable<Uri>(ACTIVITY_WRAPPER_URI_KEY)
        val openPlace = intent.parcelable<MediaControllerOpenPlace>(
            ACTIVITY_WRAPPER_MEDIA_PLACE_KEY
        ) ?: error("Неверный тип аргумента. mediaPlace")
        val openEditorEvent = viewModel.getOpenEditorEvent(
            uri = uri,
            openPlace = openPlace
        ) ?: return
        onEvent(openEditorEvent)
    }

    override fun onStart() {
        super.onStart()
        MeeraNotificationController.shouldShowNotification.set(false)
    }

    override fun onStop() {
        super.onStop()
        MeeraNotificationController.shouldShowNotification.set(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogToDismissOnActivityFinish?.dismiss()
        unregisterReceiver(momentBroadcast)
    }

    private fun onEvent(event: MediaControllerWrapperEvent) {
        when (event) {
            is MediaControllerWrapperEvent.OpenExternalEditor -> {
                if (event.forceResize && event.mediaType == NMRMediaSDK.MediaType.VIDEO) {
                    prepareVideo(event)
                } else if (event.mediaType == NMRMediaSDK.MediaType.IMAGE) {
                    prepareImage(event)
                } else {
                    openExternalEditor(
                        event.uri,
                        event.mediaType,
                        event.ratios,
                        event.maxDuration,
                        event.cropMode,
                        event.mediaSettings,
                        false
                    )
                }
            }

            is MediaControllerWrapperEvent.OpenMomentsExternalEditor -> {
                openMomentsEditor(true)
            }
        }
    }

    private fun prepareVideo(event: MediaControllerWrapperEvent.OpenExternalEditor) {
        val dialog = ProgressDialog(this).apply {
            setProgressNumberFormat(null)
            isIndeterminate = false
            progress = 0
            max = 100
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setMessage("Preparing video")
            setCancelable(false)
            show()
        }.apply {
            dialogToDismissOnActivityFinish = this
        }

        val uri = event.uri ?: return

        NMRMediaSDK.resizeVideo(
            context = this,
            sourceUri = uri,
            maxPixels = MAX_PIXELS_VIDEO_SIZE,
            progress = { progress: Int -> dialog.progress = progress },
            callback = { outputFile ->
                if (this.isDestroyed || this.isFinishing) return@resizeVideo

                dialog.dismiss()
                if (outputFile == null) {
                    finish()
                    return@resizeVideo
                }
                openExternalEditor(
                    outputFile,
                    event.mediaType,
                    event.ratios,
                    event.maxDuration,
                    event.cropMode,
                    event.mediaSettings,
                    true
                )
            }
        )
    }

    private fun prepareImage(event: MediaControllerWrapperEvent.OpenExternalEditor) {
        val dialog = ProgressDialog(this).apply {
            setProgressNumberFormat(null)
            setProgressPercentFormat(null)
            isIndeterminate = true
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setMessage("Preparing image")
            setCancelable(false)
            show()
        }.apply {
            dialogToDismissOnActivityFinish = this
        }

        val uri = event.uri?.path ?: return

        lifecycleScope.launch {
            val compressed = Compressor.compress(
                context = applicationContext,
                imageFile = File(uri),
                compressionPatch = {
                    editorResize()
                }
            )

            dialog.dismiss()
            openExternalEditor(
                Uri.fromFile(compressed),
                event.mediaType,
                event.ratios,
                event.maxDuration,
                event.cropMode,
                event.mediaSettings,
                true
            )
        }
    }

    private fun openExternalEditor(
        targetMedia: Uri?,
        mediaType: NMRMediaSDK.MediaType,
        supportedRatios: List<Ratio>,
        maxDurationMs: Long,
        cropMode: CropMode,
        mediaSettings: NMRMediaSettings,
        withResize: Boolean
    ) {
        if (targetMedia == null) return
        val videoUriWithScheme = runCatching {
            if (targetMedia.scheme == null) {
                Uri.fromFile(File(targetMedia.path))
            } else {
                targetMedia
            }
        }.getOrElse {
            Timber.e(it)
            finish()
            return
        }

        val isVideo = mediaType == NMRMediaSDK.MediaType.VIDEO

        val fragment: Fragment = if (isVideo) {
            NMRMediaSDK.createPhotoVideoEditorFragment(
                targetMedia = videoUriWithScheme,
                mediaType = mediaType,
                supportedRatios = supportedRatios,
                cropMode = cropMode,
                videoMaxDuration = maxDurationMs,
                mediaSettings = mediaSettings,
                frontCamera = false,
                openStickers = openStickers
            )
        } else {
            NMRMediaSDK.createPhotoVideoEditorFragment(
                targetMedia = videoUriWithScheme,
                mediaType = mediaType,
                supportedRatios = supportedRatios,
                cropMode = cropMode,
                mediaSettings = mediaSettings,
                frontCamera = false,
                openStickers = openStickers
            )
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.video_wrapper_container, fragment)
            addToBackStack(null)
            commit()
        }

        fragment.setFragmentResultListener(REQUEST_NMR_MEDIA_RESULT) { _, bundle ->
            val nmrResult: NMRResult = bundle.getParcelable(NMR_MEDIA_KEY) ?: let {
                if (isVideo && withResize) cleanUp(targetMedia.path)
                onCancelled()
                return@setFragmentResultListener
            }

            if (isVideo && withResize) cleanUp(targetMedia.path)
            val returnIntent = Intent()
            returnIntent.putExtra(ACTIVITY_WRAPPER_NMR_RESULT_KEY, nmrResult)
            setResult(Activity.RESULT_OK, returnIntent)

            finish()
        }
    }

    private fun openMomentsEditor(useDeepAr: Boolean) {
        NMRStoriesSDK.onNavigateToSettings = {
            component
                .getActivityNavigator()
                .navigateToMomentsSettings(this)
        }
        NMRStoriesSDK.onOpenMusicPlayer = { isAdding ->
            component
                .getActivityNavigator()
                .navigateToMusicPlayer(context = this, isAdding = isAdding)
        }
        NMRStoriesSDK.onOpenStickers = { isAddingMusic ->
            component
                .getActivityNavigator()
                .navigateToMediaKeyboard(this, isAddingMusic)
        }


        val momentsFragment = MeeraRootFragment().apply {
            arguments = bundleOf(ENABLE_DEEPAR to useDeepAr)
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.video_wrapper_container, momentsFragment)
            commit()
        }

        momentsFragment.setFragmentResultListener(REQUEST_NMR_KEY_URIS) { _, bundle ->
            val results = bundle.parcelableArrayList<NMRResult>(KEY_URIS) ?: run {
                onCancelled()
                return@setFragmentResultListener
            }
            val mediaResults = viewModel.getMediaResults(results)

            val returnIntent = Intent()
            returnIntent.putParcelableArrayListExtra(MOMENTS_WRAPPER_NMR_RESULT_KEY, mediaResults)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    private fun cleanUp(originalVideoPath: String?) {
        viewModel.deleteFile(originalVideoPath)
    }

    private fun onCancelled() {
        finish()
    }

    private fun setStatusBar() {
        window.navigationBarColor = ContextCompat.getColor(baseContext, NAVBAR_COLOR)
        window.statusBarColor = ContextCompat.getColor(baseContext, STATUS_BAR_COLOR)
    }

    companion object {
        private val NAVBAR_COLOR = R.color.black_85
        private val STATUS_BAR_COLOR = R.color.black_85
    }
}

class MomentBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        when {
            intent.action != BROADCAST_MEDIA_ACTION -> return
            intent.hasExtra(BROADCAST_MUSIC_EXTRA_JSON) -> {
                intent.getStringExtra(BROADCAST_MUSIC_EXTRA_JSON)?.let {
                    NMRStoriesSDK.onSelectCurrentTrack.invoke(it)
                }
            }

            intent.hasExtra(BROADCAST_WIDGET_EXTRA) -> {
                NMRStoriesSDK.onSelectStoryMediaObject(NMRStoryWidgetObject)
            }

            intent.hasExtra(BROADCAST_GIF_EXTRA) -> {
                intent.getBundleExtra(BROADCAST_GIF_EXTRA)
                    ?.parcelable<NMRStoryGifObject>(BROADCAST_MEDIA_DATA)
                    ?.let {
                        NMRStoriesSDK.onSelectStoryMediaObject(it)
                    }
            }

            intent.hasExtra(BROADCAST_IMAGE_EXTRA) -> {
                intent.getBundleExtra(BROADCAST_IMAGE_EXTRA)
                    ?.parcelable<Uri>(BROADCAST_MEDIA_DATA)
                    ?.let {
                        NMRStoriesSDK.onSelectStoryMediaObject(NMRStoryImageObject(it))
                    }
            }

            intent.hasExtra(BROADCAST_STICKER_EXTRA) -> {
                intent.getBundleExtra(BROADCAST_STICKER_EXTRA)
                    ?.parcelable<NMRStoryStickerObject>(BROADCAST_MEDIA_DATA)
                    ?.let {
                        NMRStoriesSDK.onSelectStoryMediaObject(it)
                    }
            }

            else -> Unit
        }
    }
}
