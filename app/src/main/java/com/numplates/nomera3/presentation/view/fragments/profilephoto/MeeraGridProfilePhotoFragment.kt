package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.gone
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraGridPhotoLayoutFragmentBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.profilephoto.ProfilePhotoAdapter
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.viewmodel.profilephoto.GridProfilePhotoViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.GridProfilePhotoEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.GridProfileViewEvent
import timber.log.Timber

private const val COUNT_COLUMN_GRID_IMAGE = 3
private const val ONLY_COUNT_COLUMN_GRID_IMAGE = 1

class MeeraGridProfilePhotoFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_grid_photo_layout_fragment,
        behaviourConfigState = ScreenBehaviourState.Full
    ),
    BaseLoadImages by BaseLoadImagesDelegate(),
    ProfilePhotoAdapter.ProfilePhotoCallback,
    TedBottomSheetPermissionActionsListener,
    BasePermission by BasePermissionDelegate() {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraGridPhotoLayoutFragmentBinding::bind)
    private lateinit var viewModel: GridProfilePhotoViewModel

    private var photoRecycler: RecyclerView? = null
    private var adapter: ProfilePhotoAdapter? = null

    private var userID: Long = -1
    private var totalPhotos: Int = 10

    /*
    * флаг для отслеживания удаления фото во фрагменте детального просмотра, можно будет заменить
    * на setFragmentResultListener() в androidx.fragment:fragment:1.3.0, см.
    * https://developer.android.com/training/basics/fragments/pass-data-between
    * */
    private var isPhotoDeletedInChildFragment = false

    private var mediaPicker: TedBottomSheetDialogFragment? = null

    override fun onStart() {
        super.onStart()
        onStartFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecycler()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)

        viewModel.init(userID)
        userID = if (userID != -1L) userID else viewModel.getUserUid()
        initObservers()

        if (viewModel.getUserUid() == userID)
            binding?.vAddPhotoBtn?.visible()
        else
            binding?.vAddPhotoBtn?.gone()

        binding?.vAddPhotoBtn?.setThrottledClickListener {
            viewModel.onAddPhotoClicked()
            checkMediaPermissions(
                object : PermissionDelegate.Listener {
                    override fun onGranted() {
                        openMediaPickerWithPermissionState(PermissionState.GRANTED)
                    }

                    override fun onDenied() {
                        openMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
                    }

                    override fun needOpenSettings() {
                        openMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                    }
                }
            )
        }

        binding?.srlGridProfile?.setOnRefreshListener {
            viewModel.refreshPhotoList()
        }

        (requireActivity() as? MeeraAct)?.permissionListener?.add(listener)
    }

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        }

    override fun onDestroyView() {
        (requireActivity() as? MeeraAct)?.permissionListener?.remove(listener)

        super.onDestroyView()
    }

    private fun onStartFragment() {
        if (isPhotoDeletedInChildFragment) {
            isPhotoDeletedInChildFragment = false
            viewModel.refreshPhotoList()
        }
        viewModel.logScreenForFragment(this.javaClass.simpleName)
    }

    override fun onPhotoClicked(position: Int) {
        viewModel.onPhotoClicked(position)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            args.get(IArgContainer.ARG_GALLERY_IMAGES_COUNT)?.let {
                totalPhotos = it as Int
            }
        }

        arguments?.let { args ->
            args.get(IArgContainer.ARG_USER_ID)?.let {
                userID = it as Long
            }
        }
        viewModel = ViewModelProviders.of(this).get(GridProfilePhotoViewModel::class.java)
    }

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
                    mediaPicker?.openCamera()
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
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.camera_settings_dialog_title)
            .setDescription(R.string.camera_settings_dialog_description)
            .setTopBtnText(R.string.camera_settings_dialog_action)
            .setBottomBtnText(R.string.camera_settings_dialog_cancel)
            .setCancelable(false)
            .setTopClickListener { requireContext().openSettingsScreen() }
            .show(childFragmentManager)
    }

    private fun openMediaPickerWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadMultiImage(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            maxCount = 5,
            type = MediaControllerOpenPlace.CreatePost,
            message = "",
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = requireActivity() as MeeraAct,
                onReadyImagesUri = { images -> onImagesReady(images) },
                onReadyImagesUriWithText = { images, _ -> onImagesReady(images) }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK
        )
    }

    private fun initRecycler() {
        adapter = ProfilePhotoAdapter(resources.displayMetrics, this)

        val columns = COUNT_COLUMN_GRID_IMAGE //Columns count
        val layoutManager = GridLayoutManager(context, columns)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter?.getItemViewType(position)) {
                    ProfilePhotoAdapter.TYPE_FULL -> COUNT_COLUMN_GRID_IMAGE
                    else -> ONLY_COUNT_COLUMN_GRID_IMAGE
                }
            }
        }

        photoRecycler?.layoutManager = layoutManager
        photoRecycler?.adapter = adapter

    }

    private fun showLoadingIndicator() {
        binding?.vAddPhotoBtn?.gone()
    }

    private fun hideLoadingIndicator() {
        binding?.vAddPhotoBtn?.visible()
    }

    private fun initObservers() {
        viewModel.liveStartImageEventSingle.observe(viewLifecycleOwner, Observer {
            openPhotoViewerScreen(it)
        })

        viewModel.pagedListLiveData.observe(viewLifecycleOwner, Observer {
            binding?.srlGridProfile?.isRefreshing = false
            adapter?.submitList(it)
        })

        viewModel.liveErrorEvent.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is GridProfilePhotoEvent.OnErrorSocket -> showNToastError()
                is GridProfilePhotoEvent.OnCloseGalleryScreen -> findNavController().popBackStack()
                else -> Unit
            }
        })

        viewModel.liveViewEvents.observe(viewLifecycleOwner, Observer {
            when (it) {
                is GridProfileViewEvent.OnPhotoLoadedSuccess -> {
                    hideLoadingIndicator()
                    viewModel.refreshPhotoList()
                }

                is GridProfileViewEvent.OnPhotoLoadedError -> {
                    hideLoadingIndicator()
                    showErrorWhilePhotoUpload()
                }

                else -> Unit
            }
        })
    }

    private fun showNToastError() {
        com.meera.core.utils.showCommonError(getText(R.string.error_try_later), requireView())
    }

    private fun showErrorWhilePhotoUpload() {
        com.meera.core.utils.showCommonError(getText(R.string.error_while_loading_photo), requireView())
    }

    private fun openPhotoViewerScreen(position: Int) {
        val isOwnProfile = viewModel.getUserUid() == userID
        findNavController().safeNavigate(
            R.id.action_meeraGridProfilePhotoFragment_to_meeraProfilePhotoViewerFragment,
            bundle = bundleOf(
                IArgContainer.ARG_GALLERY_POSITION to position,
                IArgContainer.ARG_IS_PROFILE_PHOTO to false,
                IArgContainer.ARG_IS_OWN_PROFILE to isOwnProfile,
                IArgContainer.ARG_USER_ID to userID
            )
        )
    }

    private fun initView() {
        photoRecycler = binding?.rvGridPhoto
        binding?.vNavView?.backButtonClickListener = {
            findNavController().popBackStack()
        }
    }

    //upload to user gallery
    private fun onImagesReady(images: List<Uri>) {
        if (images.isNullOrEmpty()) return
        showLoadingIndicator()
        viewModel.uploadUserPhotos(images)
        viewModel.liveUploadMediaToGallery?.observe(viewLifecycleOwner, Observer {
            handleUpload(it)
        })
    }

    private fun handleUpload(workInfo: WorkInfo?) {
        Timber.d("Bazaleev Operation.State = $workInfo")
        if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
            hideLoadingIndicator()
            viewModel.refreshPhotoList()
        }
        if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
            hideLoadingIndicator()
            showErrorWhilePhotoUpload()
        }
    }

}
