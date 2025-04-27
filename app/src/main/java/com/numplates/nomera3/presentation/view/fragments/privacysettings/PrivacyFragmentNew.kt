package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.Manifest
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.spToPx
import com.meera.core.extensions.toInt
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPrivacyNewBinding
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.modules.moments.settings.presentation.MomentSettingsFragment
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.NOT_PUBLIC
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.PRIVATE_ROAD
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COUNT_USERS_BLACKLIST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COUNT_USERS_WHITELIST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MESSAGE_SETTINGS_USER_EXCLUSIONS_COUNT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PRIVACY_TYPE_VALUE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PROFILE_CLOSED_VALUE
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.closeprofile.CloseProfileBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.closeprofile.CloseProfileListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.openprofile.OpenProfileBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.openprofile.OpenProfileListener
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_BLACKLIST
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_COMMON
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_COMMUNICATION
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_MAP
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_MOMENTS
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_MY_BIRTHDAY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_PROFILE
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_RESTORE_DEFAULTS
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_ROAD
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_SHAKE
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserTypeFragment
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.PrivacySettingsViewEvent
import timber.log.Timber

class PrivacyFragmentNew : BaseFragmentNew<FragmentPrivacyNewBinding>(),
    PrivacySettingsAdapter.IPrivacySettingsInteractor, CloseProfileListener, OpenProfileListener,
    ChangeProfileSettingsListener ,    BasePermission by BasePermissionDelegate() {

    private val viewModel by viewModels<PrivacyNewViewModel>()

    private lateinit var settingsAdapter: PrivacySettingsAdapter

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy { SyncContactsDialogDelegate(childFragmentManager) }

    private var undoSnackBar: NSnackbar? = null
    private var restoreDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initRecycler()
        initLiveObservables()
        initPermission()
        if (savedInstanceState == null) {
            viewModel.subscribeToPrivacySettings()
        }
    }

    override fun onStartFragment() {
        super.onStartFragment()
        restoreDialog?.show()
    }

    override fun onStopFragment() {
        super.onStopFragment()
        undoSnackBar?.dismiss()
        restoreDialog?.dismiss()
    }

    private fun initPermission() {
        initPermissionDelegate(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner
        )
    }

    private fun setupToolbar() {
        val params = binding?.statusBarSettings?.layoutParams as? AppBarLayout.LayoutParams
        params?.height = context.getStatusBarHeight()
        binding?.statusBarSettings?.layoutParams = params
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbar?.setNavigationOnClickListener { act.onBackPressed() }
    }

    private fun initRecycler() {
        binding?.rvPrivacy?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            settingsAdapter = PrivacySettingsAdapter(this@PrivacyFragmentNew)
            adapter = settingsAdapter
        }
    }

    fun initLiveObservables() {
        viewModel.liveSettings.observe(viewLifecycleOwner) { settings ->
            handleSettingsData(settings)
        }
        viewModel.liveViewEvents.observe(viewLifecycleOwner) { viewEvent ->
            handleViewEvents(viewEvent)
        }
        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            handlePrivacyScreenEvent(event)
        }
    }

    private fun handleSettingsData(settings: List<PrivacySettingUiModel>) {
        val items = listOf(
            SETTING_ITEM_TYPE_PROFILE,
            SETTING_ITEM_TYPE_COMMON,
            SETTING_ITEM_TYPE_MAP,
            SETTING_ITEM_TYPE_COMMUNICATION,
            SETTING_ITEM_TYPE_MY_BIRTHDAY,
            SETTING_ITEM_TYPE_ROAD,
            SETTING_ITEM_TYPE_SHAKE,
            SETTING_ITEM_TYPE_MOMENTS,
            SETTING_ITEM_TYPE_BLACKLIST,
            SETTING_ITEM_TYPE_RESTORE_DEFAULTS
        ).map { type ->
            PrivacySettingsModel(
                viewType = type,
                isEnabled = true,
                settings = settings
            )
        }.filter { model ->
            when (model.viewType) {
                SETTING_ITEM_TYPE_SHAKE -> viewModel.isShakeToggleEnabled()
                else -> true
            }
        }
        settingsAdapter.addDataSet(items)
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
            is PrivacyNewViewModel.PrivacyScreenEvent.SettingsRestored -> {
                showSuccessSnackBar(R.string.snack_bar_nofitification_title_settings_reset)
            }
            is PrivacyNewViewModel.PrivacyScreenEvent.InternetConnectionError -> {
                showErrorToast(R.string.error_check_internet)
            }
            is PrivacyNewViewModel.PrivacyScreenEvent.StartUndoTimer -> {
                showUndoRestoreSettings()
            }
            is PrivacyNewViewModel.PrivacyScreenEvent.ShowContactsSyncPermissionUiEffect -> {
                requestContactsSyncPermission(
                    key = event.key,
                    turnOn = event.enabled
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
        ConfirmDialogBuilder()
            .setHeader(requireContext().getString(R.string.turn_off_sync_contacts))
            .setDescription(requireContext().getString(R.string.turn_off_sync_description))
            .setRightBtnText(requireContext().getString(R.string.general_turn_off))
            .setRightClickListener {
                viewModel.setSetting(
                    key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key,
                    value = false.toInt(),
                    needPushAmplitude = false
                )
            }
            .setLeftBtnText(requireContext().getString(R.string.general_cancel))
            .show(childFragmentManager)
    }

    private fun showSyncContactsDialogPermissionDenied() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.contacts_synchronization_allow_access,
            descriptionRes = R.string.contacts_sync_allow_in_settings_description,
            positiveButtonRes = R.string.go_to_settings,
            positiveButtonAction = { sendUserToAppSettings() },
            negativeButtonRes = R.string.general_later,
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_dialog else R.drawable.ic_sync_contacts_dialog
        )
    }

    private fun showSyncContactsDialogSuccess(syncCount: Int) {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            positiveButtonAction = { viewModel.logSuccessDialogClosedByButton(true, syncCount) },
            closeDialogDismissListener = { viewModel.logSuccessDialogClosedByButton(false, syncCount) },
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_done else R.drawable.ic_sync_contacts_done
        )
    }

    private fun requestContactsSyncPermission(
        key: String,
        turnOn: Int
    ) {
        setPermissions(
            permission = Manifest.permission.READ_CONTACTS,
            listener = object: PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.setSetting(
                        key = key,
                        value = turnOn,
                        needPushAmplitude = false
                    )
                }
                override fun onError(error: Throwable?) {
                    Timber.e(error)
                }
            }
        )
    }

    private fun showErrorToast(@StringRes textRes: Int) {
        NToast.with(view)
            .text(getString(textRes))
            .typeError()
            .show()
    }

    private fun showSuccessSnackBar(@StringRes textRes: Int) {
        NSnackbar.with(requireView())
            .typeSuccess()
            .marginBottom(MARGIN_BOTTOM_SNACK_BAR_VALUE)
            .text(getString(textRes))
            .show()
    }

    private fun buildCustomTitleTextView(): TextView {
        val sidePaddingsDp = 24
        val extraLineSpacingSp = 3f
        val textSizeSp = 20f
        return TextView(requireContext()).apply {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.source_sans_pro)
            setText(R.string.popup_restore_settings_to_default)
            setPadding(dpToPx(sidePaddingsDp))
            setTextColor(Color.BLACK)
            setLineSpacing(spToPx(extraLineSpacingSp), lineSpacingMultiplier)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
        }
    }

    private fun showUndoRestoreSettings() {
        undoSnackBar?.dismissNoCallbacks()
        undoSnackBar = NSnackbar.with(requireView())
            .inView(requireView())
            .text(resources.getString(R.string.snack_bar_nofitification_title_settings_reset))
            .description(resources.getString(R.string.snack_bar_nofitification_description_touch_to_cancel))
            .durationIndefinite()
            .marginBottom(MARGIN_BOTTOM_SNACK_BAR_VALUE)
            .button(resources.getString(R.string.general_cancel))
            .timer(DELAY_RESTORE_SETTINGS) {
                viewModel.restoreDefaultSettings()
            }
            .show()
    }

    override fun switchGender(key: String, isEnabled: Boolean) {
        viewModel.setSetting(key, isEnabled.toInt())
    }

    override fun switchAge(key: String, isEnabled: Boolean) {
        if (viewModel.closedProfile() && isEnabled) {
            ChangeProfileSettingsBottomSheetFragment.getInstance().show(childFragmentManager)
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
                checkAppRedesigned(
                    isRedesigned = {},
                    isNotRedesigned = {
                        add(
                            RoadSettingsFragment(), Act.LIGHT_STATUSBAR,
                            Arg(ARG_MESSAGE_SETTINGS_USER_EXCLUSIONS_COUNT, count.toLong())
                        )
                    }
                )
            }
        }
    }

    override fun switchAntiObscene(key: String, isEnabled: Boolean) {
        viewModel.setSetting(key, isEnabled.toInt())
    }

    override fun switchShake(key: String, isEnabled: Boolean) {
        viewModel.handleShakeEnabledUiAction(
            key = key,
            enabled = isEnabled
        )
    }

    override fun switchShareScreenshot(key: String, isEnabled: Boolean) {
        viewModel.handleShareScreenshotSwitchAction(
            key = key,
            enabled = isEnabled
        )
    }

    override fun switchContactSync(key: String, isEnabled: Boolean) {
        viewModel.handleSyncContactSwitchAction(
            key = key,
            enabled = isEnabled
        )
    }

    override fun switchNewAvatarPost(key: String, isEnabled: Boolean) {
        viewModel.setSetting(key, if (isEnabled) PRIVATE_ROAD.state else NOT_PUBLIC.state)
    }

    override fun clickMomentSettings() {
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                add(MomentSettingsFragment(), Act.LIGHT_STATUSBAR)
            }
        )
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
        addWhiteBlackListFragment(OnlineSettingsFragment(), value, countBlacklist, countWhitelist)
    }

    override fun clickMapPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        viewModel.mapPermissionsClicked()
        add(
            MapSettingsFragment(), Act.LIGHT_STATUSBAR,
            Arg(ARG_PRIVACY_TYPE_VALUE, value),
            Arg(ARG_PROFILE_CLOSED_VALUE, viewModel.closedProfile()),
            Arg(ARG_COUNT_USERS_BLACKLIST, countBlacklist),
            Arg(ARG_COUNT_USERS_WHITELIST, countWhitelist),
            Arg(MapVisibilitySettingsOrigin.ARG, MapVisibilitySettingsOrigin.SETTINGS)
        )
    }

    override fun clickCallPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        addWhiteBlackListFragment(CallSettingsFragmentV2(), value, countBlacklist, countWhitelist)
    }

    override fun clickPersonalMessages(value: Int?, countBlacklist: Int?, countWhitelist: Int?) {
        addWhiteBlackListFragment(PersonalMessagesFragment(), value, countBlacklist, countWhitelist)
    }

    override fun onFriendsAndFollowersClicked(value: Int?) =
        addPrivacyFragment(ShowFriendsSubscribersPrivacyFragment(), value)

    override fun clickBlacklistUsers(count: Int?) {
        count?.let {
            if (count > 0) {
                add(BlacklistSettingsUsersFragment(), Act.LIGHT_STATUSBAR)
            } else {
                add(BlacklistSettingsAddUsersFragment(), Act.LIGHT_STATUSBAR)
            }
        }
    }

    override fun clickAboutMePrivacy(value: Int?) = addPrivacyFragment(AboutMeSettingsFragment(), value)

    override fun clickGaragePrivacy(value: Int?) = addPrivacyFragment(GarageSettingsFragment(), value)

    override fun clickGiftPrivacy(value: Int?) = addPrivacyFragment(GiftsSettingsFragment(), value)

    override fun clickPersonalFeedPrivacy(value: Int?) = addPrivacyFragment(PersonalFeedSettingsFragment(), value)

    override fun clickBirthdayDetails(value: Int?) = addPrivacyFragment(BirthdaySettingsFragment(), value)

    override fun clickRestoreDefaultSettings() {
        restoreDialog?.dismiss()
        restoreDialog = AlertDialog.Builder(requireContext()).apply {
            setCancelable(false)
            setCustomTitle(buildCustomTitleTextView())
            setPositiveButton(R.string.general_restore) { _, _ ->
                viewModel.checkNetworkAndRestoreSettings()
                restoreDialog = null
            }
            setNegativeButton(R.string.general_cancel) { dialog, _ ->
                dialog.cancel()
                restoreDialog = null
            }
        }.show()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPrivacyNewBinding
        get() = FragmentPrivacyNewBinding::inflate

    private fun addPrivacyFragment(fragment: BaseSettingsUserTypeFragment, value: Int?) {
        act.addFragment(
            fragment,
            Act.LIGHT_STATUSBAR,
            Arg(ARG_PRIVACY_TYPE_VALUE, value),
            Arg(ARG_PROFILE_CLOSED_VALUE, viewModel.closedProfile())
        )
    }

    private fun addWhiteBlackListFragment(
        fragment: BaseSettingsUserTypeFragment,
        value: Int?,
        countBlacklist: Int?,
        countWhitelist: Int?
    ) {
        act.addFragment(
            fragment, Act.LIGHT_STATUSBAR,
            Arg(ARG_PRIVACY_TYPE_VALUE, value),
            Arg(ARG_PROFILE_CLOSED_VALUE, viewModel.closedProfile()),
            Arg(ARG_COUNT_USERS_BLACKLIST, countBlacklist),
            Arg(ARG_COUNT_USERS_WHITELIST, countWhitelist)
        )
    }

    private fun showCloseProfileAttentionDialog() {
        CloseProfileBottomSheetFragment.getInstance().show(childFragmentManager)
    }

    private fun showOpenProfileAttentionDialog() {
        OpenProfileBottomSheetFragment.getInstance().show(childFragmentManager)
    }

    companion object {
        private const val MARGIN_BOTTOM_SNACK_BAR_VALUE = 0
        private const val DELAY_RESTORE_SETTINGS = 5
    }
}
