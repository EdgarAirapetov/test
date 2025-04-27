package com.numplates.nomera3.modules.communities.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.empty
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.extensions.toInt
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.graphics.NGraphics
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraEditGroupFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCanWrite
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHavePhoto
import com.numplates.nomera3.modules.communities.ui.fragment.adapter.CommunityEditItemType
import com.numplates.nomera3.modules.communities.ui.fragment.adapter.MeeraCommunityEditAdapter
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.viewmodel.GroupEditViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.GroupEditViewEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

private const val MIN_LENGTH_NAME = 3
private const val MIN_LENGTH_DESCRIPTION = 1
private const val MAX_NAME_LENGTH = 45
private const val MAX_DESCRIPTION_LENGTH = 500

class MeeraCommunityEditFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_edit_group_fragment,
        behaviourConfigState = ScreenBehaviourState.Full
    ),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    IOnBackPressed,
    TedBottomSheetPermissionActionsListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    var refreshCallback: (() -> Unit)? = null

    private val binding by viewBinding(MeeraEditGroupFragmentBinding::bind)
    private var groupName: String? = null
    private var groupDescription: String? = null
    private val viewModel by viewModels<GroupEditViewModel>()

    private var groupId: Int? = null
    private var selectedGroupImagePath: String = String.empty()
    private var isClosing = false

    private var currentGroupData: GroupData = createEmptySettings()
    private var defaultOrPreviousGroupData: GroupData = createEmptySettings()

    private var mediaPicker: TedBottomSheetDialogFragment? = null
    private var isMaxLengthName = false
    private var isMaxLengthDescription = false
    private var isDeleteGroupAvatar = false
    private var isPrivateGroupObservable: Boolean by Delegates.observable(true) { _, _, newValue ->
        if (newValue) setPrivateCommunity(newValue)
    }

    private val photoUrlState = MutableStateFlow("")
    private val validationErrorNameState = MutableStateFlow("")
    private val validationErrorDescriptionState = MutableStateFlow("")

    private var adapter: MeeraCommunityEditAdapter? = null
    private val act: MeeraAct by lazy {
        activity as MeeraAct
    }

    private fun setPrivateCommunity(newValue: Boolean) {
        currentGroupData.isPrivate = newValue
        tryEnableSaveButton()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.injectDependencies()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        groupId = arguments
            ?.getInt(ARG_GROUP_ID)
            ?.takeIf { it > 0 }

        viewModel.isCreatorAppearanceMode = arguments
            ?.getBoolean(IArgContainer.ARG_IS_GROUP_CREATOR, false)
            ?: false

        initViews()

        if (groupId != null) {
            viewModel.getGroupInfo(groupId)
        }

        act.permissionListener.add(listener)

        initAdapter()
        initLiveObservables()
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

    override fun onStart() {
        super.onStart()
        isClosing = false
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
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.camera_settings_dialog_title)
            .setDescription(R.string.camera_settings_dialog_description)
            .setTopBtnText(R.string.camera_settings_dialog_action)
            .setBottomBtnText(R.string.camera_settings_dialog_cancel)
            .setTopClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    private fun initAdapter() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        adapter = MeeraCommunityEditAdapter(::communityEditListener)
        binding?.rvEditGroup?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding?.rvEditGroup?.adapter = adapter
        val communityEditItemType = if (viewModel.isCreatorAppearanceMode) {
            CommunityEditItemType.entries.toList()
        } else {
            CommunityEditItemType.entries.filter {
                it != CommunityEditItemType.SWITCH_ITEM
            }.toList()
        }
        adapter?.submitList(communityEditItemType)
    }

    private fun backClicked() {
        closeEditor()
    }

    private fun closeEditor() {
        isClosing = true
        context?.hideKeyboard(requireView())
        clearData()
        findNavController().popBackStack()
    }

    private fun initViews() {
        binding?.vConfirmButton?.isEnabled = false
        if (groupId != null && groupId != 0) {
            binding.vNavBar.title = getString(R.string.editing)
            binding.vNavBar.setBackIcon(R.drawable.ic_outlined_arrow_left_m)
        } else {
            binding?.vNavBar?.setBackIcon(R.drawable.ic_outlined_close_m)
        }
        binding?.vConfirmButton?.textColor(R.color.uiKitColorDisabledPrimary)
        binding?.vConfirmButton?.setThrottledClickListener {
            saveGroup()
        }
        binding?.vNavBar?.backButtonClickListener = {
            findNavController().popBackStack()
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
        val isMandatoryFieldsNotEmpty = isMandatoryFieldsNotEmpty()
        setConfirmButtonEnable(isMandatoryFieldsNotEmpty)
    }

    private fun setConfirmButtonEnable(isMandatoryFieldsNotEmpty: Boolean) {
        if (isMandatoryFieldsNotEmpty) {
            binding?.vConfirmButton?.isEnabled = true
            binding?.vConfirmButton?.textColor(R.color.ui_light_green)
        } else {
            binding?.vConfirmButton?.isEnabled = false
            binding?.vConfirmButton?.textColor(R.color.uiKitColorDisabledPrimary)
        }
    }

    private fun isMandatoryFieldsNotEmpty(): Boolean {
        return getGroupNameEditTextLength() >= MIN_LENGTH_NAME &&
            getGroupDescriptionEditTextLength() >= MIN_LENGTH_DESCRIPTION &&
            hasUnsavedChanges()
    }

    private fun getGroupNameEditTextLength(): Int {
        return groupName?.length ?: 0
    }

    private fun getGroupDescriptionEditTextLength(): Int {
        return groupDescription?.length ?: 0
    }

    private fun initLiveObservables() {
        viewModel.liveGroupInfo.observe(viewLifecycleOwner) { group ->
            group.community?.let {
                adapter?.setCommunityInfo(it)
            }
        }

        viewModel.liveViewEvent.observe(viewLifecycleOwner) { viewEvent ->
            handleEvents(viewEvent)
        }
    }

    private fun showGroupImageOrPlaceholder(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            photoUrlState.value = imageUrl
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
                findNavController().safeNavigate(
                    resId = R.id.meeraCommunityRoadFragment,
                    bundle = Bundle().apply {
                        putInt(ARG_GROUP_ID, event.groupId)
                        putBoolean(IS_NEW_GROUP, true)
                    }
                )
            }

            is GroupEditViewEvent.FailureGroupCreate -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(R.string.group_error_create_group)
            }

            is GroupEditViewEvent.FailureGroupCreateExist -> {
                hideProgress()
                validationErrorNameState.value = getString(R.string.meera_group_error_create_group_name_occuped)
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
                showErrorMessage(R.string.group_error_edit_group)
            }

            is GroupEditViewEvent.FailureGroupEditExist -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(R.string.group_error_edit_group_exist)
            }

            is GroupEditViewEvent.SuccessGroupDeleted -> {
                refreshCallback?.invoke()
                hideProgress()
                deleteTemporaryGroupAvatar()
                findNavController().popBackStack()
            }

            is GroupEditViewEvent.FailureGroupDeleted -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(R.string.group_error_delete_group)
            }

            is GroupEditViewEvent.ErrorNameSizeMoreThenTree -> {
                hideProgress()
                showAlertMessage(R.string.community_name_should_be_longer_than_three)
            }

            GroupEditViewEvent.NoInternetConnection -> {
                hideProgress()
                deleteTemporaryGroupAvatar()
                showErrorMessage(R.string.no_internet)
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

    private enum class MandatoryGroupDataValidationResult {
        GROUP_NAME_EMPTY,
        GROUP_NAME_TOO_SHORT,
        GROUP_NAME_TOO_LONG,
        GROUP_DESCRIPTION_EMPTY,
        GROUP_DESCRIPTION_TOO_LONG,
        SUCCESS
    }

    private fun validateMandatoryGroupName(groupName: String?): MandatoryGroupDataValidationResult {
        if (groupName.isNullOrEmpty()) return MandatoryGroupDataValidationResult.GROUP_NAME_EMPTY
        if (groupName.isNullOrBlank()) return MandatoryGroupDataValidationResult.GROUP_NAME_EMPTY
        if (groupName.trim().length < 3) return MandatoryGroupDataValidationResult.GROUP_NAME_TOO_SHORT
        if (groupName.trim().length == MAX_NAME_LENGTH) {
            if (isMaxLengthName) {
                return MandatoryGroupDataValidationResult.GROUP_NAME_TOO_LONG
            } else {
                isMaxLengthName = true
            }
        } else {
            isMaxLengthName = false
        }
        return MandatoryGroupDataValidationResult.SUCCESS
    }

    private fun validateMandatoryGroupDescription(groupDescription: String?): MandatoryGroupDataValidationResult {
        if (groupDescription.isNullOrEmpty()) return MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_EMPTY
        if (groupDescription.isNullOrBlank()) return MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_EMPTY
        if (groupDescription.trim().length == MAX_DESCRIPTION_LENGTH) {
            if (isMaxLengthDescription) {
                return MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_TOO_LONG
            } else {
                isMaxLengthDescription = true
            }
        } else {
            isMaxLengthDescription = false
        }

        return MandatoryGroupDataValidationResult.SUCCESS
    }

    private fun showGroupNameError(text: String) {
        validationErrorNameState.value = text
    }

    private fun showGroupDescriptionError(text: String) {
        validationErrorDescriptionState.value = text
    }


    private fun validateNameCommunity() {
        when (validateMandatoryGroupName(groupName)) {
            MandatoryGroupDataValidationResult.GROUP_NAME_EMPTY -> {
                showGroupNameError(getString(R.string.group_name_is_mandatory))
            }

            MandatoryGroupDataValidationResult.GROUP_NAME_TOO_LONG -> {
                showGroupNameError(getString(R.string.group_name_is_too_long))
            }

            MandatoryGroupDataValidationResult.GROUP_NAME_TOO_SHORT -> {
                showGroupNameError(getString(R.string.group_name_is_too_short))
            }

            else -> Unit
        }
    }

    private fun validateDescriptionCommunity() {
        val rez = validateMandatoryGroupDescription(groupDescription)
        when (rez) {
            MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_EMPTY -> {
                showGroupDescriptionError(getString(R.string.group_description_is_mandatory))
            }

            MandatoryGroupDataValidationResult.GROUP_DESCRIPTION_TOO_LONG -> {
                showGroupDescriptionError(getString(R.string.group_description_is_too_long))
            }

            else -> Unit
        }
    }

    private fun saveGroup() {
        val (isPrivateGroup, isWriteOnlyAdmin) = getExtraSettings()
        val groupNameTrimmed = groupName?.trim() ?: ""
        val groupDescriptionTrimmed = groupDescription?.trim() ?: ""

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

    private fun getExtraSettings(): Pair<Int, Int> {
        if (viewModel.isCreatorAppearanceMode) {
            val isPrivateGroup = isPrivateGroupObservable
            val isWriteOnlyAdmin = currentGroupData.isWriteOnlyAdmin
            return Pair(isPrivateGroup.toInt(), isWriteOnlyAdmin.toInt())
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
            name = groupName,
            description = groupDescription,
            privateGroup = isPrivateGroup,
            royalty = isWriteOnlyAdmin,
            avatar = selectedGroupImagePath
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
                groupId = it,
                name = groupName,
                description = groupDescription,
                privateGroup = isPrivateGroup,
                royalty = isWriteOnlyAdmin,
                avatar = selectedGroupImagePath,
                isDeleteGroupAvatar = isDeleteGroupAvatar
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
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Community,
            cameraType = MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
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
                        showErrorMessage(R.string.error_editing_media)
                    }
                }
            }
        )
    }

    private fun clearData() {
        defaultOrPreviousGroupData = createEmptySettings()
        currentGroupData = createEmptySettings()
    }

    private fun createEmptySettings() = GroupData()

    private fun showProgress() {
        binding?.vConfirmButton?.invisible()
        binding?.pbConfirmProgressBar?.visible()
    }

    private fun hideProgress() {
        binding?.pbConfirmProgressBar?.invisible()
        binding?.vConfirmButton?.visible()
    }

    private fun deleteTemporaryGroupAvatar() {
        viewModel.deleteTempImageFile(selectedGroupImagePath)
    }

    private fun showAlertMessage(messageRes: Int) {
        showCommonSuccessMessage(getText(messageRes), requireView())
    }

    private fun showErrorMessage(messageRes: Int) {
        showCommonError(getText(messageRes), requireView())
    }

    private fun doAnalyticsLog() = viewModel.logScreenForFragment(groupId)

    private fun communityEditListener(action: MeeraCommunityEditAction) {
        when (action) {
            is MeeraCommunityEditAction.AddPhoto -> {
                openImagePicker {
                    tryEnableSaveButton()
                }
                lifecycleScope.launch {
                    photoUrlState.collect { url ->
                        action.selectUrl.invoke(url)
                    }
                }
                isDeleteGroupAvatar = false
            }

            is MeeraCommunityEditAction.DeletePhoto -> {
                deleteTemporaryGroupAvatar()
                photoUrlState.value = String.empty()
                isDeleteGroupAvatar = true
                tryEnableSaveButton()
            }

            is MeeraCommunityEditAction.EditNameCommunity -> {
                if (groupName?.isNotEmpty() == true) {
                    groupName = action.name
                    tryEnableSaveButton()
                    showGroupNameError(String.empty())
                    validateNameCommunity()
                } else {
                    groupName = action.name
                }
                groupName?.let {
                    currentGroupData.name = it
                }

                lifecycleScope.launch {
                    validationErrorNameState.collect {
                        action.validationErrorState.invoke(it)
                    }
                }
            }

            is MeeraCommunityEditAction.EditDescriptionCommunity -> {
                if (groupDescription?.isNotEmpty() == true) {
                    groupDescription = action.description
                    tryEnableSaveButton()
                    showGroupDescriptionError(String.empty())
                    validateDescriptionCommunity()
                } else {
                    groupDescription = action.description
                }

                groupDescription?.let {
                    currentGroupData.description = it
                }

                lifecycleScope.launch {
                    validationErrorDescriptionState.collect {
                        action.validationErrorState.invoke(it)
                    }
                }
            }

            is MeeraCommunityEditAction.EditPhoto -> {
                if (action.imageUrl.isNotEmpty() && !action.isLoadDevices) {
                    NGraphics.saveImageToDevice(
                        context = requireContext(),
                        imageUrl = action.imageUrl,
                        onSaved = { uri ->
                            openPhotoEditorForCommunity(uri) {
                                tryEnableSaveButton()
                            }
                        }
                    )
                } else {
                    openPhotoEditorForCommunity(Uri.parse(action.imageUrl)) {
                        tryEnableSaveButton()
                    }
                }

                lifecycleScope.launch {
                    photoUrlState.collect { url ->
                        action.selectUrl.invoke(url)
                    }
                }
            }

            is MeeraCommunityEditAction.OpenPicker -> {
                openImagePicker {
                    tryEnableSaveButton()
                }
                lifecycleScope.launch {
                    photoUrlState.collect { url ->
                        action.selectUrl.invoke(url)
                    }
                }
            }

            is MeeraCommunityEditAction.OpenCommunity -> {
                isPrivateGroupObservable = false
                tryEnableSaveButton()
            }

            is MeeraCommunityEditAction.CloseCommunity -> {
                isPrivateGroupObservable = true
                tryEnableSaveButton()
            }

            is MeeraCommunityEditAction.OnlyAdministrationWrites -> {
                if (currentGroupData.isWriteOnlyAdmin != action.isEnable) {
                    currentGroupData = currentGroupData.copy(isWriteOnlyAdmin = action.isEnable)
                    tryEnableSaveButton()
                } else {
                    setConfirmButtonEnable(false)
                }
            }
        }
    }

    data class GroupData(
        var name: String? = null,
        var description: String? = null,
        var isPrivate: Boolean? = null,
        var isWriteOnlyAdmin: Boolean? = null
    )
}


