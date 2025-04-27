package com.meera.core.base

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.meera.core.base.enums.PermissionState
import com.meera.core.bottomsheets.SuggestionsMenuContract
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.mediaviewer.MediaViewerPhotoEditorCallback
import com.meera.core.utils.mediaviewer.MediaViewerViewCallback
import com.meera.core.utils.tedbottompicker.TedBottomPicker
import com.meera.core.utils.tedbottompicker.TedBottomSheetCallback
import com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.utils.camera.CameraLensFacing
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment.OnImageSelectedListener
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.core.utils.tedbottompicker.models.MediaViewerPreviewModeParams

interface BaseLoadImages {

    fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        needWithVideo: Boolean,
        videoMaxDuration: Int? = null,
        showGifs: Boolean,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        previewModeParams: MediaViewerPreviewModeParams = MediaViewerPreviewModeParams(),
        selectedEditedMedia: List<MediaUriModel>? = null,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
    ): TedBottomSheetDialogFragment

    fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        needWithVideo: Boolean,
        videoMaxDuration: Int? = null,
        showGifs: Boolean,
        suggestionsMenu: SuggestionsMenuContract,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
    )

    /**
     * With camera type settings (Orientation e.t.c)
     */
    fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        cameraType: MediaViewerCameraTypeEnum,
        suggestionsMenu: SuggestionsMenuContract,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
    )

    fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        cameraType: MediaViewerCameraTypeEnum,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
    ): TedBottomSheetDialogFragment

    fun loadMultiImage(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        maxCount: Int,
        type: MediaControllerOpenPlace,
        message: String,
        suggestionsMenu: SuggestionsMenuContract,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
    )

    fun loadMultiImage(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        maxCount: Int,
        type: MediaControllerOpenPlace,
        message: String,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
    ): TedBottomSheetDialogFragment

    fun loadMultiMediaUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        needWithVideo: Boolean,
        videoMaxDuration: Int? = null,
        showGifs: Boolean,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        previewModeParams: MediaViewerPreviewModeParams = MediaViewerPreviewModeParams(),
        selectedEditedMedia: List<MediaUriModel>,
        loadImagesCommonCallback: BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
        maxCount: Int
    ): TedBottomSheetDialogFragment

    interface BaseLoadImagesInteractionCallback {

        fun onSetDialogColorStatusBar()

        fun onSetStatusBar()

        fun onOpenPhotoEditor(
            imageUrl: Uri,
            type: MediaControllerOpenPlace,
            supportGifEditing: Boolean,
            resultCallback: MediaViewerPhotoEditorCallback.MediaViewerPhotoEditorResultCallback
        )

        fun onAddHashSetVideoToDelete(path: String)

        fun onClickDotsMenu(result: MediaViewerViewCallback.MediaViewerViewDotsClickResult)

        fun onDeniedPermission() {}

        fun onRequestMediaReset() {}

        fun onImageReadyUri(uri: Uri) {}

        fun onImageRemoved() {}

        /**
         * Для загрузки одного изображения
         */
        fun onImageReady(imagePath: String) {}

        /**
         * Колбек срабатывает, если в билдере нет метода setOnImageReadyWithText
         */
        fun onImagesUriReady(images: List<Uri>) {}

        fun onMultiMediaUrisReady(images: List<MediaUriModel>) {}
        /**
         * Колбек срабатывает, если в билдере есть метод setOnImageReadyWithText
         */
        fun onImagesUriReadyWithText(images: List<Uri>, text: String) {}

        fun onProgress() {}

        fun onDismiss() {}
    }
}

class BaseLoadImagesDelegate : BaseLoadImages {

    override fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        needWithVideo: Boolean,
        videoMaxDuration: Int?,
        showGifs: Boolean,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        previewModeParams: MediaViewerPreviewModeParams,
        selectedEditedMedia: List<MediaUriModel>?,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
    ): TedBottomSheetDialogFragment {
        val ted: TedBottomPicker.Builder = TedBottomPicker
            .with(activity as FragmentActivity)
            .showTitle(false)
            .showGalleryTile(false)
            .setWithPreview(type)
            .setPreviewMaxCount(Int.MAX_VALUE)
            .setVideoMaxDuration(videoMaxDuration)
            .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
            .setSupportFragmentManager(activity.supportFragmentManager)
            .setSuggestionMenu(suggestionsMenu)
            .setPermissionState(permissionState)
            .setPermissionsActionsListener(tedBottomSheetPermissionActionsListener)
            .setPreviewModeParams(previewModeParams)
            .setAlreadySelectedMedia(selectedEditedMedia)
            .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
            .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
            .setDialogDismissListener { loadImagesCommonCallback.onDismiss() }
            .showGifs(showGifs)
            .setCameraLensFacing(cameraLensFacing)
        if (needWithVideo) ted.showImageAndVideoMedia()
        return ted.show(object : OnImageSelectedListener {
            override fun onImageSelected(uri: Uri) {
                loadImagesCommonCallback.onImageReadyUri(uri)
            }

            override fun onImageUnselected() {
                loadImagesCommonCallback.onImageRemoved()
            }

            override fun onRequestMediaReset() {
                loadImagesCommonCallback.onRequestMediaReset()
            }
        })
    }

    override fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        needWithVideo: Boolean,
        videoMaxDuration: Int?,
        showGifs: Boolean,
        suggestionsMenu: SuggestionsMenuContract,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback
    ) {
        PermissionDelegate(
            activity = activity,
            viewLifecycleOwner = viewLifecycleOwner
        ).setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    val ted: TedBottomPicker.Builder = TedBottomPicker
                        .with(activity as FragmentActivity)
                        .showTitle(false)
                        .showGalleryTile(false)
                        .setWithPreview(type)
                        .setPreviewMaxCount(Int.MAX_VALUE)
                        .setVideoMaxDuration(videoMaxDuration)
                        .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
                        .setSupportFragmentManager(activity.supportFragmentManager)
                        .setSuggestionMenu(suggestionsMenu)
                        .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
                        .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
                        .setDialogDismissListener { loadImagesCommonCallback.onDismiss() }
                        .showGifs(showGifs)
                    if (needWithVideo) ted.showImageAndVideoMedia()
                    ted.show(object : OnImageSelectedListener {
                        override fun onImageSelected(uri: Uri) {
                            loadImagesCommonCallback.onImageReadyUri(uri)
                        }

                        override fun onImageUnselected() {
                            loadImagesCommonCallback.onImageRemoved()
                        }

                        override fun onRequestMediaReset() {
                            loadImagesCommonCallback.onRequestMediaReset()
                        }
                    })
                }

                override fun onDenied() {
                    loadImagesCommonCallback.onDeniedPermission()
                }

                override fun onError(error: Throwable?) {
                    Log.e("BaseLoadImages", "Error get permission when multi load images:$error")
                }

            },
            Manifest.permission.CAMERA,
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    override fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        cameraType: MediaViewerCameraTypeEnum,
        suggestionsMenu: SuggestionsMenuContract,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback
    ) {
        PermissionDelegate(
            activity = activity,
            viewLifecycleOwner = viewLifecycleOwner
        ).setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    val ted: TedBottomPicker.Builder = TedBottomPicker
                        .with(activity as FragmentActivity)
                        .showTitle(false)
                        .showGalleryTile(false)
                        .setWithPreview(type)
                        .setPreviewMaxCount(Int.MAX_VALUE)
                        .setCameraTypePreview(cameraType)
                        .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
                        .setSupportFragmentManager(activity.supportFragmentManager)
                        .setSuggestionMenu(suggestionsMenu)
                        .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
                        .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
                        .setDialogDismissListener { loadImagesCommonCallback.onDismiss() }
                    ted.show(object : OnImageSelectedListener {
                        override fun onImageSelected(uri: Uri) {
                            loadImagesCommonCallback.onImageReadyUri(uri)
                        }

                        override fun onImageUnselected() {
                            loadImagesCommonCallback.onImageRemoved()
                        }

                        override fun onRequestMediaReset() {
                            loadImagesCommonCallback.onRequestMediaReset()
                        }
                    })
                }

                override fun onDenied() {
                    loadImagesCommonCallback.onDeniedPermission()
                }

                override fun onError(error: Throwable?) {
                    Log.e("BaseLoadImages", "Error get permission when multi load images:$error")
                }

            },
            Manifest.permission.CAMERA,
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )

    }

    override fun loadSingleImageUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        cameraType: MediaViewerCameraTypeEnum,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
    ): TedBottomSheetDialogFragment {
        val ted: TedBottomPicker.Builder = TedBottomPicker
            .with(activity as FragmentActivity)
            .showTitle(false)
            .showGalleryTile(false)
            .setWithPreview(type)
            .setPreviewMaxCount(Int.MAX_VALUE)
            .setCameraTypePreview(cameraType)
            .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
            .setSupportFragmentManager(activity.supportFragmentManager)
            .setSuggestionMenu(suggestionsMenu)
            .setPermissionState(permissionState)
            .setPermissionsActionsListener(tedBottomSheetPermissionActionsListener)
            .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
            .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
            .setDialogDismissListener { loadImagesCommonCallback.onDismiss() }
            .setCameraLensFacing(cameraLensFacing)

        return ted.show(object : OnImageSelectedListener {
            override fun onImageSelected(uri: Uri) {
                loadImagesCommonCallback.onImageReadyUri(uri)
            }

            override fun onImageUnselected() {
                loadImagesCommonCallback.onImageRemoved()
            }

            override fun onRequestMediaReset() {
                loadImagesCommonCallback.onRequestMediaReset()
            }
        })
    }

    override fun loadMultiImage(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        maxCount: Int,
        type: MediaControllerOpenPlace,
        message: String,
        suggestionsMenu: SuggestionsMenuContract,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback,
    ) {
        PermissionDelegate(
            activity = activity,
            viewLifecycleOwner = viewLifecycleOwner
        ).setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    val ted: TedBottomPicker.Builder = TedBottomPicker
                        .with(activity as FragmentActivity)
                        .showGalleryTile(false)
                        .showTitle(false)
                        .setWithPreview(type)
                        .setSelectMaxCount(maxCount)
                        .setPreviewMaxCount(Integer.MAX_VALUE)
                        .setMessage(message)
                        .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
                        .setSupportFragmentManager(activity.supportFragmentManager)
                        .setSuggestionMenu(suggestionsMenu)
                        .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
                        .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
                        .setDialogDismissListener(loadImagesCommonCallback::onDismiss)
                        .setOnImageReadyWithText(loadImagesCommonCallback::onImagesUriReadyWithText)
                    if (type == MediaControllerOpenPlace.Chat) ted.showImageAndVideoMedia()
                    ted.showMultiImage(loadImagesCommonCallback::onImagesUriReady)
                }

                override fun onDenied() {
                    loadImagesCommonCallback.onDeniedPermission()
                }

                override fun onError(error: Throwable?) {
                    Log.e("BaseLoadImages", "Error get permission when multi load images:$error")
                }
            },
            Manifest.permission.CAMERA,
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    override fun loadMultiImage(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        maxCount: Int,
        type: MediaControllerOpenPlace,
        message: String,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
    ): TedBottomSheetDialogFragment {
        val ted: TedBottomPicker.Builder = TedBottomPicker
            .with(activity as FragmentActivity)
            .showGalleryTile(false)
            .showTitle(false)
            .setWithPreview(type)
            .setSelectMaxCount(maxCount)
            .setPreviewMaxCount(Integer.MAX_VALUE)
            .setMessage(message)
            .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
            .setSupportFragmentManager(activity.supportFragmentManager)
            .setSuggestionMenu(suggestionsMenu)
            .setPermissionState(permissionState)
            .setPermissionsActionsListener(tedBottomSheetPermissionActionsListener)
            .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
            .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
            .setDialogDismissListener(loadImagesCommonCallback::onDismiss)
            .setOnImageReadyWithText(loadImagesCommonCallback::onImagesUriReadyWithText)
            .setCameraLensFacing(cameraLensFacing)
        if (type == MediaControllerOpenPlace.Chat) ted.showImageAndVideoMedia()
        return ted.showMultiImage(loadImagesCommonCallback::onImagesUriReady)
    }

    override fun loadMultiMediaUri(
        activity: Activity,
        viewLifecycleOwner: LifecycleOwner,
        type: MediaControllerOpenPlace,
        needWithVideo: Boolean,
        videoMaxDuration: Int?,
        showGifs: Boolean,
        suggestionsMenu: SuggestionsMenuContract,
        permissionState: PermissionState,
        tedBottomSheetPermissionActionsListener: TedBottomSheetPermissionActionsListener,
        previewModeParams: MediaViewerPreviewModeParams,
        selectedEditedMedia: List<MediaUriModel>,
        loadImagesCommonCallback: BaseLoadImages.BaseLoadImagesInteractionCallback,
        @CameraLensFacing cameraLensFacing: Int,
        maxCount: Int
    ): TedBottomSheetDialogFragment {
        val ted: TedBottomPicker.Builder = TedBottomPicker
            .with(activity as FragmentActivity)
            .showTitle(false)
            .showGalleryTile(false)
            .setWithPreview(type)
            .setSelectMaxCount(maxCount)
            .setPreviewMaxCount(Int.MAX_VALUE)
            .setVideoMaxDuration(videoMaxDuration)
            .setTedBottomSheetCallback(TedBottomSheetCallbackImpl(loadImagesCommonCallback))
            .setSupportFragmentManager(activity.supportFragmentManager)
            .setSuggestionMenu(suggestionsMenu)
            .setPermissionState(permissionState)
            .setPermissionsActionsListener(tedBottomSheetPermissionActionsListener)
            .setPreviewModeParams(previewModeParams)
            .setAlreadySelectedMedia(selectedEditedMedia)
            .setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallbackImpl(loadImagesCommonCallback))
            .setMediaViewerViewCallback(MediaViewerViewCallbackImpl(loadImagesCommonCallback))
            .setDialogDismissListener { loadImagesCommonCallback.onDismiss() }
            .showGifs(showGifs)
            .setCameraLensFacing(cameraLensFacing)
        if (needWithVideo) ted.showImageAndVideoMedia()
        return ted.showMultiMediaPicker { list -> loadImagesCommonCallback.onMultiMediaUrisReady(list.map { it }) }
    }

    class TedBottomSheetCallbackImpl(
        private val baseLoadImagesCallback: BaseLoadImages.BaseLoadImagesInteractionCallback
    ): TedBottomSheetCallback {

        override fun onSetDialogColorStatusBar() {
            baseLoadImagesCallback.onSetDialogColorStatusBar()
        }

        override fun onSetStatusBar() {
            baseLoadImagesCallback.onSetStatusBar()
        }
    }

    class MediaViewerViewCallbackImpl(
        private val baseLoadImagesCallback: BaseLoadImages.BaseLoadImagesInteractionCallback
    ): MediaViewerViewCallback {
        override fun onClickDotsMenu(result: MediaViewerViewCallback.MediaViewerViewDotsClickResult) {
            baseLoadImagesCallback.onClickDotsMenu(result)
        }
    }

    class MediaViewerPhotoEditorCallbackImpl(
        private val baseLoadImagesCallback: BaseLoadImages.BaseLoadImagesInteractionCallback
    ): MediaViewerPhotoEditorCallback {
        override fun onOpenPhotoEditor(
            imageUrl: Uri,
            type: MediaControllerOpenPlace,
            supportGifEditing: Boolean,
            resultCallback: MediaViewerPhotoEditorCallback.MediaViewerPhotoEditorResultCallback
        ) {
            baseLoadImagesCallback.onOpenPhotoEditor(imageUrl, type, supportGifEditing, resultCallback)
        }

        override fun onAddHashSetVideoToDelete(path: String) {
            baseLoadImagesCallback.onAddHashSetVideoToDelete(path)
        }
    }


}
