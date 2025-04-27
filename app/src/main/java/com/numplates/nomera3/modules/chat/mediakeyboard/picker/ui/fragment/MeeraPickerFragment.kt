@file:Suppress("UNNECESSARY_SAFE_CALL")

package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.isEnabledPermission
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils.getImageFromCamera
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.tedbottompicker.GridSpacingItemDecoration
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMediakeyboardPickerFragmentBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity.Album
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.model.MeeraPickerUiEffect
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel.MeeraPickerViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.BottomSheetSlideOffsetListener
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer.Companion.with
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnImageChangeListener
import com.numplates.nomera3.presentation.view.utils.camera.CameraOrientation
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment.BaseBuilder.MediaType
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.adapter.MeeraGalleryAdapter
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.util.RealPathUtil
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.viewholder.MeeraGalleryViewHolder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File

private const val EXTRA_CAMERA_IMAGE_URI = "camera_image_uri"
private const val EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri"
private const val CAMERA_TILE_POSITION = 0
private const val RECYCLER_VIEW_STATE_KEY = "rv_gallery_state"
const val MEERA_PICKER_KEY_MAP = "MEERA_PICKER_KEY_MAP"

private const val COLLECTION_OFFSET = 1
private const val TARGET_LOGO_POSITION = 0.5f

class MeeraPickerFragment :
    MeeraBaseFragment(layout = R.layout.meera_mediakeyboard_picker_fragment),
    BasePermission by BasePermissionDelegate(), BottomSheetSlideOffsetListener {

    var builder: TedBottomSheetDialogFragment.BaseBuilder<*>? = null

    private val binding by viewBinding(MeeraMediakeyboardPickerFragmentBinding::bind)
    private val viewModel: MeeraPickerViewModel by viewModels { App.component.getViewModelFactory() }
    private val imageGalleryAdapter: MeeraGalleryAdapter by lazy { provideMeeraPickerAdapter() }
    private val isMultiModeEnabled: Boolean
        get() = builder?.onMultiImageSelectedListener != null

    private var currentMediaType: Int = MediaType.IMAGE_AND_VIDEO

    private var tempUriList: List<Uri>? = null
    private var cameraImageUri: Uri? = null
    private var isPermissionGranted: Boolean = false
    private var isFromMap: Boolean = false
    private var bucketId: String? = null

    private val screenListener: OrientationScreenListener = OrientationListener()

    private var pickerExpandStatus: ExpandStatus = ExpandStatus.INVISIBLE
    private var snackBar: UiKitSnackBar? = null

    private val act get() = requireActivity() as? MeeraAct?

    override fun onBottomSheetSlide(slideOffset: Float) {
        runCatching {
            updateLogoPosition(slideOffset)
            setExpandStatus(slideOffset)
        }.onFailure { Timber.e(it) }
    }

    private fun setExpandStatus(slideOffset: Float) {
        pickerExpandStatus = when (slideOffset) {
            1.0f -> ExpandStatus.FULL_EXPANDED
            0.0f -> ExpandStatus.HALF_EXPANDED
            -1.0f -> ExpandStatus.INVISIBLE
            else -> ExpandStatus.INVISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screenListener.onOrientationChanged(newConfig.orientation)
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(AlbumsBottomSheetDialogFragment.KEY_ALBUM) { _, bundle ->
            val album = bundle.getParcelable<Album>(AlbumsBottomSheetDialogFragment.KEY_ALBUM)
            album?.let {
                bucketId = it.id
                binding?.tvRecentFolder?.text = album.name
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkGalleryVisibilityLimit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSavedInstanceState(savedInstanceState)
    }

    private fun setupSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            cameraImageUri = builder?.selectedUri
            tempUriList = builder?.selectedUriList
        } else {
            cameraImageUri = savedInstanceState.getParcelable(EXTRA_CAMERA_IMAGE_URI)
            tempUriList = savedInstanceState.getParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(RECYCLER_VIEW_STATE_KEY, binding.rvGallery.layoutManager?.onSaveInstanceState())
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri)
        outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, tempUriList?.let { ArrayList(it) })
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initView()
        initListener()
        initPermissionView()

        isFromMap = arguments?.getBoolean(MEERA_PICKER_KEY_MAP, false) == true
        if (isFromMap || !isMultiModeEnabled) {
            binding?.vMediaKeyboardDismiss?.isVisible = false
            binding?.vMediaKeyboardAddSelected?.isVisible = false
        }
        savedInstanceState?.let {
            it.getParcelable<Parcelable>(RECYCLER_VIEW_STATE_KEY)
                ?.let { state -> binding.rvGallery.layoutManager?.onRestoreInstanceState(state) }
        }
        act?.permissionListener?.add(listener)
    }

    override fun onStop() {
        super.onStop()
        snackBar?.dismiss()
    }

    private fun initView() {
        if (checkConditionsGalleryVisibilityLimit()) {
            binding?.vgRecent?.isClickable = false
            binding?.tvRecentFolder?.textColor(R.color.uiKitColorDisabledPrimary)
        } else {
            initClickListenerRecent()
        }
        binding?.vMediaKeyboardDismiss?.setThrottledClickListener {
            viewModel.removeAllPhotos()
            viewModel.sendSelectedEvent()
        }
        binding?.vMediaKeyboardAddSelected?.setThrottledClickListener {
            viewModel.sendSelectedEvent()
        }
        if (!isPermissionGranted) updateGalleryTitle()
        setupRecyclerView()
    }

    private fun initPermissionView() {
        binding?.spvPermissionView?.bind(
            onRequestPermissions = { requestGalleryPermissions() },
            onOpenSettings = { openSettings() }
        )

        checkMediaPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    showPermissionViews(false)
                    builder?.let(viewModel::loadPickerTiles)
                }

                override fun onDenied() {
                    showPermissionViews(permissionState = PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
                }

                override fun needOpenSettings() {
                    showPermissionViews()
                }

                override fun onError(error: Throwable?) {
                    showPermissionViews()
                }
            }
        )
    }

    val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, _, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    showPermissionViews(false)
                    builder?.let(viewModel::loadPickerTiles)
                } else {
                    showPermissionViews()
                }
            }
        }

    private fun requestGalleryPermissions() {
        setMediaPermissions()
    }

    private fun initClickListenerRecent() {
        binding?.vgRecent?.isClickable = true
        binding?.tvRecentFolder?.textColor(R.color.ui_black)
        binding?.vgRecent?.click {
            startGalleryIntent()
        }
    }

    private fun showPermissionViews(
        show: Boolean = true,
        permissionState: PermissionState = PermissionState.NOT_GRANTED_OPEN_SETTINGS
    ) {
        viewModel.showOrHidePermissionViews(show)
        viewModel.setPermissionViewState(permissionState)
    }

    private fun openSettings() {
        requireContext().openSettingsScreen()
    }

    @Suppress("LocalVariableName")
    private fun setupRecyclerView() {
        binding?.rvGallery?.apply {
            adapter = imageGalleryAdapter
            val _layoutManager = GridLayoutManager(requireContext(), 4)
            layoutManager = _layoutManager
            addItemDecoration(GridSpacingItemDecoration(_layoutManager.spanCount, 2.dp, false))
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun provideMeeraPickerAdapter(): MeeraGalleryAdapter {
        return MeeraGalleryAdapter(
            builder = requireNotNull(builder),
            isMultiMediaModeEnabled = isMultiModeEnabled,
            cameraProvider = CameraProvider.Builder(requireActivity().applicationContext)
                .cameraOrientation(CameraOrientation.BACK)
                .build(),
            lifecycleOwner = viewLifecycleOwner,
            onOpenImagePreview = object : MeeraGalleryAdapter.OnOpenImagePreview {
                override fun onOpenPreview(pickerTile: PickerTile, position: Int) {
                    if (position == CAMERA_TILE_POSITION) {
                        startCameraIntent()
                    } else {
                        showPhotoMediaView(imageGalleryAdapter.currentList, position - COLLECTION_OFFSET)
                    }
                }
            },
            onItemClickListener = object : MeeraGalleryAdapter.OnItemClickListener {
                override fun onItemClick(pickerTile: PickerTile) {
                    when (pickerTile?.tileType) {
                        PickerTile.CAMERA -> startCameraIntent()
                        PickerTile.GALLERY -> startGalleryIntent()
                        PickerTile.IMAGE,
                        PickerTile.VIDEO -> handleResult(pickerTile)
                    }
                }
            },
        )
    }

    private fun updateLogoPosition(percentage: Float) {
        (binding.spvPermissionView.layoutParams as ConstraintLayout.LayoutParams).verticalBias =
            TARGET_LOGO_POSITION * percentage
        binding.spvPermissionView.requestLayout()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onBottomSheetStateChange(state: Int) {
        runCatching {
            updateGalleryTitle()
        }.onFailure { Timber.e(it) }
    }

    fun onStartAnimationTransitionFragment() {
        val cameraItem = findCameraViewHolder()
        cameraItem?.setCameraPreviewEnabled(false)
    }

    fun onOpenTransitionFragment() {
        val cameraItem = findCameraViewHolder()
        cameraItem?.setCameraPreviewEnabled(true)
    }

    override fun onDestroyView() {
        act?.permissionListener?.remove(listener)

        super.onDestroyView()
    }

    private fun findCameraViewHolder(): MeeraGalleryViewHolder? {
        val index = imageGalleryAdapter.currentList.indexOfFirst { it.isCameraTile }
        if (index < 0) return null
        return binding?.rvGallery?.findViewHolderForAdapterPosition(index) as? MeeraGalleryViewHolder? ?: return null
    }

    private fun initListener() {
        viewModel.mediaButtonsLiveData.observe(viewLifecycleOwner) { isShow ->
            binding?.apply {
                val isButtonVisible = isShow && isPermissionGranted && !isFromMap
                vMediaKeyboardDismiss.isVisible = isButtonVisible
                vMediaKeyboardAddSelected.isVisible =
                    isButtonVisible && viewModel.pickerTilesLiveData.value?.any { it.isSelected } == true
                if (isShow) {
                    updateGalleryTitle()
                } else {
                    if (isPermissionGranted) updateGalleryTitle()
                }
            }
        }
        viewModel.permissionViewsVisibilityLiveData.observe(viewLifecycleOwner) { isShow ->
            binding?.apply {
                vMediaKeyboardDismiss.isVisible = !isShow && !isFromMap && isMultiModeEnabled
                vMediaKeyboardAddSelected.isVisible = !isShow && !isFromMap && isMultiModeEnabled
                rvGallery.isVisible = !isShow
                spvPermissionView.isVisible = isShow
                isPermissionGranted = !isShow
                updateGalleryTitle()
            }
        }
        viewModel.permissionViewStateLiveData.observe(viewLifecycleOwner) { permissionState ->
            binding?.spvPermissionView?.updateState(permissionState)
        }
        viewModel.pickerTilesLiveData.observe(viewLifecycleOwner) { pickerTiles ->
            imageGalleryAdapter.submitList(pickerTiles)
            updateGalleryTitle()
        }

        viewModel.meeraPickerEffectsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleMeeraPickerEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleMeeraPickerEffect(effect: MeeraPickerUiEffect) {
        when (effect) {
            is MeeraPickerUiEffect.ShowMediaAlert -> showMediaAlertMessage(effect.messageRes)
            is MeeraPickerUiEffect.ShowTooLongVideoAlert -> showDialogToLongVideo(effect.uri)
            is MeeraPickerUiEffect.UpdateListTiles -> builder?.let(viewModel::loadPickerTiles)
        }
    }

    private fun handleResult(pickerTile: PickerTile) {
        pickerTile.imageUri?.let { complete(it) }
    }

    private fun showPhotoMediaView(
        list: List<PickerTile>,
        currentPosition: Int,
        openEditor: Boolean = false
    ) {
        if (list.isEmpty()) return
        val subList = list.subList(1, list.size)
        val imageList = ArrayList<ImageViewerData>(subList.size + 1)
        for (i in subList) {
            val type =
                if (i.tileType == PickerTile.VIDEO) RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING
                else RecyclingPagerAdapter.VIEW_TYPE_IMAGE
            val data = ImageViewerData(i.imageUri.toString(), "", -1L, type)
            if (imageGalleryAdapter.currentList.filter { it.isSelected }.contains(i)) {
                data.isSelected = true
                data.cnt = i.counter
            }
            imageList.add(data)
        }
        if (imageList.size == 0) return
        val mediaBuilder = with(context)
            .setMeeraAct(requireActivity() as? MeeraAct?)
            .setImageList(imageList)
            .startPosition(currentPosition)
            .setType(MediaControllerOpenPlace.Chat)
            .setLifeCycle(lifecycle)
            .openEditor(openEditor)
            .setSelectedCount(imageGalleryAdapter.currentList.filter { it.isSelected }.size)
            .setOrientationChangedListener(screenListener)
            .onGetPreselectedMedia { viewModel.editedPhotosLiveData.value?.map { Uri.parse(it) }?.toSet() }
            .setUniqueNameSuggestionsMenu(
                SuggestionsMenu(this, SuggestionsMenuType.ROAD)
            )
            .onImageReady { img: String? ->
                img?.let { complete(Uri.parse(it)) }
            }
            .onChangeListener(object : OnImageChangeListener {
                override fun onImageChange(position: Int) = Unit
                override fun onImageAdded(image: ImageViewerData) {
                    if (image.viewType == RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING) {
                        addVideoToLaterDelete(Uri.parse(image.imageUrl))
                    }
                }

                override fun onImageChecked(image: ImageViewerData, isChecked: Boolean) {
                    image.imageUrl?.let { uri ->
                        if (isChecked) {
                            viewModel.addPhotoClicked(uri)
                        } else {
                            viewModel.removePhotoClicked(uri)
                        }
                    }
                }
            })
            .onImageEdit { img ->
                if (builder?.onImageEditListener != null) {
                    builder?.onImageEditListener?.onImageEdit(Uri.parse(img))
                }
            }
            .setMessage(viewModel.messageTextLiveData.value?.text ?: String.empty())
            .setUserName(viewModel.userNameLiveData.value?.name ?: String.empty())
            .addTextWatcher { viewModel.messageChanged(it) }
            .onImageReadyWithText { image: List<Uri>?, text: String ->
                builder?.onImageWithTextReady?.onImageWithText(image, text)
            }
        mediaBuilder.show()
    }

    private fun complete(uri: Uri, isCameraImage: Boolean = false) {
        if (!isMultiModeEnabled) {
            builder?.onImageSelectedListener?.onImageSelected(uri)
            return
        }

        if (isCameraImage.not()) {
            viewModel.toggleSelection(uri)
        } else {
            viewModel.addImageFromCamera(uri)
        }
    }

    private fun updateGalleryTitle() {
        val selected = viewModel.pickerTilesLiveData.value.orEmpty().filter { it.isSelected }
        if (!isPermissionGranted) {
            binding?.vgRecent?.isClickable = false
            binding?.ivRecentFolderArrow?.gone()
            binding?.tvRecentFolder?.text = getString(R.string.gallery_title)
        } else if (selected.isNotEmpty()) {
            binding?.vgRecent?.isClickable = false
            binding?.ivRecentFolderArrow?.gone()
            val hasVideoSelected = selected.any { it.isVideoTile }
            if (hasVideoSelected) {
                binding?.tvRecentFolder?.text = getString(R.string.count_of_videos_selected, selected.size)
            } else {
                binding?.tvRecentFolder?.text = getString(R.string.select_max_count, selected.size)
            }
        } else {
            binding?.vgRecent?.isClickable = true
            binding?.ivRecentFolderArrow?.visible()
            binding?.tvRecentFolder?.text = getString(R.string.recent)
        }
    }

    private fun startCameraIntent() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            when (currentMediaType) {
                MediaType.IMAGE,
                MediaType.IMAGE_AND_VIDEO -> if (activity != null) {
                    getImageFromCamera(requireActivity(), object : ImageCaptureUtils.Listener {
                        override fun onResult(result: ImageCaptureResultModel) {
                            cameraImageUri = result.fileUri
                            cameraImageUri?.let { complete(it, isCameraImage = true) }
                        }

                        override fun onFailed() {
                            findCameraViewHolder()?.bindingAdapterPosition?.let(imageGalleryAdapter::notifyItemChanged)
                        }
                    }, CameraCharacteristics.LENS_FACING_BACK)
                }
            }
        } else {
            requestCameraPermissions()
        }
    }

    private fun requestCameraPermissions() {
        setPermissionsWithSettingsOpening(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    startCameraIntent()
                }

                override fun needOpenSettings() {
                    showCameraSettingsDialog()
                }

                override fun onError(error: Throwable?) {
                    showCameraSettingsDialog()
                }
            },
            Manifest.permission.CAMERA
        )
    }

    private fun showCameraSettingsDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.camera_settings_dialog_title)
            .setDescription(R.string.camera_settings_dialog_description)
            .setTopBtnText(R.string.camera_settings_dialog_action)
            .setBottomBtnText(R.string.camera_settings_dialog_cancel)
            .setCancelable(false)
            .setTopClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    private fun startGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        if (currentMediaType == MediaType.IMAGE || currentMediaType == MediaType.IMAGE_AND_VIDEO) {
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        } else {
            galleryIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
        }
        if (galleryIntent.resolveActivity(requireActivity().packageManager) == null) {
            Timber.e("This Phone do not have Gallery Application")
            return
        }
        TedOnActivityResult.with(activity)
            .setIntent(galleryIntent)
            .setListener { resultCode: Int, data: Intent? ->
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        onActivityResultGallery(it)
                    }
                }
            }
            .startActivityForResult()
    }

    private fun onActivityResultGallery(data: Intent) {
        val temp = data.data
        val realPath = RealPathUtil.getRealPath(
            activity, temp
        )
        val selectedImageUri = try {
            Uri.fromFile(File(realPath))
        } catch (ex: Exception) {
            Timber.e(ex)
            if (realPath == null) return
            Uri.parse(realPath)
        }
        complete(selectedImageUri)
    }

    private fun showDialogToLongVideo(uri: Uri?) {
        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.warning_video_duration_title))
            .setDescription(getString(R.string.you_cant_send_video_more))
            .setTopBtnText(getString(R.string.meera_open_editor))
            .setBottomBtnText(getString(R.string.cancel))
            .setCancelable(false)
            .setTopClickListener {
                openEditor(uri)
            }
            .show(childFragmentManager)
    }

    private fun openEditor(uri: Uri?) {
        if (uri == null) {
            return
        }
        try {
            showPhotoMediaView(
                list = imageGalleryAdapter.currentList,
                currentPosition = imageGalleryAdapter.currentList.indexOfFirst { it.imageUri == uri } - COLLECTION_OFFSET,
                openEditor = true
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun addVideoToLaterDelete(uri: Uri?) {
        if (uri == null) return
        val app = requireContext().applicationContext as? App?
        if (uri.path != null) {
            app?.hashSetVideoToDelete?.add(uri.path!!)
        }
    }

    private class OrientationListener : OrientationScreenListener() {
        override fun onOrientationChanged(orientation: Int) {
            orientationChangedListener.invoke(orientation)
        }
    }

    private fun showMediaAlertMessage(@StringRes alertResId: Int) {
        snackBar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(alertResId),
                    avatarUiState = AvatarUiState.WarningIconState,
                ),
            )
        ).also { snackBar ->
            if (pickerExpandStatus == ExpandStatus.FULL_EXPANDED) {
                snackBar.setAnchorView(binding.vMediaKeyboardDismiss)
            } else {
                snackBar.setAnchorView(requireActivity().window.decorView.findViewById(R.id.vg_white_container))
            }
            snackBar.show()
        }
    }

    private fun checkGalleryVisibilityLimit() {
        if (checkConditionsGalleryVisibilityLimit()) {
            binding?.vgPermissionMediaRequest?.visibility = View.VISIBLE
            binding?.vgRecent?.isClickable = false
            binding?.tvRecentFolder?.textColor(R.color.uiKitColorDisabledPrimary)
            binding?.tvChangePermissionRedMediaVisual?.setThrottledClickListener {
                openSettings()
            }
            builder?.let(viewModel::loadPickerTiles)
        } else {
            initClickListenerRecent()
            binding?.vgPermissionMediaRequest?.visibility = View.GONE
        }
    }

    private fun checkConditionsGalleryVisibilityLimit(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            requireContext().isEnabledPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) &&
            (!requireContext().isEnabledPermission(Manifest.permission.READ_MEDIA_IMAGES) ||
                !requireContext().isEnabledPermission(Manifest.permission.READ_MEDIA_VIDEO))
    }

    private enum class ExpandStatus {
        INVISIBLE, HALF_EXPANDED, FULL_EXPANDED
    }
}
