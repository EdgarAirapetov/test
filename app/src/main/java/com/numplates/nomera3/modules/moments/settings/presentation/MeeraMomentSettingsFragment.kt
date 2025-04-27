package com.numplates.nomera3.modules.moments.settings.presentation

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.dialogs.MeeraConfirmPicDialogBuilder
import com.meera.core.dialogs.MeeraConfirmVariantDialogBuilder
import com.meera.core.dialogs.MeeraConfirmVariantType
import com.meera.core.extensions.externalStoragePermissionAfter33And34
import com.meera.core.extensions.safeNavigate
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMomentSettingsFragmentBinding
import com.numplates.nomera3.modules.moments.settings.data.MomentSettingsEvent
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsListener
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsDataMapper
import kotlinx.coroutines.launch

class MeeraMomentSettingsFragment :
    MeeraBaseDialogFragment(R.layout.meera_moment_settings_fragment, ScreenBehaviourState.Full),
    ChangeProfileSettingsListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraMomentSettingsFragmentBinding::bind)

    private val viewModel by viewModels<MomentSettingsViewModel> { App.component.getViewModelFactory() }
    private var updateItemType = MeeraMomentsSettingsItemType.SETTING_VISIBILITY

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = saveToExternalPermissions().all { key -> permissions[key] == true }
            if (isGranted) {
                viewModel.toggleSaveToGallery()
            }
        }
    private val settingsAdapter by lazy {
        MeeraMomentSettingsAdapter(
            isGalleryPermissionEnable = isSaveToExternalGranted(),
            callback = this@MeeraMomentSettingsFragment::initMeeraMomentSettingsAction,
        )
    }
    private val settingsMapper = MeeraPrivacySettingsDataMapper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        initObservables()
        binding.vMomentsSettingsNavView.backButtonClickListener = {
            if (activity is MomentsWrapperActivity) {
                activity?.finish()
            } else {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshUserSettingsFromNetwork()
    }

    private fun Boolean.toSettingsEnum(): SettingsUserTypeEnum {
        return if (this) {
            SettingsUserTypeEnum.ALL
        } else {
            SettingsUserTypeEnum.NOBODY
        }
    }

    private fun initObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settingsState.collect { settings ->
                handleSettingsData(settings)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventStream.collect { event ->
                handleSettingsEvent(event)
            }
        }
    }

    private fun setupAdapter() {
        binding.rvMomentsSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }
    }

    private fun initMeeraMomentSettingsAction(action: MeeraMomentSettingsAction) {
        when (action) {
            is MeeraMomentSettingsAction.HideFrom -> {
                updateItemType = MeeraMomentsSettingsItemType.SETTING_VISIBILITY
                if (action.count > 0) {
                    findNavController().safeNavigate(R.id.action_meeraMomentSettingsFragment_to_meeraMomentSettingsHideFromFragment)
                } else {
                    findNavController().safeNavigate(R.id.action_meeraMomentSettingsFragment_to_meeraMomentSettingsHideFromAddUserFragment)
                }
            }

            is MeeraMomentSettingsAction.HideMoment -> {
                updateItemType = MeeraMomentsSettingsItemType.MOMENTS_NOT_SHOW
                findNavController().safeNavigate(R.id.action_meeraMomentSettingsFragment_to_meeraMomentSettingsNotShowFragment)
            }

            is MeeraMomentSettingsAction.ShowOnlyFriends -> {
                updateItemType = MeeraMomentsSettingsItemType.SETTING_VISIBILITY
                if (!action.isCheck && viewModel.isProfileClosed()) {
                    confirmPicDialogOpen { changeProfileSettingConfirmed() }
                } else {
                    viewModel.setSetting(SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS, action.isCheck.toSettingsEnum())
                }
            }

            is MeeraMomentSettingsAction.AllowComments -> {
                updateItemType = MeeraMomentsSettingsItemType.ALLOW_COMMENTS
                confirmDialogOpen(action.variant)
            }

            is MeeraMomentSettingsAction.SaveGallery -> {
                if (isSaveToExternalGranted()) {
                    updateItemType = MeeraMomentsSettingsItemType.GALLERY_SAVING
                    viewModel.setSetting(
                        settingEnum = SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY,
                        value = action.isCheck.toSettingsEnum()
                    )
                } else {
                    checkWriteExternalPermission()
                }
            }
        }
    }

    override fun changeProfileSettingCanceled() = Unit

    override fun changeProfileSettingConfirmed() {
        viewModel.setSetting(
            settingEnum = SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS,
            value = false.toSettingsEnum(),
            shouldUpdate = true
        )
    }

    private fun confirmDialogOpen(selectVariant: Int) {
        MeeraConfirmVariantDialogBuilder()
            .setHeader(R.string.moment_settings_allow_comments)
            .setFirstCellText(R.string.everyone)
            .setFirstCellIcon(R.drawable.ic_outlined_planet_m)
            .setSecondCellText(R.string.friends)
            .setSecondCellIcon(R.drawable.ic_outlined_user_m)
            .setThirdCellText(R.string.nobody)
            .setThirdCellIcon(R.drawable.ic_outlined_circle_block_m)
            .setSelectOption(getConfirmVariantType(selectVariant))
            .setVariantCellListener {
                viewModel.setSetting(
                    settingEnum = SettingsKeyEnum.MOMENTS_ALLOW_COMMENT,
                    value = getTypeConfirmDialog(it)
                )
            }
            .show(childFragmentManager)
    }

    private fun confirmPicDialogOpen(changeSettingListener: (() -> Unit)? = null) {
        MeeraConfirmPicDialogBuilder()
            .setHeader(R.string.meera_change_settings)
            .setDescriptionFirst(R.string.settings_change_profile_settings_open_profile)
            .setDescriptionSecond(R.string.settings_change_profile_settings_settings_not_change)
            .setTopBtnText(R.string.change)
            .setTopClickListener { changeSettingListener?.invoke() }
            .setBottomBtnText(R.string.cancel)
            .setTopBtnType(ButtonType.FILLED)
            .show(childFragmentManager)
    }

    private fun getTypeConfirmDialog(type: MeeraConfirmVariantType): SettingsUserTypeEnum {
        return when (type) {
            MeeraConfirmVariantType.FIRST -> SettingsUserTypeEnum.NOBODY
            MeeraConfirmVariantType.SECOND -> SettingsUserTypeEnum.FRIENDS
            MeeraConfirmVariantType.THIRD -> SettingsUserTypeEnum.ALL
        }
    }

    private fun getConfirmVariantType(selectVariant: Int): MeeraConfirmVariantType {
        return when (selectVariant) {
            SettingsUserTypeEnum.NOBODY.ordinal -> MeeraConfirmVariantType.FIRST
            SettingsUserTypeEnum.FRIENDS.ordinal -> MeeraConfirmVariantType.SECOND
            SettingsUserTypeEnum.ALL.ordinal -> MeeraConfirmVariantType.THIRD
            else -> MeeraConfirmVariantType.FIRST
        }
    }

    private fun handleSettingsEvent(event: MomentSettingsEvent) {
        when (event) {
            MomentSettingsEvent.Error -> {
                showCommonError(R.string.settings_error_get_settings)
            }
        }
    }

    private fun showCommonError(errorTextRes: Int) {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(errorTextRes),
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
            )

        ).show()
    }

    private fun handleSettingsData(settings: List<PrivacySettingUiModel>) {
        settingsAdapter.submitList(settingsMapper.handleSettingsMomentData(settings))
    }

    private fun checkWriteExternalPermission() {
        when {
            isSaveToExternalGranted() -> {
                viewModel.toggleSaveToGallery()
            }

            shouldShowRequestPermissionRationale() -> {
                showRationaleWriteExternal()
            }

            else -> {
                requestPermissionLauncher.launch(saveToExternalPermissions().toTypedArray())
            }
        }
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        return saveToExternalPermissions().any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
        }
    }

    private fun isSaveToExternalGranted(): Boolean {
        return saveToExternalPermissions().all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun saveToExternalPermissions(): List<String> {
        return externalStoragePermissionAfter33And34().toList()
    }

    private fun showRationaleWriteExternal() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.allow_access)
            .setDescription(R.string.file_moment_access_permissions)
            .setTopBtnText(R.string.open_settings)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener { sendUserToAppSettings() }
            .show(childFragmentManager)
    }

}
