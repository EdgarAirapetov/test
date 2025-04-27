package com.numplates.nomera3.modules.moments.settings.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMomentSettingsBinding
import com.numplates.nomera3.modules.moments.settings.data.MomentSettingsEvent
import com.numplates.nomera3.modules.moments.settings.hidefrom.presentation.MomentSettingsHideFromAddUserFragment
import com.numplates.nomera3.modules.moments.settings.hidefrom.presentation.MomentSettingsHideFromFragment
import com.numplates.nomera3.modules.moments.settings.notshow.presentation.MomentSettingsNotShowFragment
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PRIVACY_TYPE_VALUE
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings.ChangeProfileSettingsListener
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

const val MOMENT_SETTINGS_REFRESH_NETWORK = "MOMENT_SETTINGS_REFRESH_NETWORK"
const val MOMENT_SETTINGS_REFRESH_CACHE = "MOMENT_SETTINGS_REFRESH_CACHE"
const val REFRESH_RESULT_EMPTY_KEY = "EMPTY_KEY"

class MomentSettingsFragment : BaseFragmentNew<FragmentMomentSettingsBinding>(), ChangeProfileSettingsListener {

    private var settingsAdapter: MomentSettingsAdapter? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMomentSettingsBinding
        get() = FragmentMomentSettingsBinding::inflate

    private val viewModel by viewModels<MomentSettingsViewModel> { App.component.getViewModelFactory() }

    private val momentsSettingsCallback =
        object : MomentSettingsAdapter.MomentSettingsAdapterCallback {
            override fun onShowMomentForFriendSwitch(isToggled: Boolean) {
                if (!isToggled && viewModel.isProfileClosed()) {
                    ChangeProfileSettingsBottomSheetFragment.getInstance().show(childFragmentManager)
                } else {
                    viewModel.setSetting(SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS, isToggled.toSettingsEnum())
                }
            }

            override fun onMomentWhoCanCommentClick(userTypeValue: Int) {
                checkAppRedesigned(
                    isRedesigned = {
                        val args = act.getBundle(Arg(ARG_PRIVACY_TYPE_VALUE, userTypeValue))
                        MeeraMomentSettingsAllowCommentFragment.show(childFragmentManager, args)
                    },
                    isNotRedesigned = { addBaseFragment(MomentSettingsAllowCommentFragment(), userTypeValue) }
                )
            }

            override fun onSaveToGallerySwitch() {
                checkWriteExternalPermission()
            }

            override fun onSaveToArchiveSwitch(isToggled: Boolean) {
                viewModel.setSetting(
                    settingEnum = SettingsKeyEnum.SAVE_MOMENTS_TO_ARCHIVE,
                    value = isToggled.toSettingsEnum()
                )
            }

            override fun notShowMomentsClick() {
                add(MomentSettingsNotShowFragment(), Act.LIGHT_STATUSBAR)
            }

            override fun hideFromMomentsClick(count: Int) {
                if (count > 0) {
                    addBaseFragment(MomentSettingsHideFromFragment(), Act.LIGHT_STATUSBAR)
                } else {
                    addBaseFragment(MomentSettingsHideFromAddUserFragment(), Act.LIGHT_STATUSBAR)
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = saveToExternalPermissions().all { key -> permissions[key] == true }
            if (isGranted) {
                viewModel.toggleSaveToGallery()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFragmentResultListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshUserSettingsFromCache()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupToolbar()
        initObservables()
    }

    override fun changeProfileSettingCanceled() = Unit
    override fun changeProfileSettingConfirmed() {
        viewModel.setSetting(
            settingEnum = SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS,
            value = false.toSettingsEnum(),
            shouldUpdate = true
        )
    }

    private fun initFragmentResultListener() {
        setFragmentResultListener(MOMENT_SETTINGS_REFRESH_CACHE) { _, bundle ->
            viewModel.refreshUserSettingsFromCache()
        }
        setFragmentResultListener(MOMENT_SETTINGS_REFRESH_NETWORK) { _, bundle ->
            viewModel.refreshUserSettingsFromNetwork()
        }
    }

    private fun Boolean.toSettingsEnum(): SettingsUserTypeEnum {
        return if (this) {
            SettingsUserTypeEnum.ALL
        } else {
            SettingsUserTypeEnum.NOBODY
        }
    }

    private fun initObservables() {
        viewModel.settingsState
            .catch { e -> Timber.e(e) }
            .onEach { settings -> handleSettingsData(settings) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.eventStream
            .catch { e -> Timber.e(e) }
            .onEach { event -> handleSettingsEvent(event) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupAdapter() {
        binding?.rvMomentsSettings?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            settingsAdapter = MomentSettingsAdapter(momentsSettingsCallback)
            settingsAdapter?.setHasStableIds(true)
            adapter = settingsAdapter
        }
    }

    private fun handleSettingsEvent(event: MomentSettingsEvent) {
        when (event) {
            MomentSettingsEvent.Error -> {
                showCommonError(R.string.settings_error_get_settings)
            }
        }
    }

    private fun handleSettingsData(settings: List<PrivacySettingUiModel>) {
        val items = listOf(
            MomentSettingsAdapter.VISIBLE_SETTINGS,
            MomentSettingsAdapter.COMMENT_SETTINGS,
            MomentSettingsAdapter.SAVE_SETTINGS
        ).map { type ->
            PrivacySettingsModel(
                viewType = type,
                isEnabled = true,
                settings = settings
            )
        }

        settingsAdapter?.addDataSet(items)
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
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            emptyList()
        }
    }

    private fun showRationaleWriteExternal() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.allow_access)
            .setMessage(R.string.file_moment_access_permissions)
            .setPositiveButton(R.string.open_settings) { _, _ -> sendUserToAppSettings() }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun setupToolbar() {
        val params = binding?.statusBarSettings?.layoutParams as? AppBarLayout.LayoutParams
        params?.height = context.getStatusBarHeight()
        binding?.statusBarSettings?.layoutParams = params
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbar?.setNavigationOnClickListener {
            if (act is MomentsWrapperActivity) {
                act.finish()
            } else {
                act.onBackPressed()
            }
        }
    }

    private fun addBaseFragment(fragment: BaseFragment, value: Int) {
        if (act is MomentsWrapperActivity) {
            fragment.arguments = act.getBundle(Arg(ARG_PRIVACY_TYPE_VALUE, value))
            (act as MomentsWrapperActivity).navigateTo(fragment)
        } else {
            act.addFragment(fragment, Act.LIGHT_STATUSBAR, Arg(ARG_PRIVACY_TYPE_VALUE, value))
        }
    }
}
