package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.isEnabledPermission
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils.getImageFromCamera
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMediakeyboardPickerBinding
import com.numplates.nomera3.modules.chat.SNACKBAR_BOTTOM_FULL_EXPANDED_MAX_MEDIA_COUNT_MARGIN_DP
import com.numplates.nomera3.modules.chat.SNACKBAR_BOTTOM_HALF_EXPANDED_MAX_MEDIA_COUNT_MARGIN_DP
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity.Album
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.viewmodel.PickerViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.BottomSheetSlideOffsetListener
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer.Companion.with
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnImageChangeListener
import com.numplates.nomera3.presentation.view.utils.camera.CameraOrientation
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment.BaseBuilder.MediaType
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.adapter.GalleryAdapter
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.util.RealPathUtil
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val MAX_VIDEO_COUNT = 1
const val MAX_PICTURE_COUNT = 5
private const val MAX_VIDEO_DURATION_MS = 5 * 60 * 1000
private const val UPDATE_LIST_ITEM_DELAY = 200L

private const val EXTRA_CAMERA_IMAGE_URI = "camera_image_uri"
private const val EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri"
private const val DEFAULT_MEDIA_COUNT = 0

private const val EMPTY_VIEW_HEIGHT = 234
private const val DEFAULT_EMPTY_VIEW_POSITION = 150

class PickerFragment :
    BaseFragmentNew<FragmentMediakeyboardPickerBinding>(),
    BasePermission by BasePermissionDelegate(), BottomSheetSlideOffsetListener {

    @Inject
    lateinit var filesManager: FileManager

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMediakeyboardPickerBinding
        get() = FragmentMediakeyboardPickerBinding::inflate

    var builder: TedBottomSheetDialogFragment.BaseBuilder<*>? = null

    private val viewModel: PickerViewModel by viewModels {
        App.component.getViewModelFactory()
    }

    private val isOpenFromMoments by lazy {
        builder?.type == MediaControllerOpenPlace.Moments
    }

    private var currentMediaType: Int = MediaType.IMAGE_AND_VIDEO

    private var imageGalleryAdapter: GalleryAdapter? = null

    private var selectedUriList: MutableList<Uri> = mutableListOf()
    private var tempUriList: List<Uri>? = null
    private var cameraImageUri: Uri? = null
    private var mediaSize: Int? = null

    private val screenListener: OrientationScreenListener = OrientationListener()

    private var pickerExpandStatus = ExpandStatus.INVISIBLE
    private var snackBar: NSnackbar? = null

    override fun onBottomSheetSlide(slideOffset: Float) {
        val bottomSheetOpenedEmptyViewPosition =
            binding?.root?.height?.div(2)?.minus(EMPTY_VIEW_HEIGHT.dp) ?: DEFAULT_EMPTY_VIEW_POSITION.dp
        val offset = bottomSheetOpenedEmptyViewPosition * slideOffset
        binding?.spvPermissionView?.setMargins(top = offset.toInt())
        setExpandStatus(slideOffset)
    }

    private fun setExpandStatus(slideOffset: Float) {
        pickerExpandStatus = when(slideOffset) {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(AlbumsBottomSheetDialogFragment.KEY_ALBUM) { _, bundle ->
            val album = bundle.getParcelable<Album>(AlbumsBottomSheetDialogFragment.KEY_ALBUM)
            album?.let {
                imageGalleryAdapter?.setBucketId(it.id)
                binding?.tvFolder?.text = album.name
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
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri)
        outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, ArrayList(selectedUriList))
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initView()
        initVideoDuration()
        initListener()
        initPermissionView()

        act.permissionListener.add(listener)
    }

    override fun onStop() {
        super.onStop()
        snackBar?.dismiss()
    }

    private fun initView() {
        if (isOpenFromMoments) {
            binding?.tvGalleryHeader?.textColor(R.color.ui_white)
            binding?.tvChosenCount?.textColor(R.color.ui_white)
        }
        if (checkConditionsGalleryVisibilityLimit()){
            binding?.vgRecent?.isClickable = false
            binding?.tvFolder?.textColor(R.color.uiKitColorDisabledPrimary)
        } else {
            initClickListenerRecent()
        }
        binding?.tvMediaKeyboardDismiss?.setThrottledClickListener {
            viewModel.removeAllPhotos()
            viewModel.sendSelectedEvent()
        }
        binding?.tvMediaKeyboardAddSelected?.setThrottledClickListener {
            viewModel.sendSelectedEvent()
        }
        setupRecyclerView()
    }

    private fun initPermissionView() {
        binding?.spvPermissionView?.bind(
            useDarkMode = isOpenFromMoments,
            onRequestPermissions = { requestGalleryPermissions() },
            onOpenSettings = { openSettings() }
        )

        checkMediaPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    showPermissionViews(false)
                    imageGalleryAdapter?.permissionsGranted()
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
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    showPermissionViews(false)
                    imageGalleryAdapter?.permissionsGranted()
                } else {
                    showPermissionViews()
                }
            }
        }

    private fun requestGalleryPermissions() {
        setMediaPermissions()
    }

    private fun initClickListenerRecent(){
        binding?.vgRecent?.isClickable = true
        binding?.tvFolder?.textColor(if (isOpenFromMoments) R.color.ui_white else R.color.ui_black)
        binding?.vgRecent?.click {
            startGalleryIntent()
        }
    }

    private fun showPermissionViews(show: Boolean = true, permissionState: PermissionState = PermissionState.NOT_GRANTED_OPEN_SETTINGS) {
        viewModel.showOrHidePermissionViews(show)
        viewModel.setPermissionViewState(permissionState)
    }

    private fun openSettings() {
        requireContext().openSettingsScreen()
    }

    private fun setupRecyclerView() {
        binding?.rvGallery?.apply {
            val gridLayoutManager = GridLayoutManager(
                activity, 3
            )
            layoutManager = gridLayoutManager
            itemAnimator = null
        }
        updateAdapter()
    }

    private fun updateAdapter() {
        val cameraProvider = CameraProvider.Builder(requireActivity().applicationContext)
            .cameraOrientation(CameraOrientation.BACK)
            .build()
        imageGalleryAdapter = GalleryAdapter(
            context,
            builder,
            cameraProvider,
            viewLifecycleOwner,
            filesManager,
        )

        imageGalleryAdapter?.isMultiModeEnabled = builder?.onMultiImageSelectedListener != null

        binding?.rvGallery?.adapter = imageGalleryAdapter
        val animator = binding?.rvGallery?.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations =
                false
        }
        imageGalleryAdapter?.setOnItemClickListener { view: View?, position: Int ->
            val pickerTile =
                imageGalleryAdapter?.getItem(position)
            when (pickerTile?.tileType) {
                PickerTile.CAMERA -> startCameraIntent()
                PickerTile.GALLERY -> startGalleryIntent()
                PickerTile.IMAGE -> {
                    handleResult(pickerTile)
                }
                PickerTile.VIDEO -> {
                    handleResult(pickerTile)
                }
            }
        }

        imageGalleryAdapter?.setBlockedOnItemClickListener { _, _, maxMediaCount ->
            showMaxMediaCountMessage(maxMediaCount)
        }
        imageGalleryAdapter?.setOnOpenImagePreview { view: View?, position: Int ->
            imageGalleryAdapter?.collection?.let { collection ->
                showPhotoMediaView(collection, position - 1)
            }
        }
    }

    override fun onStartAnimationTransitionFragment() {
        super.onStartAnimationTransitionFragment()
        val cameraItem = findCameraViewHolder()
        cameraItem?.setCameraPreviewEnabled(false)
    }

    override fun onOpenTransitionFragment() {
        super.onOpenTransitionFragment()
        val cameraItem = findCameraViewHolder()
        cameraItem?.setCameraPreviewEnabled(true)
    }

    override fun onDestroyView() {
        imageGalleryAdapter = null
        act.permissionListener.remove(listener)

        super.onDestroyView()
    }

    private fun findCameraViewHolder(): GalleryAdapter.GalleryViewHolder? {
        val index =
            imageGalleryAdapter?.pickerTiles?.indexOfFirst { it.isCameraTile } ?: INDEX_NOT_FOUND
        if (index < 0) return null
        return binding?.rvGallery?.findViewHolderForAdapterPosition(index) as? GalleryAdapter.GalleryViewHolder?
            ?: return null
    }

    private fun initVideoDuration() {
        val duration =
            TedBottomSheetDialogFragment.IVideoDurationRequest {
                    uri: Uri?,
                    listener: TedBottomSheetDialogFragment.IOnDurationReady ->
                val liveData = MutableLiveData<Long>()
                val t = Thread {
                    val durationMils = filesManager.getVideoDurationMils(uri)
                    liveData.postValue(durationMils)
                }
                liveData.observe(viewLifecycleOwner, listener::onResult)
                t.start()
            }
        imageGalleryAdapter?.setDurationListener(duration)
    }

    private fun initListener() {
        viewModel.totalMediaListLiveData.observe(viewLifecycleOwner) { media ->
            imageGalleryAdapter?.setPreselectedMedia(media.mediaFromMessage)
            mediaSize = media.mediaFromMessage.size + media.mediaFromPicker.size
            val mediaFromMessage = media.mediaFromMessage
            val messageHasVideoAttachment =
                mediaFromMessage.firstOrNull()?.let { filesManager.isVideoUri(Uri.parse(it)) } == true
            val messageHasMaxMediaCount = mediaFromMessage.size == MAX_PICTURE_COUNT
            checkUriList()
            if (messageHasVideoAttachment || messageHasMaxMediaCount) {
                disableAll()
                return@observe
            }
            val pickerMedia = media.mediaFromPicker
            if (pickerMedia.isEmpty()) {
                enableAll()
                imageGalleryAdapter?.updateWhiteBg()
            } else {
                val changedPhotoString = if (pickerMedia.size < selectedUriList.size) {
                    selectedUriList.map { it.toString() }
                        .minus(pickerMedia)
                        .firstOrNull() ?: return@observe
                } else if (pickerMedia.size > selectedUriList.size) {
                    pickerMedia.minus(selectedUriList.map { it.toString() })
                        .firstOrNull() ?: return@observe
                } else {
                    return@observe
                }
                imageSelected(Uri.parse(changedPhotoString))
            }
            viewModel.setButtonStateByBehavior()
        }
        viewModel.mediaButtonsLiveData.observe(viewLifecycleOwner) { isShow ->
            binding?.tvMediaKeyboardDismiss?.isVisible = isShow
            binding?.tvMediaKeyboardAddSelected?.isVisible = isShow && selectedUriList.isNotEmpty()
        }
        viewModel.permissionViewsVisibilityLiveData.observe(viewLifecycleOwner) { isShow ->
            binding?.apply {
                vgMediaButtonsRoot.isVisible = !isShow
                rvGallery.isVisible = !isShow
                tvGalleryHeader.isVisible = isShow
                spvPermissionView.isVisible = isShow
            }
        }

        viewModel.permissionViewStateLiveData.observe(viewLifecycleOwner) { permissionState ->
            binding?.spvPermissionView?.updateState(permissionState)
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
            if (imageGalleryAdapter?.selectedPickerTiles?.contains(i) == true) {
                data.isSelected = true
                data.cnt = i.counter
            }
            imageList.add(data)
        }
        if (imageList.size == 0) return
        val mediaBuilder = with(context)
            .setImageList(imageList)
            .startPosition(currentPosition)
            .setType(MediaControllerOpenPlace.Chat)
            .setLifeCycle(lifecycle)
            .openEditor(openEditor)
            .setSelectedCount(imageGalleryAdapter?.selectedPickerTiles?.size ?: 0)
            .setOrientationChangedListener(screenListener)
            .onGetPreselectedMedia { viewModel.editedPhotosLiveData.value?.map { Uri.parse(it) }?.toSet() }
            .setUniqueNameSuggestionsMenu(
                SuggestionsMenu(
                    act.getCurrentFragment(),
                    SuggestionsMenuType.ROAD
                )
            )
            .setAct(act)
            .onImageReady { img: String? ->
                img?.let { complete(Uri.parse(it)) }
            }
            .onChangeListener(object : OnImageChangeListener {
                override fun onImageChange(position: Int) = Unit
                override fun onImageAdded(image: ImageViewerData) {
                    if (image.viewType == RecyclingPagerAdapter.VIEW_TYPE_IMAGE) {
                        imageGalleryAdapter?.addItemNotSelected(
                            Uri.parse(image.imageUrl),
                            RecyclingPagerAdapter.VIEW_TYPE_IMAGE
                        )
                    } else if (image.viewType == RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING) {
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
            .addTextWatcher { viewModel.messageChanged(it) }
            .onImageReadyWithText { image: List<Uri>?, text: String ->
                builder?.onImageWithTextReady?.onImageWithText(image, text)
            }
        mediaBuilder.show()
    }

    private fun imageSelected(uri: Uri) {
        val index =
            imageGalleryAdapter?.pickerTiles?.indexOfFirst { it.imageUri == uri } ?: INDEX_NOT_FOUND
        if (index < 0) return
        imageGalleryAdapter?.apply {
            val pickerTile = pickerTiles[index]
            pickerTile.isSelected = !pickerTile.isSelected
            if (selectedUriList.contains(uri)) {
                val selectedPickerTileIndex =
                    selectedPickerTiles.indexOfFirst { it.imageUri == uri }
                if (selectedPickerTileIndex >= 0) {
                    selectedPickerTiles.removeAt(selectedPickerTileIndex)
                }
                updateCounters(pickerTile.counter)
                pickerTile.counter = DEFAULT_MEDIA_COUNT
            } else {
                if (selectedPickerTiles.size < MAX_PICTURE_COUNT && !pickerTile.isCameraTile) {
                    selectedPickerTiles.add(pickerTile)
                    pickerTile.counter = selectedPickerTiles.size
                }
            }
            updateWhiteBg()
            handleResult(pickerTile)
        }
    }

    private fun complete(uri: Uri) {
        if (imageGalleryAdapter?.isMultiModeEnabled == false) {
            builder?.onImageSelectedListener?.onImageSelected(uri)
            return
        }
        if (filesManager.getMediaType(uri) == MEDIA_TYPE_VIDEO) {
            val videoDuration = filesManager.getVideoDurationMils(uri)
            if (videoDuration > MAX_VIDEO_DURATION_MS) {
                showDialogToLongVideo(uri)
                return
            }
        }
        val uriString = uri.toString()
        if (selectedUriList.contains(uri)) {
            removeImage(uri)
            viewModel.removePhotoClicked(uriString)
        } else {
            addUri(uri)
            viewModel.addPhotoClicked(uriString)
        }
    }

    private fun checkUriList() {
        if (selectedUriList.size > 0) {
            binding?.tvChosenCount?.text =
                if (filesManager.getMediaType(selectedUriList.first()) == MEDIA_TYPE_VIDEO) {
                    String.format(
                        getString(R.string.count_of_videos_selected),
                        selectedUriList.size
                    )
                } else {
                    String.format(
                        getString(R.string.select_max_count),
                        mediaSize ?: selectedUriList.size
                    )
                }
            binding?.tvChosenCount?.visible()
            binding?.vgRecent?.gone()
        } else if ((mediaSize ?: 0) > 0) {
            binding?.tvChosenCount?.text =
                String.format(
                    getString(R.string.select_max_count),
                    mediaSize ?: selectedUriList.size
                )
            binding?.tvChosenCount?.visible()
            binding?.vgRecent?.gone()
        } else {
            binding?.tvChosenCount?.gone()
            binding?.vgRecent?.visible()
        }
    }


    private fun addUri(uri: Uri) {
        if (selectedUriList.size == MAX_PICTURE_COUNT) {
            val message =
                String.format(resources.getString(R.string.select_max_count), MAX_PICTURE_COUNT)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            return
        }
        selectedUriList.add(uri)
        imageGalleryAdapter?.setSelectedUriList(selectedUriList, uri)
        checkUriList()
        viewModel.setButtonStateByBehavior()
    }

    private fun removeImage(uri: Uri) {
        selectedUriList.remove(uri)
        checkUriList()
        imageGalleryAdapter?.setSelectedUriList(selectedUriList, uri)
        viewModel.setButtonStateByBehavior()
    }

    private fun disableAll() {
        selectedUriList.clear()
        imageGalleryAdapter?.disableAll()
    }

    private fun enableAll() {
        selectedUriList.clear()
        imageGalleryAdapter?.unSelectAll()
    }

    private fun startCameraIntent() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            when (currentMediaType) {
                MediaType.IMAGE, MediaType.IMAGE_AND_VIDEO -> if (activity != null) {
                    getImageFromCamera(act, object : ImageCaptureUtils.Listener {
                        override fun onResult(result: ImageCaptureResultModel) {
                            cameraImageUri = result.fileUri
                            if (imageGalleryAdapter?.isMultiModeEnabled == true) {
                                complete(result.fileUri)
                                addNewSelectedItemToAdapter(cameraImageUri!!)
                            } else {
                                imageGalleryAdapter?.addItemNotSelected(
                                    result.fileUri,
                                    RecyclingPagerAdapter.VIEW_TYPE_IMAGE
                                )
                            }
                        }

                        override fun onFailed() {
                            binding?.rvGallery?.postDelayed({ imageGalleryAdapter?.updateCameraItem() }, UPDATE_LIST_ITEM_DELAY)
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
        ConfirmDialogBuilder()
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

    private fun startGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        if (currentMediaType == MediaType.IMAGE || currentMediaType == MediaType.IMAGE_AND_VIDEO) {
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        } else {
            galleryIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
        }
        if (galleryIntent.resolveActivity(act.packageManager) == null) {
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

    private fun addNewSelectedItemToAdapter(uri: Uri) {
        imageGalleryAdapter?.addItemSelected(uri)
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
        imageGalleryAdapter?.setSelected(selectedImageUri, true)
        complete(selectedImageUri)
    }

    private fun showDialogToLongVideo(uri: Uri?) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.warning_video_duration_title))
            .setDescription(getString(R.string.you_cant_send_video_more))
            .setLeftBtnText(getString(R.string.cancel_caps))
            .setRightBtnText(getString(R.string.open_editor_caps))
            .setCancelable(false)
            .setLeftClickListener {
                enableAll()
            }
            .setRightClickListener {
                openEditor(uri)
            }.show(childFragmentManager)
    }

    private fun openEditor(uri: Uri?) {
        if (uri == null) {
            return
        }

        try {
            imageGalleryAdapter?.collection?.let { collection ->
                showPhotoMediaView(
                    list = collection,
                    currentPosition = collection.indexOfFirst { it.imageUri == uri } - COLLECTION_OFFSET,
                    openEditor = true
                )
            }

            /*
                        act.getMediaEditorController().open(
                uri,
                MediaViewerEnum.CHAT,
                object : IMediaEditorCallback {
                    override fun onPhotoReady(resultUri: Uri) {}
                    override fun onVideoReady(resultUri: Uri) {
                        onVideoReadyUri(resultUri)
                    }

                    override fun onError() {
                        enableAll()
                    }

                    override fun onCanceled() {
                        enableAll()
                    }
                },
            )
             */
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

    private fun showMaxMediaCountMessage(maxMediaCount: Int) {
        val keyboardHeight = pxToDp(viewModel.getKeyboardHeight())
        val marginBottom = when(pickerExpandStatus) {
            ExpandStatus.FULL_EXPANDED -> SNACKBAR_BOTTOM_FULL_EXPANDED_MAX_MEDIA_COUNT_MARGIN_DP
            ExpandStatus.HALF_EXPANDED -> keyboardHeight + SNACKBAR_BOTTOM_HALF_EXPANDED_MAX_MEDIA_COUNT_MARGIN_DP
            else -> 0
        }

        snackBar = NSnackbar.with(view)
            .inView(view)
            .typeError()
            .text(getString(R.string.maximum_media_files_selected, maxMediaCount))
            .marginBottom(marginBottom)
            .durationLong()
            .show()
    }

    private fun checkGalleryVisibilityLimit() {
        if (checkConditionsGalleryVisibilityLimit()) {
            binding?.vgPermissionMediaRequest?.visibility = View.VISIBLE
            binding?.vgRecent?.isClickable = false
            binding?.tvFolder?.textColor(R.color.uiKitColorDisabledPrimary)
            binding?.tvChangePermissionRedMediaVisual?.setThrottledClickListener {
                openSettings()
            }
            imageGalleryAdapter?.loadTiles()
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

    companion object {
        private const val INDEX_NOT_FOUND = -1

        private const val COLLECTION_OFFSET = 1

        private enum class ExpandStatus {
            INVISIBLE, HALF_EXPANDED, FULL_EXPANDED
        }
    }
}
