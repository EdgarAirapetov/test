package com.numplates.nomera3.modules.registration.ui.avatar

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.input.BottomTextState
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.AnimatedAvatarUtils
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationAvatarBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.data.RegistrationUserData.Companion.GENDER_MALE
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val TAG_PHOTO_MENU = "PHOTO_MENU"
private const val HEIGHT_UNIQUE_NAME_INPUT = 44

class MeeraRegistrationAvatarFragment : MeeraBaseFragment(R.layout.meera_fragment_registration_avatar),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    TedBottomSheetPermissionActionsListener {

    private val viewModel by viewModels<RegistrationAvatarViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val binding by viewBinding(MeeraFragmentRegistrationAvatarBinding::bind)
    private val act by lazy { activity as MeeraAct }

    private val uniqueNameDisposable = CompositeDisposable()

    private var avatarAnimation: String? = null
    private var mediaPicker: TedBottomSheetDialogFragment? = null

    private val argAuthType by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_AUTH_TYPE) }
    private val argCountryNumber by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_COUNTRY_NUMBER) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initView()
        observeViewModel()
        viewModel.setAuthType(argAuthType)
        viewModel.setCountryNumber(argCountryNumber)

        act.permissionListener.add(listener)
    }

    val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, _, grantResults ->
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

    override fun onStart() {
        super.onStart()
        viewModel.initUserData()
    }

    override fun onStop() {
        super.onStop()
        uniqueNameDisposable.clear()
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
            .setHeader(getString(R.string.camera_settings_dialog_title))
            .setDescription(getString(R.string.camera_settings_dialog_description))
            .setBottomBtnText(getString(R.string.camera_settings_dialog_cancel))
            .setTopBtnText(getString(R.string.camera_settings_dialog_action))
            .setCancelable(false)
            .setTopClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    private fun observeViewModel() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            setContinueButtonAvailability(false)
            when (event) {
                is RegistrationAvatarViewEvent.SetUniqueName -> {
                    initUniqueName(event.uniqueName)
                }

                is RegistrationAvatarViewEvent.ShowAvatar -> {
                    setAvatarGenerated(event.avatarState)
                }

                is RegistrationAvatarViewEvent.ShowPhoto -> {
                    showPhoto(event.path)
                }

                is RegistrationAvatarViewEvent.UniqueNameNotAllowed -> {
                    showUniqueNameError(getString(R.string.unickname_disabled))
                }

                is RegistrationAvatarViewEvent.UniqueNameNotValid -> {
                    showUniqueNameError(getString(R.string.unickname_not_allowed))
                }

                is RegistrationAvatarViewEvent.UniqueNameAlreadyTaken -> {
                    showUniqueNameError(getString(R.string.unickname_taken))
                }

                is RegistrationAvatarViewEvent.UniqueNameValidated -> {
                    handleUniqueNameValidation(event.result)
                }

                is RegistrationAvatarViewEvent.UniqueNameValid -> {
                    showUniqueNameError(null)
                    enableContinueButtonIfAllowed()
                }

                is RegistrationAvatarViewEvent.FinishRegistration -> {
                    navigationViewModel.finishRegistration()
                }

                else -> {}
            }
        }
    }

    private fun handleUniqueNameValidation(result: UniqueUsernameValidationResult) {
        when (result) {
            is UniqueUsernameValidationResult.IsTooShort -> {
                showUniqueNameError(getString(R.string.unickname_too_short))
            }

            is UniqueUsernameValidationResult.IsTooLong -> {
                showUniqueNameError(getString(R.string.unickname_too_long))
            }

            is UniqueUsernameValidationResult.IsStartedByDot -> {
                showUniqueNameError(getString(R.string.unickname_started_by_dot))
            }

            is UniqueUsernameValidationResult.IsEndedByDot -> {
                showUniqueNameError(getString(R.string.unickname_ended_by_dot))
            }

            is UniqueUsernameValidationResult.IsTwoDotOneByOne -> {
                showUniqueNameError(getString(R.string.unickname_dots_row))
            }

            is UniqueUsernameValidationResult.IsNotAllowed -> {
                showUniqueNameError(getString(R.string.unickname_not_allowed))
            }

            is UniqueUsernameValidationResult.IsTookByAnotherUser -> {
                showUniqueNameError(getString(R.string.unickname_taken))
            }

            is UniqueUsernameValidationResult.IsEmpty -> {
                showUniqueNameError(getString(R.string.unickname_empty))
            }

            is UniqueUsernameValidationResult.IsValid -> {
                showUniqueNameError(null)
                enableContinueButtonIfAllowed()
            }

            else -> {}
        }
    }

    private fun setAvatarGenerated(avatarState: String) {
        avatarAnimation = avatarState
        viewModel.saveAvatarInFile(avatarState)
        binding.avAvatar.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
        binding.avAvatar.startParallaxEffect()
        viewModel.avatarSet()
        setAvatarButtonsTexts()
        handleAvatarProgress(false)
        binding.upiAvatar.gone()
        binding.avAvatar.visible()
        enableContinueButtonIfAllowed()
    }

    private fun initView() {
        binding.ukiUniqueName.etInput.apply {
            val layoutParams = layoutParams
            layoutParams.height = HEIGHT_UNIQUE_NAME_INPUT.dp
            this.layoutParams = layoutParams
        }
        binding.btnReady.setThrottledClickListener {
            viewModel.uploadProfileData()
        }
        binding.btnBack.setThrottledClickListener {
            navigationViewModel.goBack()
        }
        handleAvatarProgress(true)
        binding.btnAddPhoto.setThrottledClickListener { openMediaPicker() }
        binding.btnPhotoMenu.setThrottledClickListener {
            val photoCallback = object : MeeraSelectAvatarBottomSheetDialog.PhotoSelectorDismissListener {
                override fun selectPhoto() {
                    openMediaPicker()
                }

                override fun selectAvatar() {
                    openAvatarCreator()
                }

                override fun generateAvatar() {
                    binding.avAvatar.clear()
                    viewModel.generateRandomAvatar()
                    handleAvatarProgress(true)
                }
            }
            MeeraSelectAvatarBottomSheetDialog(photoCallback, MeeraSelectAvatarBottomSheetDialog.FromScreen.REGISTRATION)
                .show(childFragmentManager, TAG_PHOTO_MENU)
        }
        setContinueButtonAvailability(false)
    }

    private fun openMediaPicker() {
        if (viewModel.isMediaPickerOpened()) return
        viewModel.openMediaPickerClicked()
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

    private fun openMediaPickerWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            needWithVideo = false,
            showGifs = true,
            suggestionsMenu = SuggestionsMenu(parentFragment ?: this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri -> openImageEditor(imageUri) },
                onDismissPicker = {
                    viewModel.closedMediaPicker()
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK
        )
    }

    private fun openImageEditor(selectedImageUri: Uri) {
        viewModel.closedMediaPicker()
        act.openPhotoEditorForProfile(
            uri = selectedImageUri,
            listener = object : MediaControllerCallback {
                override fun onPhotoReady(
                    resultUri: Uri,
                    nmrAmplitude: NMRPhotoAmplitude?
                ) {
                    val editedImagePath = resultUri.path
                    if (editedImagePath != null) {
                        showPhoto(editedImagePath)
                    } else {
                        UiKitSnackBar.make(
                            requireView(), SnackBarParams(
                                SnackBarContainerUiState(
                                    avatarUiState = AvatarUiState.ErrorIconState,
                                    messageText = getText(R.string.error_editing_media)
                                )
                            )
                        ).show()
                    }
                    enableContinueButtonIfAllowed()
                    nmrAmplitude?.let(viewModel::onAvatarEdits)
                }
            }
        )
    }

    private fun showPhoto(path: String) {
        viewModel.avatarSet()
        viewModel.setAvatarAnimationPhoto(null)
        binding.upiAvatar.visible()
        binding.avAvatar.gone()
        binding.upiAvatar.setConfig(UserpicUiModel(userAvatarUrl = path))
        viewModel.setUserPhoto(path)
        viewModel.setAvatarAnimation(null)
        handleAvatarProgress(false)
        setPhotoButtonsTexts()
        enableContinueButtonIfAllowed()
    }

    private fun openAvatarCreator() {
        val avatarState = if (avatarAnimation.isNullOrEmpty()) {
            if (viewModel.getGender() == GENDER_MALE) AnimatedAvatarUtils.DEFAULT_MALE_STATE
            else AnimatedAvatarUtils.DEFAULT_FEMALE_STATE
        } else {
            avatarAnimation ?: ""
        }
        navigationViewModel.registrationCreateAvatar(avatarState = avatarState)
        observeAvatarChange()
        viewModel.avatarEditorStarted()
    }

    private fun observeAvatarChange() {
        setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR
        ) { _, bundle ->
            val avatarState: String =
                bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
            viewModel.setAvatarAnimation(avatarState)
            viewModel.saveAvatarInFile(avatarState)
            viewModel.setAvatarState(avatarState)
            avatarAnimation = avatarState
            binding.avAvatar.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
            binding.avAvatar.startParallaxEffect()
            setAvatarButtonsTexts()
            viewModel.setUserPhoto(null)
            viewModel.avatarEditorFinished()
            handleAvatarProgress(false)
            binding.upiAvatar.gone()
            binding.avAvatar.visible()
            enableContinueButtonIfAllowed()
        }
    }

    private fun setAvatarButtonsTexts() {
        binding.btnAddPhoto.text = context?.string(R.string.add_photo_short)
        binding.btnAddPhoto.buttonType = ButtonType.FILLED
    }

    private fun setPhotoButtonsTexts() {
        binding.btnAddPhoto.text = getString(R.string.select_other_photo)
        binding.btnAddPhoto.buttonType = ButtonType.OUTLINE
    }

    private fun handleAvatarProgress(inProgress: Boolean) {
        setContinueButtonAvailability(!inProgress)
        binding.btnAddPhoto.isEnabled = !inProgress
        binding.btnPhotoMenu.isEnabled = !inProgress
        binding.avatarProgressBar.isVisible = inProgress
    }

    private fun initUniqueName(uniqueName: String?) {
        binding.ukiUniqueName.let { input ->
            input.doAfterSearchTextChanged {
                if (input.etInput.lineCount > 1) {
                    context?.hideKeyboard(requireView())
                    input.etInput.text.delete(input.etInput.selectionEnd - 1, input.etInput.selectionStart)
                }
                uniqueNameDisposable.add(
                    RxTextView.textChanges(input.etInput)
                        .debounce(UNIQUE_NAME_DEBOUNCE, TimeUnit.MILLISECONDS)
                        .map { it.toString() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { viewModel.validateUserName(it) },
                            { Timber.e(it) }
                        )
                )
            }
            input.etInput.setText(uniqueName)
            input.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) context?.hideKeyboard(requireView())
            }
            input.etInput.clearFocus()
        }
        enableContinueButtonIfAllowed()
    }

    private fun showUniqueNameError(error: String?) {
        val state = when {
            error.isNullOrEmpty() -> BottomTextState.Empty
            else -> BottomTextState.Error(errorText = error)
        }
        binding.ukiUniqueName.setBottomTextState(state)
    }

    private fun enableContinueButtonIfAllowed() {
        setContinueButtonAvailability(
            viewModel.isUniqueNameValid() && !viewModel.isAvatarNotExist()
        )
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding.btnReady.isEnabled = enabled
    }

    companion object {
        private const val UNIQUE_NAME_DEBOUNCE = 300L
    }
}
