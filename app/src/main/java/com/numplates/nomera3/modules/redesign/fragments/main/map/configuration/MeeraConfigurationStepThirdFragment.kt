package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.common.MEDIA_PICKER_HALF_EXTENDED_RATIO
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils
import com.meera.core.utils.imagecapture.ui.getImageFromCamera
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerPreviewModeParams
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import timber.log.Timber
import javax.inject.Inject

class MeeraConfigurationStepThirdFragment : MeeraBaseFragment(R.layout.fragment_configuration_step_third),
    BaseLoadImages by BaseLoadImagesDelegate(),
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    TedBottomSheetPermissionActionsListener {

    @Inject
    lateinit var fileManager: FileManager

    private var currentDialog: AppCompatDialogFragment? = null

    private val act by lazy { activity as MeeraAct }

    private val mapViewModel: MeeraMapViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var mediaPicker: TedBottomSheetDialogFragment? = null

    private val orientationScreenListener =
        object : OrientationScreenListener() {
            override fun onOrientationChanged(orientation: Int) {
                orientationChangedListener.invoke(orientation)
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state = UiKitToolbarViewState.COLLAPSED
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationScreenListener.onOrientationChanged(newConfig.orientation)
    }
//    val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
//        { requestCode, permissions, grantResults ->
//            if (requestCode == PERMISSION_MEDIA_CODE) {
//                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
//                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
//                    initMediaGallery()
//                } else {
//                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
//                    binding?.rvGalleryMedia?.gone()
//                    isPreviewPermitted = false
//                }
//            }
//        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        act.permissionListener.add(listener)
    }

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, _, grantResults ->
            if (isAdded && requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        act.permissionListener.remove(listener)
    }

    fun showMediaPicker() {
        showMediaPicker { imageUri -> handleMediaByPicker(imageUri) }
    }

    private fun handleMediaByPicker(path: Uri, afterEdit: Boolean = false) {
        Timber.e("path $path")
        mapViewModel.checkSelectedEditedMediaUri(path, afterEdit)
    }

    private fun showMediaPicker(onPickImageUri: (imageUri: Uri) -> Unit) {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    showMediaPickerWithPermissionState(PermissionState.GRANTED, onPickImageUri)
                }

                override fun onDenied() {
                    showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED, onPickImageUri)
                }

                override fun needOpenSettings() {
                    showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS, onPickImageUri)
                }
            }
        )
    }

    private fun showMediaPickerWithPermissionState(
        permissionState: PermissionState,
        onPickImageUri: (imageUri: Uri) -> Unit
    ) {
        Timber.e("onPickImageUri $onPickImageUri")
        mediaPicker?.dismissAllowingStateLoss()
        mediaPicker = loadSingleImageUri(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            needWithVideo = false,
            showGifs = false,
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            previewModeParams = MediaViewerPreviewModeParams(
                isPreviewModeEnabled = false,
                halfExtendedHeight = getHalfExpandedMediaPickerHeight(),
                collapsedHeight = getCollapsedMediaPickerHeight()
            ),
            videoMaxDuration = 0,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri ->
                    Timber.e("imageUri $imageUri")
                    mediaAttachmentSelected(imageUri)
                },
                onDismissPicker = {
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
        )
    }

    fun openPhoto(photo: String) {
        val imageList = mutableListOf<ImageViewerData>()
        imageList.add(
            ImageViewerData(
                imageUrl = photo,
                viewType = RecyclingPagerAdapter.VIEW_TYPE_IMAGE
            )
        )

        showMediaViewer(imageList)
    }

    private fun showMediaViewer(data: MutableList<ImageViewerData>) {
        if (data.isNotEmpty()) {
            //Далее собираем билдер
            MediaViewer.with(context)
                .setImageList(data)
                .startPosition(0)
                .setOrientationChangedListener(orientationScreenListener)
                .onSaveImage { saveImage(it, false) }
                .shareAvailable(false)
                .copyAvailable(false)
                .setMeeraAct(act)
                .onDismissListener({})
                .setLifeCycle(lifecycle) // need when video shown
                .show()
        }
    }

    private fun saveImage(imageUrl: String, toCache: Boolean) {
        saveImage(imageUrl, {}, toCache)
    }

    private fun saveImage(imageUrl: String, onSuccess: (Uri) -> Unit, toCache: Boolean) {
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = act,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = onSuccess,
            toCache
        )
    }

    fun mediaAttachmentSelected(uri: Uri, afterEdit: Boolean = false) {
        mapViewModel.handleSelectedEditedMediaUri(afterEdit = afterEdit, uri)
        view?.hideKeyboard()
        Timber.e("openEditor 11")
        openEditor(uri = uri, automaticOpen = true)
    }

    internal fun openEditor(uri: Uri, automaticOpen: Boolean = false) {
        Timber.e("openEditor 211 $automaticOpen")
        act.getMediaControllerFeature().open(
            uri = uri,
            callback = photoEditorCallback,
            openPlace = MediaControllerOpenPlace.EventPost
        )
    }

    private val photoEditorCallback = object : MediaControllerCallback {
        override fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {
            Timber.e("onPhotoReady $resultUri")
            mapViewModel.onImageChosen(resultUri.toString())
        }

        override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) = Unit

        override fun onCanceled() = Unit

        override fun onError() {
            showError(getString(R.string.error_while_working_with_image))
        }
    }

    private fun showError(message: String) {
        NToast.with(view)
            .typeError()
            .text(message)
            .show()
    }

    private fun getHalfExpandedMediaPickerHeight(): Int = (getScreenHeight() * MEDIA_PICKER_HALF_EXTENDED_RATIO).toInt()

    private fun getCollapsedMediaPickerHeight(): Int = 500.dp

    override fun onGalleryRequestPermissions() {
        setMediaPermissions()
    }

    override fun onGalleryOpenSettings() {
        requireContext().openSettingsScreen()
    }

    override fun onCameraRequestPermissions(fromMediaPicker: Boolean) {
        setPermissionsWithSettingsOpening(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    if (fromMediaPicker) {
                        mediaPicker?.openCamera()
                    } else {
                        activity?.getImageFromCamera(object : ImageCaptureUtils.Listener {
                            override fun onResult(result: ImageCaptureResultModel) {
                                mediaAttachmentSelected(result.fileUri)
                            }
                        })
                    }
                }

                override fun needOpenSettings() {
                    onCameraOpenSettings()
                }

                override fun onError(error: Throwable?) {
                    onCameraOpenSettings()
                }
            },
            Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    private fun showCameraSettingsDialog() {
        currentDialog = ConfirmDialogBuilder()
            .setHeader(getString(R.string.camera_settings_dialog_title))
            .setDescription(getString(R.string.camera_settings_dialog_description))
            .setLeftBtnText(getString(R.string.camera_settings_dialog_cancel))
            .setRightBtnText(getString(R.string.camera_settings_dialog_action))
            .setCancelable(false)
            .setRightClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    fun hidePicker() {
        mediaPicker?.dismissAllowingStateLoss()
    }
}
