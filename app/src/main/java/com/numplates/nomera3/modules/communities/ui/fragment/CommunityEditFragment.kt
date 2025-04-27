package com.numplates.nomera3.modules.communities.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTintColor
import com.meera.core.extensions.toInt
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentEditGroupRedesignedBinding
import com.numplates.nomera3.databinding.FragmentEditGroupRedesignedDescriptionFieldBinding
import com.numplates.nomera3.databinding.FragmentEditGroupRedesignedNameFieldBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCanWrite
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHavePhoto
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment.MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_EMPTY
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment.MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_TOO_LONG
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment.MandatoryGroupDataValidationResult.GROUP_NAME_EMPTY
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment.MandatoryGroupDataValidationResult.GROUP_NAME_TOO_LONG
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment.MandatoryGroupDataValidationResult.GROUP_NAME_TOO_SHORT
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityEditFragment.MandatoryGroupDataValidationResult.SUCCESS
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.GroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.GroupEditViewEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.properties.Delegates

const val DELAY_BEFORE_START_CHECK_MANDATORY_FIELDS = 300L

class CommunityEditFragment :
    BaseFragmentNew<FragmentEditGroupRedesignedBinding>(),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    IOnBackPressed,
    TedBottomSheetPermissionActionsListener {

    var refreshCallback: (() -> Unit)? = null

    private val groupName: String
        get() = groupNameBinding?.etNameInput?.text?.toString()?.trim().orEmpty()

    private val groupNameHeaderTextView: TextView?
        get() = groupNameBinding?.tvNameHeader

    private val groupNameErrorTextView: TextView?
        get() = groupNameBinding?.tvNameError

    private val groupDescription: String
        get() = groupDescBinding?.etDescriptionInput?.text?.toString()?.trim().orEmpty()

    private val groupDescriptionHeaderTextView: TextView?
        get() = groupDescBinding?.tvDescriptionHeader

    private val groupDescriptionErrorTextView: TextView?
        get() = groupDescBinding?.tvDescriptionError

    private val communityExtraSettingsContainerView: View?
        get() = binding?.communityExtraSettingsContainer

    private var groupNameBinding: FragmentEditGroupRedesignedNameFieldBinding? = null
    private var groupDescBinding: FragmentEditGroupRedesignedDescriptionFieldBinding? = null

    private val viewModel by viewModels<GroupEditViewModel>()

    private var groupId: Int? = null
    private var selectedGroupImagePath: String = String.empty()
    private var enableSaveButtonJob: Job? = null
    private var isClosing = false

    private var currentGroupData: GroupData = createEmptySettings()
    private var defaultOrPreviousGroupData: GroupData = createEmptySettings()

    private var mediaPicker: TedBottomSheetDialogFragment? = null

    private var isPrivateGroupObservable: Boolean by Delegates.observable(true) { _, _, newValue ->
        if (currentGroupData.isPrivate && !newValue) showSetNotPrivateAgreementDialog()
        else setPrivateCommunity(newValue)
    }

    private fun showSetNotPrivateAgreementDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.community_set_not_private_dialog_header))
            .setDescription(getString(R.string.community_set_not_private_dialog_description))
            .setLeftBtnText(getString(R.string.community_set_not_private_dialog_positive))
            .setRightBtnText(getString(R.string.community_set_not_private_dialog_negative))
            .setLeftClickListener { setPrivateCommunity(false) }
            .setRightClickListener { /* dismiss */ }
            .show(childFragmentManager)
    }

    private fun setPrivateCommunity(newValue: Boolean) {
        currentGroupData.isPrivate = newValue
        updateOpenPrivateGroupOptionCheckboxList(newValue)
        tryEnableSaveButton()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEditGroupRedesignedBinding =
        { l, _, _ ->
            FragmentEditGroupRedesignedBinding.inflate(l)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.injectDependencies()
    }

    override fun onStart() {
        super.onStart()
        binding?.statusBar?.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initBindings()
        groupId = arguments
            ?.getInt(IArgContainer.ARG_GROUP_ID)
            ?.takeIf { it > 0 }

        viewModel.isCreatorAppearanceMode = arguments
            ?.getBoolean(IArgContainer.ARG_IS_GROUP_CREATOR, false)
            ?: false

        initViews()
        initLiveObservables()

        if (groupId != null) {
            viewModel.getGroupInfo(groupId)
        } else {
            groupNameBinding?.etNameInput?.doAfterTextChanged {
                tryEnableSaveButton()
            }

            groupDescBinding?.etDescriptionInput?.doAfterTextChanged {
                tryEnableSaveButton()
            }
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
        isClosing = false
        onShowHints()
        doAnalyticsLog()
    }

    override fun onBackPressed(): Boolean {
        return if (!isClosing) {
            backClicked()
            true
        } else {
            false
        }
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

    private fun backClicked() {
        if (hasUnsavedChanges()) showUnsavedChangesDialog()
        else closeEditor()
    }

    private fun closeEditor() {
        isClosing = true
        context?.hideKeyboard(requireView())
        act.onBackPressed()
        clearData()
    }

    private fun initViews() {
        if (groupId != null) {
            binding?.title?.text = getString(R.string.group_edit_fragment_title_existing_group)
        } else {
            binding?.title?.text = getString(R.string.group_edit_fragment_title_new_group)
        }

        binding?.closeButton?.click {
            backClicked()
        }

        context?.also { binding?.confirmButton?.drawable?.setTintColor(it, R.color.ui_gray) }
        binding?.confirmButtonContainer?.isEnabled = false
        binding?.confirmButtonContainer?.click {
            saveGroup()
        }

        binding?.addGroupPhotoView?.setThrottledClickListener {
            openImagePicker {
                tryEnableSaveButton()
            }
        }

        binding?.groupImageContainer?.setThrottledClickListener {
            openImagePicker {
                tryEnableSaveButton()
            }
        }

        // groupName
        groupNameBinding?.etNameInput?.filters = arrayOf(InputFilter.LengthFilter(MAX_NAME_LENGTH))
        groupNameBinding?.etNameInput?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                groupNameBinding?.tvNameTypedCharCount?.text = getGroupNameEditTextLength().toString()
                groupNameBinding?.vgNameTypedCharCountContainer?.visible()
            } else {
                groupNameBinding?.vgNameTypedCharCountContainer?.invisible()
            }
        }

        groupNameBinding?.etNameInput?.doAfterTextChanged {
            if (groupNameBinding?.vgNameTypedCharCountContainer?.isVisible == true) {
                groupNameBinding?.tvNameTypedCharCount?.text = getGroupNameEditTextLength().toString()
            }

            if (groupNameErrorTextView?.isVisible == true) {
                groupNameErrorTextView?.invisible()
                if (groupNameHeaderTextView?.isVisible == false) {
                    groupNameHeaderTextView?.visible()
                }
            }

            currentGroupData.name = groupName
        }

        // groupDescription
        groupDescBinding?.etDescriptionInput?.filters = arrayOf(
            InputFilter
                .LengthFilter(MAX_DESCRIPTION_LENGTH)
        )
        groupDescBinding?.etDescriptionInput?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                groupDescBinding?.tvDescriptionTypedCharCount?.text =
                    getGroupDescriptionEditTextLength().toString()
                groupDescBinding?.vgDescriptionTypedCharCountContainer?.visible()
            } else {
                groupDescBinding?.vgDescriptionTypedCharCountContainer?.invisible()
            }
        }

        groupDescBinding?.etDescriptionInput?.doAfterTextChanged {
            if (groupDescBinding?.vgDescriptionTypedCharCountContainer?.isVisible == true) {
                groupDescBinding?.tvDescriptionTypedCharCount?.text =
                    getGroupDescriptionEditTextLength().toString()
            }

            if (groupDescriptionErrorTextView?.isVisible == true) {
                groupDescriptionErrorTextView?.invisible()
                if (groupDescriptionHeaderTextView?.isVisible == false) {
                    groupDescriptionHeaderTextView?.visible()
                }
            }

            currentGroupData.description = groupDescription
        }

        binding?.openGroupTypeOptionInclude?.openGroupOptionCheckbox?.isChecked = true
        binding?.openGroupTypeOptionInclude?.root?.click {
            isPrivateGroupObservable = false
        }

        binding?.closeGroupTypeOptionInclude?.closeGroupOptionCheckbox?.isChecked = false
        binding?.closeGroupTypeOptionInclude?.root?.click {
            isPrivateGroupObservable = true
        }

        binding?.writeOnlyAdminOptionSwitch?.setOnCheckedChangeListener { _, newIsWriteOnlyAdmin ->
            if (currentGroupData.name.isNotEmpty()) {
                if (currentGroupData.isWriteOnlyAdmin != newIsWriteOnlyAdmin) {
                    currentGroupData.isWriteOnlyAdmin = newIsWriteOnlyAdmin
                    tryEnableSaveButton()
                } else {
                    setConfirmButtonEnable(false)
                }
            }
        }

        if (!viewModel.isCreatorAppearanceMode) {
            communityExtraSettingsContainerView?.gone()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        return when {
            defaultOrPreviousGroupData.name != currentGroupData.name -> true
            defaultOrPreviousGroupData.description != currentGroupData.description -> true
            defaultOrPreviousGroupData.isPrivate != currentGroupData.isPrivate -> true
            defaultOrPreviousGroupData.isWriteOnlyAdmin != currentGroupData.isWriteOnlyAdmin -> true
            selectedGroupImagePath.isNotEmpty() -> true
            else -> false
        }
    }

    private fun tryEnableSaveButton() {
        enableSaveButtonJob?.cancel()
        enableSaveButtonJob = lifecycleScope.launchWhenResumed {
            delay(DELAY_BEFORE_START_CHECK_MANDATORY_FIELDS)
            val isMandatoryFieldsNotEmpty = isMandatoryFieldsNotEmpty()
            setConfirmButtonEnable(isMandatoryFieldsNotEmpty)
        }
    }

    private fun setConfirmButtonEnable(isMandatoryFieldsNotEmpty: Boolean) {
        binding?.confirmButtonContainer?.isEnabled = isMandatoryFieldsNotEmpty
        context?.also {
            val colorResId =
                if (isMandatoryFieldsNotEmpty) R.color.ui_blue_purple else R.color.ui_gray
            binding?.confirmButton?.drawable?.setTintColor(it, colorResId)
        }
    }

    private fun isMandatoryFieldsNotEmpty(): Boolean {
        return getGroupNameEditTextLength() > 0 &&
            getGroupDescriptionEditTextLength() > 0 &&
            hasUnsavedChanges()
    }

    private fun getGroupNameEditTextLength(): Int {
        return groupNameBinding?.etNameInput?.text?.length ?: 0
    }

    private fun getGroupDescriptionEditTextLength(): Int {
        return groupDescBinding?.etDescriptionInput?.text?.length ?: 0
    }

    private fun initLiveObservables() {
        viewModel.liveGroupInfo.observe(viewLifecycleOwner, { group ->
            showGroupInfo(group.community)
        })

        viewModel.liveViewEvent.observe(viewLifecycleOwner, { viewEvent ->
            handleEvents(viewEvent)
        })
    }

    private fun showGroupImageOrPlaceholder(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            binding?.groupDefaultImageContainer?.invisible()
            binding?.groupImageView?.visible()
            binding?.groupImageView?.also {
                Glide.with(act)
                    .load(imageUrl)
                    .into(it)
            }
        } else {
            binding?.groupDefaultImageContainer?.visible()
            binding?.groupImageView?.invisible()
        }

        binding?.addGroupPhotoView?.text = if (imageUrl != null) getString(R.string.change)
        else getString(R.string.general_add)
    }

    private fun updateOpenPrivateGroupOptionCheckboxList(isPrivate: Boolean) {
        binding?.openGroupTypeOptionInclude?.openGroupOptionCheckbox?.isChecked = !isPrivate
        binding?.closeGroupTypeOptionInclude?.closeGroupOptionCheckbox?.isChecked = isPrivate
    }

    private fun showGroupInfo(communityEntity: CommunityEntity?) {
        communityEntity?.also { entity ->
            defaultOrPreviousGroupData.name = entity.caption ?: ""
            defaultOrPreviousGroupData.description = entity.description ?: ""
            defaultOrPreviousGroupData.isPrivate = entity.private == 1
            defaultOrPreviousGroupData.isWriteOnlyAdmin = entity.royalty == 1

            currentGroupData = defaultOrPreviousGroupData.copy()

            showGroupImageOrPlaceholder(entity.avatar)
            groupNameBinding?.etNameInput?.setText(entity.caption)
            groupDescBinding?.etDescriptionInput?.setText(entity.description)
            binding?.writeOnlyAdminOptionSwitch?.isChecked = entity.royalty == 1
            updateOpenPrivateGroupOptionCheckboxList(entity.private == 1)
        }

        groupNameBinding?.etNameInput?.doAfterTextChanged {
            tryEnableSaveButton()
        }

        groupDescBinding?.etDescriptionInput?.doAfterTextChanged {
            tryEnableSaveButton()
        }
    }

    private fun handleEvents(event: GroupEditViewEvent) {
        when (event) {
            is GroupEditViewEvent.SuccessGroupCreate -> {
                logAmplitudeCommunityCreated()
                refreshCallback?.invoke()
                hideProgress()
                deleteTemporaryGroupAvatar()
                isClosing = true
                replace(
                    act.getFragmentsCount() - 1,
                    CommunityRoadFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(ARG_GROUP_ID, event.groupId)
                )
            }

            is GroupEditViewEvent.FailureGroupCreate -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(getString(R.string.group_error_create_group))
            }

            is GroupEditViewEvent.FailureGroupCreateExist -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(getString(R.string.group_error_create_group_exist))
            }

            is GroupEditViewEvent.SuccessGroupEdit -> {
                refreshCallback?.invoke()
                hideProgress()
                deleteTemporaryGroupAvatar()
                closeEditor()
            }

            is GroupEditViewEvent.FailureGroupEdit -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(getString(R.string.group_error_edit_group))
            }

            is GroupEditViewEvent.FailureGroupEditExist -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(getString(R.string.group_error_edit_group_exist))
            }

            is GroupEditViewEvent.SuccessGroupDeleted -> {
                refreshCallback?.invoke()
                hideProgress()
                deleteTemporaryGroupAvatar()
                if (act.getFragmentsCount() >= 3) {
                    act.returnToTargetFragment(act.getFragmentsCount() - 3, true)
                } else {
                    act.onBackPressed()
                }
            }

            is GroupEditViewEvent.FailureGroupDeleted -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(getString(R.string.group_error_delete_group))
            }

            is GroupEditViewEvent.ErrorNameSizeMoreThenTree -> {
                hideProgress()
                showAlertMessage(getString(R.string.community_name_should_be_longer_than_three))
            }

            GroupEditViewEvent.NoInternetConnection -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(getString(R.string.no_internet))
            }
        }
    }

    private fun logAmplitudeCommunityCreated() {
        val (isPrivateGroup, isWriteOnlyAdmin) = getExtraSettings()

        val communityType = if (isPrivateGroup == 0)
            AmplitudePropertyCommunityType.OPEN
        else AmplitudePropertyCommunityType.CLOSED

        val canWrite = if (isWriteOnlyAdmin == 0)
            AmplitudePropertyCanWrite.ALL
        else AmplitudePropertyCanWrite.ADMIN

        val havePhoto = if (selectedGroupImagePath.isNotEmpty())
            AmplitudePropertyHavePhoto.YES
        else AmplitudePropertyHavePhoto.NO

        viewModel.analyticsInteractor.logCommunityCreated(
            type = communityType,
            whoCanWrite = canWrite,
            havePhoto = havePhoto
        )
    }

    enum class MandatoryGroupDataValidationResult {
        GROUP_NAME_EMPTY,
        GROUP_NAME_TOO_SHORT,
        GROUP_NAME_TOO_LONG,
        GROUP_DESCRIPTION_EMPTY,
        GROUP_DESCRIPTION_TOO_LONG,
        SUCCESS
    }

    private fun validateMandatoryGroupData(
        groupName: String?,
        groupDescription: String?
    ): MandatoryGroupDataValidationResult {
        if (groupName.isNullOrEmpty()) return GROUP_NAME_EMPTY
        if (groupName.isNullOrBlank()) return GROUP_NAME_EMPTY
        if (groupName.trim().length < 3) return GROUP_NAME_TOO_SHORT
        if (groupName.trim().length > 45) return GROUP_NAME_TOO_LONG

        if (groupDescription.isNullOrEmpty()) return GROUP_DESCRIPTION_EMPTY
        if (groupDescription.isNullOrBlank()) return GROUP_DESCRIPTION_EMPTY
        if (groupName.trim().length > 500) return GROUP_DESCRIPTION_TOO_LONG

        return SUCCESS
    }

    private fun showGroupNameError(text: String) {
        groupNameErrorTextView?.text = text

        groupNameHeaderTextView?.invisible()
        groupNameErrorTextView?.visible()
    }

    private fun showGroupDescriptionError(text: String) {
        groupDescriptionErrorTextView?.text = text

        groupDescriptionHeaderTextView?.invisible()
        groupDescriptionErrorTextView?.visible()
    }

    private fun saveGroup() {
        when (validateMandatoryGroupData(groupName, groupDescription)) {
            // ошибки для поля название сообщества
            GROUP_NAME_EMPTY -> showGroupNameError(getString(R.string.group_name_is_mandatory))
            GROUP_NAME_TOO_LONG -> showGroupNameError(getString(R.string.group_name_is_too_long))
            GROUP_NAME_TOO_SHORT -> showGroupNameError(getString(R.string.group_name_is_too_short))
            // ошибки для поля описание сообщества
            GROUP_DESCRIPTION_EMPTY -> showGroupDescriptionError(getString(R.string.group_description_is_mandatory))
            GROUP_DESCRIPTION_TOO_LONG -> showGroupDescriptionError(getString(R.string.group_description_is_too_long))
            // всё ок
            SUCCESS -> {
                val (isPrivateGroup, isWriteOnlyAdmin) = getExtraSettings()
                val groupNameTrimmed = groupName.trim()
                val groupDescriptionTrimmed = groupDescription.trim()

                if (groupId != null && groupId != 0) {
                    editGroup(
                        groupNameTrimmed,
                        groupDescriptionTrimmed,
                        isPrivateGroup,
                        isWriteOnlyAdmin
                    )
                } else {
                    createGroup(
                        groupNameTrimmed,
                        groupDescriptionTrimmed,
                        isPrivateGroup,
                        isWriteOnlyAdmin
                    )
                }

                showProgress()
            }
        }
    }

    private fun getExtraSettings(): Pair<Int, Int> {
        if (viewModel.isCreatorAppearanceMode) {
            val isPrivateGroup = binding?.closeGroupTypeOptionInclude?.closeGroupOptionCheckbox
                ?.isChecked
                ?.toInt()
                ?: 0

            val isWriteOnlyAdmin = binding?.writeOnlyAdminOptionSwitch?.isChecked?.toInt() ?: 0

            return Pair(isPrivateGroup, isWriteOnlyAdmin)
        } else {
            return Pair(
                currentGroupData.isPrivate.toInt(),
                currentGroupData.isWriteOnlyAdmin.toInt()
            )
        }
    }

    private fun createGroup(
        groupName: String,
        groupDescription: String,
        isPrivateGroup: Int,
        isWriteOnlyAdmin: Int
    ) {
        viewModel.createGroup(
            groupName,
            groupDescription,
            isPrivateGroup,
            isWriteOnlyAdmin,
            selectedGroupImagePath
        )
    }

    private fun editGroup(
        groupName: String,
        groupDescription: String,
        isPrivateGroup: Int,
        isWriteOnlyAdmin: Int
    ) {
        groupId?.also {
            viewModel.editGroup(
                it,
                groupName,
                groupDescription,
                isPrivateGroup,
                isWriteOnlyAdmin,
                selectedGroupImagePath,
                false
            )
        }
    }

    private fun openImagePicker(onComplete: (String?) -> Unit) {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    openImagePickerWithPermissionState(PermissionState.GRANTED, onComplete)
                }

                override fun onDenied() {
                    openImagePickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED, onComplete)
                }

                override fun needOpenSettings() {
                    openImagePickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS, onComplete)
                }

                override fun onError(error: Throwable?) {
                    openImagePickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS, onComplete)
                }
            }
        )
    }

    private fun openImagePickerWithPermissionState(permissionState: PermissionState, onComplete: (String?) -> Unit) {
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            cameraType = com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri ->
                    if (imageUri != null) {
                        openPhotoEditorForCommunity(imageUri, onComplete)
                    } else {
                        onComplete.invoke(null)
                    }
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK
        )
    }

    private fun openPhotoEditorForCommunity(selectedImageUri: Uri, onComplete: (String?) -> Unit) {
        act.openPhotoEditorForCommunity(
            uri = selectedImageUri,
            listener = object : MediaControllerCallback {
                override fun onPhotoReady(
                    resultUri: Uri,
                    nmrAmplitude: NMRPhotoAmplitude?
                ) {
                    val editedImagePath = resultUri.path
                    if (editedImagePath != null) {
                        selectedGroupImagePath = editedImagePath
                        showGroupImageOrPlaceholder(editedImagePath)
                            .also { onComplete.invoke(editedImagePath) }
                        nmrAmplitude?.let(viewModel::logPhotoEdits)
                    } else {
                        onComplete.invoke(null)
                        NToast.with(view)
                            .typeError()
                            .text(getString(R.string.error_editing_media))
                            .show()
                    }
                }
            }
        )
    }

    private fun showUnsavedChangesDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.group_edit_fragment_unsaved_changes_dialog_title))
            .setDescription(getString(R.string.group_edit_fragment_unsaved_changes_dialog_message))
            .setLeftBtnText(getString(R.string.general_cancel))
            .setRightBtnText(getString(R.string.general_exit))
            .setLeftClickListener { /* dismiss */ }
            .setRightClickListener { closeEditor() }
            .show(childFragmentManager)
    }

    private fun clearData() {
        defaultOrPreviousGroupData = createEmptySettings()
        currentGroupData = createEmptySettings()
        groupNameBinding?.etNameInput?.setText(String.empty())
        groupDescBinding?.etDescriptionInput?.setText(String.empty())
    }

    private fun createEmptySettings(): GroupData {
        return GroupData(
            String.empty(), String.empty(),
            isPrivate = true,
            isWriteOnlyAdmin = true
        )
    }

    private fun showProgress() {
        binding?.confirmButtonContainer?.isEnabled = false
        binding?.confirmButton?.invisible()
        binding?.confirmProgressBar?.visible()
    }

    private fun hideProgress() {
        binding?.confirmButtonContainer?.isEnabled = true
        binding?.confirmProgressBar?.invisible()
        binding?.confirmButton?.visible()
    }

    private fun deleteTemporaryGroupAvatar() {
        viewModel.deleteTempImageFile(selectedGroupImagePath)
    }

    private fun showAlertMessage(message: String) {
        NToast.with(view)
            .text(message)
            .typeAlert()
            .show()
    }

    private fun showErrorMessage(message: String) {
        NToast.with(view)
            .text(message)
            .show()
    }

    private fun doAnalyticsLog() = viewModel.logScreenForFragment(groupId)

    private fun initBindings() {
        binding?.let {
            groupNameBinding = FragmentEditGroupRedesignedNameFieldBinding.bind(it.root)
            groupDescBinding = FragmentEditGroupRedesignedDescriptionFieldBinding.bind(it.root)
        }
    }

    data class GroupData(
        var name: String,
        var description: String,
        var isPrivate: Boolean,
        var isWriteOnlyAdmin: Boolean
    )

    companion object {
        private const val MAX_NAME_LENGTH = 45
        private const val MAX_DESCRIPTION_LENGTH = 500
    }
}
