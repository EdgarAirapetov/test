package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.toInt
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.NSnackbar
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentPrivacyBinding
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.NOT_PUBLIC
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.PRIVATE_ROAD
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COUNT_USERS_BLACKLIST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COUNT_USERS_WHITELIST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MESSAGE_SETTINGS_USER_EXCLUSIONS_COUNT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PRIVACY_TYPE_VALUE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PROFILE_CLOSED_VALUE
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.MeeraChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.closeprofile.CloseProfileListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.closeprofile.MeeraCloseProfileBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.openprofile.MeeraOpenProfileBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.openprofile.OpenProfileListener
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsDataMapper
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.PrivacySettingsViewEvent
import timber.log.Timber

class MeeraPrivacyFragment : MeeraBaseDialogFragment(R.layout.meera_fragment_privacy, ScreenBehaviourState.Full),
    MeeraPrivacySettingsAdapter.IPrivacySettingsInteractor, CloseProfileListener, OpenProfileListener,
    ChangeProfileSettingsListener, BasePermission by BasePermissionDelegate() {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentPrivacyBinding::bind)

    private val viewModel by viewModels<PrivacyNewViewModel>()

    private val settingsAdapter: MeeraPrivacySettingsAdapter by lazy {
        MeeraPrivacySettingsAdapter(this@MeeraPrivacyFragment)
    }

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy {
        SyncContactsDialogDelegate(childFragmentManager)
    }

    private val settingsDataMapper = MeeraPrivacySettingsDataMapper()

    private var undoSnackBar: NSnackbar? = null
    private var restoreDialog: Dialog? = null
    private var pendingDeleteSnackbar: UiKitSnackBar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initRecycler()
        initLiveObservables()
        initListeners()
        initPermission()
        if (savedInstanceState == null) {
            viewModel.subscribeToPrivacySettings()
        }
    }

    override fun onStart() {
        super.onStart()
        restoreDialog?.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.requestSettings()
    }

    override fun onStop() {
        super.onStop()
        undoSnackBar?.dismiss()
        restoreDialog?.dismiss()
    }

    private fun initPermission() {
        initPermissionDelegate(
            activity = requireActivity(), viewLifecycleOwner = viewLifecycleOwner
        )
    }

    private fun setupToolbar() {
        binding.apply {
            rvPrivacy.let { navView.addScrollableView(it) }
            navView.title = getString(R.string.profile_privacy)
            navView.backButtonClickListener = { findNavController().popBackStack() }
        }
    }

    private fun initRecycler() {
        binding.rvPrivacy.adapter = settingsAdapter
    }

    private fun initLiveObservables() {
        viewModel.liveSettings.observe(viewLifecycleOwner) { settings ->
            settingsAdapter.submitList(settingsDataMapper.handleSettingsData(settings))
        }
        viewModel.liveViewEvents.observe(viewLifecycleOwner) { viewEvent ->
            handleViewEvents(viewEvent)
        }
        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            handlePrivacyScreenEvent(event)
        }
    }

    private fun initListeners() {
        childFragmentManager.setFragmentResultListener(
            MeeraCloseProfileBottomSheetFragment.ARG_CLOSE_PROFILE_REQUEST_KEY, viewLifecycleOwner
        ) { requestKey, bundle ->
            if (requestKey == MeeraCloseProfileBottomSheetFragment.ARG_CLOSE_PROFILE_REQUEST_KEY) {
                val closeProfileConfirmationResult =
                    bundle.getBoolean(MeeraCloseProfileBottomSheetFragment.ARG_CLOSE_PROFILE, false)
                if (closeProfileConfirmationResult) closeProfileSettingConfirmed() else closeProfileSettingCanceled()
            }
        }
        childFragmentManager.setFragmentResultListener(
            MeeraOpenProfileBottomSheetFragment.ARG_OPEN_PROFILE_REQUEST_KEY, viewLifecycleOwner
        ) { requestKey, bundle ->
            if (requestKey == MeeraOpenProfileBottomSheetFragment.ARG_OPEN_PROFILE_REQUEST_KEY) {
                val openProfileConfirmationResult =
                    bundle.getBoolean(MeeraOpenProfileBottomSheetFragment.ARG_OPEN_PROFILE, false)
                if (openProfileConfirmationResult) openProfileSettingConfirmed() else openProfileSettingCanceled()
            }
        }
        childFragmentManager.setFragmentResultListener(
            MeeraChangeProfileSettingsBottomSheetFragment.ARG_CHANGE_PROFILE_REQUEST_KEY, viewLifecycleOwner
        ) { requestKey, bundle ->
            val changeProfile =
                bundle.getBoolean(MeeraChangeProfileSettingsBottomSheetFragment.ARG_CHANGE_PROFILE, false)
            if (changeProfile) changeProfileSettingConfirmed()
            else changeProfileSettingCanceled()

        }
    }

    private fun handleViewEvents(event: PrivacySettingsViewEvent) {
        when (event) {
            is PrivacySettingsViewEvent.OnLoadSettingsError -> {
                showErrorToast(R.string.settings_error_get_settings)
            }
        }
    }

    private fun handlePrivacyScreenEvent(event: PrivacyNewViewModel.PrivacyScreenEvent) {
        when (event) {

            is PrivacyNewViewModel.PrivacyScreenEvent.InternetConnectionError -> {
                showErrorToast(R.string.error_check_internet)
            }

            is PrivacyNewViewModel.PrivacyScreenEvent.StartUndoTimer -> {
                showUndoRestoreSettings()
            }

            is PrivacyNewViewModel.PrivacyScreenEvent.ShowContactsSyncPermissionUiEffect -> {
                requestContactsSyncPermission(
                    key = event.key, turnOn = event.enabled
                )
            }

            is PrivacyNewViewModel.PrivacyScreenEvent.ShowConfirmCancelSyncContactsDialogUiEffect -> {
                showConfirmCancelSyncContactsDialog()
            }

            is PrivacyNewViewModel.PrivacyScreenEvent.ShowSyncContactsDialogPermissionDenied -> {
                showSyncContactsDialogPermissionDenied()
            }

            is PrivacyNewViewModel.PrivacyScreenEvent.ShowSyncContactsSuccessUiEffect -> {
                showSyncContactsDialogSuccess(event.syncCount)
            }

            else -> Unit
        }
    }

    private fun showConfirmCancelSyncContactsDialog() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.turn_off_sync_contacts,
            descriptionRes = R.string.turn_off_sync_description,
            positiveButtonRes = R.string.general_turn_off,
            positiveButtonAction = {
                viewModel.setSetting(
                    key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key, value = false.toInt(), needPushAmplitude = false
                )
            },
            negativeButtonRes = R.string.general_cancel,
            negativeButtonAction = {
                val currentList = settingsAdapter.getCurrentCollection()
                viewModel.changePermissionSyncContactsSwitch(
                    currentList = currentList, enabled = true
                )
            },
            iconRes = R.drawable.meera_ic_sync_contacts_dialog,
            isAppRedesigned = true
        )
    }

    private fun showSyncContactsDialogPermissionDenied() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.contacts_synchronization_allow_access,
            descriptionRes = R.string.contacts_sync_allow_in_settings_description,
            positiveButtonRes = R.string.go_to_settings,
            positiveButtonAction = { sendUserToAppSettings() },
            negativeButtonRes = R.string.general_later,
            iconRes = R.drawable.meera_ic_sync_contacts_dialog,
            isAppRedesigned = true
        )
    }

    private fun showSyncContactsDialogSuccess(syncCount: Int) {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            positiveButtonAction = { viewModel.logSuccessDialogClosedByButton(true, syncCount) },
            closeDialogDismissListener = { viewModel.logSuccessDialogClosedByButton(false, syncCount) },
            iconRes = R.drawable.meera_ic_sync_contacts_done,
            isAppRedesigned = true
        )
    }

    private fun requestContactsSyncPermission(
        key: String, turnOn: Int
    ) {
        setPermissions(permission = Manifest.permission.READ_CONTACTS, listener = object : PermissionDelegate.Listener {
            override fun onGranted() {
                viewModel.setSetting(
                    key = key, value = turnOn, needPushAmplitude = false
                )
            }

            override fun onDenied() {
                val currentList = settingsAdapter.getCurrentCollection()
                val deniedAndNoRationaleNeededAfterRequest =
                    !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                viewModel.handlePermissionReadContactsDenied(
                    deniedAndNoRationaleNeededAfterRequest = deniedAndNoRationaleNeededAfterRequest,
                    currentList = currentList
                )
            }

            override fun onError(error: Throwable?) {
                Timber.e(error)
            }
        })
    }

    private fun showErrorToast(@StringRes textRes: Int) {
        NToast.with(view).text(getString(textRes)).typeError().show()
    }

    private fun showUndoRestoreSettings() {
        pendingDeleteSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.meera_snack_bar_nofitification_title_settings_reset),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_RESTORE_SETTINGS.toLong(),
                        onTimerFinished = {
                            viewModel.restoreDefaultSettings()
                        }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                dismissOnClick = true,
                dismissListeners = DismissListeners(
                    dismissListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }
                )
            )
        )

        pendingDeleteSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        pendingDeleteSnackbar?.show()
    }

    override fun switchGender(key: String, isEnabled: Boolean) {
        viewModel.setSetting(key, isEnabled.toInt())
    }

    override fun switchAge(key: String, isEnabled: Boolean) {
        if (viewModel.closedProfile() && isEnabled) {
            MeeraChangeProfileSettingsBottomSheetFragment.show(childFragmentManager)
            return
        }
        viewModel.setSetting(key, isEnabled.toInt())
    }

    override fun changeProfileSettingConfirmed() {
        viewModel.setSetting(
            key = SettingsKeyEnum.SHOW_BIRTHDAY.key, value = true.toInt(), shouldUpdate = true
        )
    }

    override fun changeProfileSettingCanceled() {
        viewModel.requestSettings()
    }

    override fun clickHideRoadPosts(count: Int?) {
        count?.let {
            if (count > 0) {
                val args = bundleOf(ARG_MESSAGE_SETTINGS_USER_EXCLUSIONS_COUNT to count.toLong())
                findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraRoadSettingsFragment, args)
            }
        }
    }

    override fun switchAntiObscene(key: String, isEnabled: Boolean) {
        viewModel.setSetting(key, isEnabled.toInt())
    }

    override fun switchShake(key: String, isEnabled: Boolean) {
        viewModel.handleShakeEnabledUiAction(
            key = key, enabled = isEnabled
        )
    }

    override fun switchShareScreenshot(key: String, isEnabled: Boolean) {
        viewModel.handleShareScreenshotSwitchAction(
            key = key, enabled = isEnabled
        )
    }

    override fun switchContactSync(key: String, isEnabled: Boolean) {
        viewModel.handleSyncContactSwitchAction(
            key = key, enabled = isEnabled
        )
    }

    override fun switchNewAvatarPost(key: String, isEnabled: Boolean) {
        viewModel.setSetting(key, if (isEnabled) PRIVATE_ROAD.state else NOT_PUBLIC.state)
    }

    override fun clickMomentSettings() {
        findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraMomentSettingsFragment)
    }

    override fun switchClosedProfile(key: String, isEnabled: Boolean) {
        if (isEnabled) showCloseProfileAttentionDialog() else showOpenProfileAttentionDialog()
    }

    override fun closeProfileSettingConfirmed() {
        viewModel.setSetting(key = SettingsKeyEnum.CLOSED_PROFILE.key, value = true.toInt(), shouldUpdate = true)
    }

    override fun closeProfileSettingCanceled() {
        viewModel.requestSettings()
    }

    override fun openProfileSettingConfirmed() {
        viewModel.setSetting(key = SettingsKeyEnum.CLOSED_PROFILE.key, value = false.toInt(), shouldUpdate = true)
    }

    override fun openProfileSettingCanceled() {
        viewModel.requestSettings()
    }

    override fun clickOnlineStatus(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        val args = getWhiteBlackListBundle(value, countBlacklist, countWhitelist)
        findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraOnlineSettingsFragment, args)
    }

    override fun clickMapPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        val args = getWhiteBlackListBundle(value, countBlacklist, countWhitelist)
        findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraMapSettingsFragment, args)
    }

    override fun clickCallPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        val args = getWhiteBlackListBundle(value, countBlacklist, countWhitelist)
        findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraCallSettingsFragment, args)
    }

    override fun clickPersonalMessages(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        val args = getWhiteBlackListBundle(value, countBlacklist, countWhitelist)
        findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraPersonalMessagesFragment, args)
    }

    override fun onFriendsAndFollowersClicked(value: Int?) {
        val args = bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile()
        )
        MeeraShowFriendsSubscribersPrivacyFragment.show(childFragmentManager, args)
    }

    override fun clickBlacklistUsers(count: Int?) {
        count?.let {
            if (count > 0) {
                findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraBlacklistSettingsUsersFragment)
            } else {
                findNavController().safeNavigate(R.id.action_meeraPrivacyFragment_to_meeraBlacklistSettingsAddUsersFragment)
            }
        }
    }

    override fun clickAboutMePrivacy(value: Int?) {
        val args = bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile()
        )
        MeeraAboutMeSettingsFragment.show(childFragmentManager, args)
    }

    override fun clickGaragePrivacy(value: Int?) {
        val args = bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile()
        )
        MeeraGarageSettingsFragment.show(childFragmentManager, args)
    }

    override fun clickGiftPrivacy(value: Int?) {
        val args = bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile()
        )
        MeeraGiftsSettingsFragment.show(childFragmentManager, args)
    }


    override fun clickPersonalFeedPrivacy(value: Int?) {
        val args = bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile()
        )
        MeeraPersonalFeedSettingsFragment.show(childFragmentManager, args)
    }

    override fun clickBirthdayDetails(value: Int?) {
        val args = bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile()
        )
        MeeraBirthdaySettingsFragment.show(childFragmentManager, args)
    }

    override fun clickRestoreDefaultSettings() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.meera_settings_restore_defaults)
            .setDescription(R.string.meera_settings_restore_defaults_description)
            .setTopBtnText(R.string.general_restore)
            .setTopBtnType(ButtonType.FILLED)
            .setTopClickListener { viewModel.checkNetworkAndRestoreSettings() }
            .setBottomBtnText(R.string.cancel)
            .setBottomBtnType(ButtonType.TRANSPARENT)
            .show(childFragmentManager)
    }


    private fun getWhiteBlackListBundle(value: Int?, countBlacklist: Int?, countWhitelist: Int?): Bundle =
        bundleOf(
            ARG_PRIVACY_TYPE_VALUE to value,
            ARG_PROFILE_CLOSED_VALUE to viewModel.closedProfile(),
            ARG_COUNT_USERS_BLACKLIST to countBlacklist,
            ARG_COUNT_USERS_WHITELIST to countWhitelist
        )

    private fun showCloseProfileAttentionDialog() {
        MeeraCloseProfileBottomSheetFragment.show(childFragmentManager)
    }

    private fun showOpenProfileAttentionDialog() {
        MeeraOpenProfileBottomSheetFragment.show(childFragmentManager)
    }

    companion object {
        private const val DELAY_RESTORE_SETTINGS = 5
    }
}
