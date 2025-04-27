package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.extensions.gone
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGridPhotoLayoutBinding
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.profilephoto.ProfilePhotoAdapter
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.profilephoto.GridProfilePhotoViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.GridProfilePhotoEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.GridProfileViewEvent
import timber.log.Timber

class GridProfilePhotoFragment :
    BaseFragmentNew<FragmentGridPhotoLayoutBinding>(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    ProfilePhotoAdapter.ProfilePhotoCallback,
    TedBottomSheetPermissionActionsListener {
    private lateinit var viewModel: GridProfilePhotoViewModel

    private var photoRecycler: RecyclerView? = null
    private var title: TextView? = null
    private var backBtn: ImageView? = null
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGridPhotoLayoutBinding
        get() = FragmentGridPhotoLayoutBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecycler()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)

        viewModel.init(userID)
        userID = if (userID != -1L) userID else viewModel.getUserUid()
        initObservers()

        if (viewModel.getUserUid() == userID)
            binding?.toolbar?.ivAddPhoto?.visible()
        else
            binding?.toolbar?.ivAddPhoto?.gone()

        binding?.toolbar?.ivAddPhoto?.setOnClickListener {
            // заменить на showBottomSheetMultiSelectionImagePickerDialog
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

        act.permissionListener.add(listener)
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
        act.permissionListener.remove(listener)

        super.onDestroyView()
    }

    override fun onStartFragment() {
        super.onStartFragment()
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
        com.meera.core.dialogs.ConfirmDialogBuilder()
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

    private fun openMediaPickerWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadMultiImage(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            maxCount = 5,
            type = MediaControllerOpenPlace.Gallery,
            message = "",
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImagesUri = { images -> onImagesReady(images) },
                onReadyImagesUriWithText = { images, _ -> onImagesReady(images) }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK
        )
    }

    private fun initRecycler() {
        // Adapter
        adapter = ProfilePhotoAdapter(resources.displayMetrics, this)

        val columns = 3 //Columns count
        val layoutManager = GridLayoutManager(context, columns)


        //Need to show headers( if type full view will fill 3 cells )
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter?.getItemViewType(position)) {
                    ProfilePhotoAdapter.TYPE_FULL -> 3
                    else -> 1
                }
            }
        }

        photoRecycler?.layoutManager = layoutManager
        photoRecycler?.adapter = adapter

    }

    private fun showLoadingIndicator() {
        binding?.toolbar?.pbTbGridProfile?.visible()
        binding?.toolbar?.ivAddPhoto?.gone()
    }

    private fun hideLoadingIndicator() {
        binding?.toolbar?.pbTbGridProfile?.gone()
        binding?.toolbar?.ivAddPhoto?.visible()
    }

    private fun initObservers() {
        //when photo clicked starting new fragment
        viewModel.liveStartImageEventSingle.observe(viewLifecycleOwner, Observer {
            openPhotoViewerScreen(it)
        })

        viewModel.pagedListLiveData.observe(viewLifecycleOwner, Observer {
            binding?.srlGridProfile?.isRefreshing = false
            Timber.d("Grid observed: ${it.size}")
            adapter?.submitList(it)
            Timber.d("Grid Adapter: ${adapter?.itemCount}")
        })

        viewModel.liveErrorEvent.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is GridProfilePhotoEvent.OnErrorSocket -> showNToastError()
                is GridProfilePhotoEvent.OnCloseGalleryScreen -> act.onBackPressed()
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

            }
        })
    }

    private fun showNToastError() {
        NToast.with(view)
            .text(getString(R.string.error_try_later))
            .show()
        Timber.e(" showNToastError")
    }

    private fun showErrorWhilePhotoUpload() {
        NToast.with(view)
            .text(getString(R.string.error_while_loading_photo))
            .show()
    }

    private fun openPhotoViewerScreen(position: Int) {
        val fragment = ProfilePhotoViewerFragment()

        fragment.setOnPhotoDeletedCallback {
            isPhotoDeletedInChildFragment = true
        }
        act.addFragment(
            fragment, Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(IArgContainer.ARG_GALLERY_POSITION, position),
            Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
            Arg(IArgContainer.ARG_IS_OWN_PROFILE, viewModel.getUserUid() == userID),
            Arg(IArgContainer.ARG_USER_ID, userID)
        )
    }

    private fun initView() {
        photoRecycler = binding?.rvGridPhoto
        title = binding?.toolbar?.tvGridPhotoToolbar
        backBtn = binding?.toolbar?.tbBtnBack

        backBtn?.setOnClickListener { act.onBackPressed() }
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
