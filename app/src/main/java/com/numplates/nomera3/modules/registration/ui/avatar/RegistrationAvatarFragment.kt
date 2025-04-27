package com.numplates.nomera3.modules.registration.ui.avatar

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlideRoundedCorner
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.referrals.ui.PopUpGetVipDialogFragment
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.Act
import com.numplates.nomera3.AnimatedAvatarUtils
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationAvatarBinding
import com.numplates.nomera3.modules.avatar.ContainerAvatarFragment
import com.numplates.nomera3.modules.registration.data.RegistrationUserData.Companion.GENDER_MALE
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_AVATAR_STATE
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RegistrationAvatarFragment :
    BaseFragmentNew<FragmentRegistrationAvatarBinding>(),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    TedBottomSheetPermissionActionsListener {

    private val viewModel by viewModels<RegistrationAvatarViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )
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

    override fun onStart() {
        super.onStart()
        viewModel.initUserData()
    }

    override fun onStop() {
        super.onStop()
        uniqueNameDisposable.clear()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        binding?.vAvatarView?.startParallaxEffect()
        act.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onStopFragment() {
        super.onStopFragment()
        binding?.vAvatarView?.stopParallaxEffect()
        act.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
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
        viewModel.progressLiveData.observe(viewLifecycleOwner) {
            handleProgress(it)
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
        binding?.vAvatarView?.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
        binding?.vAvatarView?.startParallaxEffect()
        viewModel.avatarSet()
        setAvatarButtonsTexts()
        handleAvatarProgress(false)
        binding?.userPhoto?.gone()
        binding?.vAvatarView?.visible()
        enableContinueButtonIfAllowed()
    }

    private fun initView() {
        binding?.cvNextButton?.click {
            it.clickAnimateScaleUp()
            viewModel.uploadProfileData()
        }
        binding?.ivBackIcon?.click {
            navigationViewModel.goBack()
        }
        act.supportFragmentManager.setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR, viewLifecycleOwner
        ) { _, bundle ->
            // get json state
            val avatarState: String =
                bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
            binding?.vAvatarView?.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
            binding?.vAvatarView?.startParallaxEffect()
        }
        handleAvatarProgress(true)
        binding?.ivGenerateAvatar?.click {
            binding?.vAvatarView?.clear()
            viewModel.generateRandomAvatar()
            handleAvatarProgress(true)
        }
        binding?.tvAddPhotoButton?.click {
            openMediaPicker()
        }
        binding?.tvEditAvatarButton?.click {
            openAvatarCreator()
        }
        binding?.referralContainer?.click {
            showVipDialog()
        }
        setContinueButtonAvailability(false)
        binding?.cvNextButton?.setFinishButton()
        binding?.tvStep?.text = getString(R.string.registration_step_count, STEP)
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
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri -> openImageEditor(imageUri) },
                onDismissPicker = {
                    viewModel.closedMediaPicker()
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
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
                        NToast.with(view)
                            .typeError()
                            .text(getString(R.string.error_editing_media))
                            .show()
                    }
                    enableContinueButtonIfAllowed()
                    nmrAmplitude?.let(viewModel::onAvatarEdits)
                }
            }
        )
        /*
        amplitudeEditorParams = AmplitudeEditorParams(
        where = AmplitudeEditorWhereProperty.OTHER,
        automaticOpen = true
        )
         */
    }

    private fun showPhoto(path: String) {
        viewModel.avatarSet()
        viewModel.setAvatarAnimationPhoto(null)
        binding?.userPhoto?.visible()
        binding?.vAvatarView?.gone()
        binding?.userPhoto?.loadGlideRoundedCorner(path, 8)
        viewModel.setUserPhoto(path)
        viewModel.setAvatarAnimation(null)
        handleAvatarProgress(false)
        setPhotoButtonsTexts()
        enableContinueButtonIfAllowed()
        binding?.ivGenerateAvatar?.gone()
    }

    private fun openAvatarCreator() {
        val avatarState = if (avatarAnimation.isNullOrEmpty()) {
            if (viewModel.getGender() == GENDER_MALE) AnimatedAvatarUtils.DEFAULT_MALE_STATE
            else AnimatedAvatarUtils.DEFAULT_FEMALE_STATE
        } else {
            avatarAnimation ?: ""
        }
        Timber.i("UserPersonal AvatarState:${avatarState}")
        add(ContainerAvatarFragment(), Act.LIGHT_STATUSBAR, Arg(ARG_AVATAR_STATE, avatarState))
        observeAvatarChange()
        viewModel.avatarEditorStarted()
    }

    private fun observeAvatarChange() {
        act.supportFragmentManager.setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR,
            viewLifecycleOwner
        ) { _, bundle ->
            // get json state
            val avatarState: String =
                bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
            viewModel.setAvatarAnimation(avatarState)
            viewModel.saveAvatarInFile(avatarState)
            viewModel.setAvatarState(avatarState)
            avatarAnimation = avatarState
            binding?.vAvatarView?.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
            binding?.vAvatarView?.startParallaxEffect()
            setAvatarButtonsTexts()
            viewModel.setUserPhoto(null)
            viewModel.avatarEditorFinished()
            handleAvatarProgress(false)
            binding?.userPhoto?.gone()
            binding?.vAvatarView?.visible()
            enableContinueButtonIfAllowed()
        }
    }

    private fun setAvatarButtonsTexts() {
        binding?.tvAddPhotoButton?.text = context?.string(R.string.add_photo_short)
        binding?.tvEditAvatarButton?.text = context?.string(R.string.edit_avatar)
    }

    private fun setPhotoButtonsTexts() {
        binding?.tvAddPhotoButton?.text = getString(R.string.select_other_photo)
        binding?.tvEditAvatarButton?.text = getString(R.string.add_avatar)
    }

    private fun handleAvatarProgress(inProgress: Boolean) {
        setContinueButtonAvailability(!inProgress)
        binding?.ivGenerateAvatar?.isEnabled = !inProgress
        binding?.tvAddPhotoButton?.isEnabled = !inProgress
        binding?.tvEditAvatarButton?.isEnabled = !inProgress
        if (inProgress) {
            binding?.ivGenerateAvatar?.gone()
            binding?.vAvatarView?.invisible()
            binding?.tvAddPhotoButton?.invisible()
            binding?.tvEditAvatarButton?.invisible()
            binding?.avatarProgressBar?.visible()
        } else {
            binding?.ivGenerateAvatar?.visible()
            binding?.tvAddPhotoButton?.visible()
            binding?.tvEditAvatarButton?.visible()
            binding?.avatarProgressBar?.gone()
        }
    }

    private fun initUniqueName(uniqueName: String?) {
        binding?.etUniqueName?.let { editText ->
            editText.doAfterTextChanged { _ ->
                if (editText.lineCount > 1) {
                    context?.hideKeyboard(requireView())
                    editText.text?.delete(editText.selectionEnd - 1, editText.selectionStart)
                }
                uniqueNameDisposable.add(
                    RxTextView.textChanges(editText)
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
            editText.setText(uniqueName)
            binding?.llUniqueName?.click { editText.requestFocus() }
            editText.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) context?.hideKeyboard(requireView())
            }
            binding?.etUniqueName?.clearFocus()
        }
        enableContinueButtonIfAllowed()
    }

    private fun showUniqueNameError(error: String?) {
        if (error.isNullOrEmpty()) {
            binding?.llUniqueName?.background?.setTint(requireContext().getColorCompat(R.color.tale_white))
            binding?.tvUniqueNameError?.text = error
            binding?.tvUniqueNameError?.gone()
        } else {
            binding?.llUniqueName?.background?.setTint(requireContext().getColorCompat(R.color.wrong_code_color))
            binding?.tvUniqueNameError?.text = error
            binding?.tvUniqueNameError?.visible()
        }
    }

    private fun showVipDialog() {
        val vipDialog = PopUpGetVipDialogFragment()
        vipDialog.show(act.supportFragmentManager, "check_code_dialog")
        vipDialog.successCheckCodeCallback = { code: String ->
            viewModel.setReferralCode(code)
            binding?.ivReferralIcon?.setImageResource(R.drawable.ic_profile_crown_vip_new)
            binding?.ivReferralIcon?.imageTintList = null
            binding?.tvReferralText?.text = getString(R.string.vip_one_day_activated)
            binding?.referralContainer?.isClickable = false
            context?.hideKeyboard(requireView())
            vipDialog.dismiss()
        }
    }

    private fun enableContinueButtonIfAllowed() {
        setContinueButtonAvailability(
            viewModel.isUniqueNameValid() && !viewModel.isAvatarNotExist()
        )
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding?.cvNextButton?.isEnabled = enabled
    }

    private fun handleProgress(inProgress: Boolean) {
        binding?.cvNextButton?.showProgress(inProgress)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationAvatarBinding
        get() = FragmentRegistrationAvatarBinding::inflate

    companion object {
        const val STEP = "5"

        private const val UNIQUE_NAME_DEBOUNCE = 300L
    }
}
