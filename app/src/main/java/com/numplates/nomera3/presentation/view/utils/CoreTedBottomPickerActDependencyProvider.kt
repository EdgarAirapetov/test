package com.numplates.nomera3.presentation.view.utils

import android.net.Uri
import com.meera.core.base.BaseLoadImages
import com.meera.core.utils.mediaviewer.MediaViewerPhotoEditorCallback
import com.meera.core.utils.mediaviewer.MediaViewerViewCallback
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import timber.log.Timber

class CoreTedBottomPickerActDependencyProvider(
    private val act: Act,
    private val onReadyImagesUri: (images: List<Uri>) -> Unit = {},
    private val onMultiMediaUrisChanges: (images: List<MediaUriModel>) -> Unit = {},
    private val onReadyImagesUriWithText: (images: List<Uri>, text: String) -> Unit = { _, _ -> },
    private val onReadyImageUri: (image: Uri) -> Unit = {},
    private val onImageRemoved: () -> Unit = {},
    private val onReadyImagePath: (imagePath: String) -> Unit = {},
    private val onRequestMediaReset: () -> Unit = {},
    private val onDismissPicker: () -> Unit = {}
): BaseLoadImages.BaseLoadImagesInteractionCallback {

    override fun onSetDialogColorStatusBar() {
        act.setDialogColorStatusBar()
    }

    override fun onSetStatusBar() {
        act.setStatusBar()
    }

    override fun onOpenPhotoEditor(
        imageUrl: Uri,
        type: MediaControllerOpenPlace,
        supportGifEditing: Boolean,
        resultCallback: MediaViewerPhotoEditorCallback.MediaViewerPhotoEditorResultCallback
    ) {
        act.logEditorOpen(
            uri = imageUrl,
            where = AmplitudePropertyWhere.PROFILE
        )
        act.getMediaControllerFeature().open(
            uri = imageUrl,
            openPlace = type,
            callback = object : MediaControllerCallback {
                override fun onPhotoReady(
                    resultUri: Uri,
                    nmrAmplitude: NMRPhotoAmplitude?
                ) {
                    nmrAmplitude?.let {
                        act.logPhotoEdits(
                            nmrAmplitude = nmrAmplitude,
                            where = AmplitudePropertyWhere.PROFILE
                        )
                    }
                    resultCallback.onPhotoReady(resultUri)
                }

                override fun onVideoReady(
                    resultUri: Uri,
                    nmrAmplitude: NMRVideoAmplitude?
                ) {
                    nmrAmplitude?.let {
                        act.logVideoEdits(
                            nmrAmplitude = nmrAmplitude,
                            where = AmplitudePropertyWhere.PROFILE
                        )
                    }
                    resultCallback.onVideoReady(resultUri)
                }

                override fun onError() {
                    resultCallback.onError()
                }

                override fun onCanceled() {
                    resultCallback.onCanceled()
                }
            }
        )
    }

    override fun onAddHashSetVideoToDelete(path: String) {
        val app = act.applicationContext as App
        app.hashSetVideoToDelete.add(path)
    }

    override fun onClickDotsMenu(result: MediaViewerViewCallback.MediaViewerViewDotsClickResult) {
        val menu = MeeraMenuBottomSheet(act)
        menu.addItem(R.string.save_image, R.drawable.image_download_menu_item) {
            result.onClickSaveImage()
        }
        if (result.isShowDeleteMenuItem()) {
            menu.addItem(R.string.delete_photo_txt, R.drawable.delete_red_menu_item) {
                result.onClickDeleteImage()
            }
        }
        menu.show(act.supportFragmentManager)
    }

    override fun onImagesUriReady(images: List<Uri>) {
        onReadyImagesUri(images)
    }

    override fun onMultiMediaUrisReady(images: List<MediaUriModel>) {
        onMultiMediaUrisChanges(images)
    }

    override fun onImagesUriReadyWithText(images: List<Uri>, text: String) {
        onReadyImagesUriWithText(images, text)
    }

    override fun onImageReadyUri(uri: Uri) {
        onReadyImageUri(uri)
    }

    override fun onImageReady(imagePath: String) {
        onReadyImagePath(imagePath)
    }

    override fun onImageRemoved() {
        onImageRemoved.invoke()
    }

    override fun onRequestMediaReset() {
        onRequestMediaReset.invoke()
    }

    override fun onProgress() {
        Timber.d("ON Progress - NOT Implemented")
    }

    override fun onDismiss() {
        onDismissPicker()
    }

    override fun onDeniedPermission() {
        Timber.e("Error: onDenied permission when call TedBottomPicker")
    }
}
