package com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraPushNotifSettingsFragmentBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.ARG_CHANGE_LIST_USER_KEY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.ARG_CHANGE_LIST_USER_REQUEST_KEY
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

private const val RECYCLER_CONTENTS_COUNT = 37

class MeeraPushNotificationSettingsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_push_notif_settings_fragment,
    behaviourConfigState = ScreenBehaviourState.Full
) {

    private val viewModel: PushNotificationsSettingsViewModel by viewModels { App.component.getViewModelFactory() }
    private val adapter by lazy(LazyThreadSafetyMode.NONE) { PushSettingsAdapter(viewModel::onAction) }
    override val containerId: Int
        get() = R.id.fragment_first_container_view
    private val binding by viewBinding(MeeraPushNotifSettingsFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecyclerView()
        viewModel.onAction(MeeraPushNotificationSettingsAction.OnViewCreated)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pushSettingState.collect { state ->
                    if (state.isError) {
                        Timber.d("Error receiving notification settings")
                    } else {
                        adapter.submitList(state.items)
                    }
                }
            }
        }
        viewModel.notificationAction.onEach { action ->
            notificationClickAction(action)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        parentFragmentManager.setFragmentResultListener(
            ARG_CHANGE_LIST_USER_REQUEST_KEY, viewLifecycleOwner
        ) { requestKey, bundle ->
            val changeProfile =
                bundle.getBoolean(ARG_CHANGE_LIST_USER_KEY, false)
            if (changeProfile) viewModel.onAction(MeeraPushNotificationSettingsAction.OnViewCreated)
        }
    }

    private fun initToolbar() {
        binding?.apply {
            pushNotifSettingsFragmentNaw.title = getString(R.string.notification_on_turn_on_push)
            pushNotifSettingsFragmentNaw.backButtonClickListener = { findNavController().popBackStack() }
            pushNotifSettingsContent.let { recycler -> binding?.pushNotifSettingsFragmentNaw?.addScrollableView(recycler) }
        }
    }

    private fun initRecyclerView(){
        binding?.pushNotifSettingsContent?.setItemViewCacheSize(RECYCLER_CONTENTS_COUNT)
        binding?.pushNotifSettingsContent?.adapter = adapter
    }

    private fun notificationClickAction(action: MeeraPushNotificationSettingsAction?) {
        when (action) {
            MeeraPushNotificationSettingsAction.ShowMessageNotificationUserFragment -> {
                findNavController()
                    .safeNavigate(
                        R.id.action_meeraPushNotificationSettingsFragment_to_meeraMessageNotificationsUsersFragment
                    )
            }

            MeeraPushNotificationSettingsAction.ShowMessageNotificationAddUserFragment -> {
                findNavController()
                    .safeNavigate(
                        R.id.action_meeraPushNotificationSettingsFragment_to_meeraMessageNotificationsAddUsersFragment
                    )
            }

            MeeraPushNotificationSettingsAction.ShowSubscriptionsNotificationUsersFragment -> {
                findNavController()
                    .safeNavigate(
                        R.id.action_meeraPushNotificationSettingsFragment_to_meeraSubscriptionsNotificationUsersFragment
                    )
            }

            MeeraPushNotificationSettingsAction.ShowSubscriptionsNotificationAddUsersFragment -> {
                findNavController()
                    .safeNavigate(
                        R.id.action_meeraPushNotificationSettingsFragment_to_meeraSubscriptionsNotificationAddUsersFragment
                    )
            }

            is MeeraPushNotificationSettingsAction.UpdateOtherSetting -> {
                adapter.submitList(action.listSettings)
            }

            else -> {
                Timber.d("An unknown was received notification settings action")
            }
        }
    }
}
