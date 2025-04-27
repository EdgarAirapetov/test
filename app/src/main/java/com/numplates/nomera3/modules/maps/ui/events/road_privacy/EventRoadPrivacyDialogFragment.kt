package com.numplates.nomera3.modules.maps.ui.events.road_privacy

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentDialogMapPrivacyBinding
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.model.EventRoadPrivacyEvent
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class EventRoadPrivacyDialogFragment : DialogFragment() {

    private val viewModel: EventRoadPrivacyDialogViewModel by viewModels { App.component.getViewModelFactory() }
    private var windowAnimations: Int = NO_WINDOW_ANIMATIONS
    private var restoreWindowAnimationsJob: Job? = null
    private var binding: FragmentDialogMapPrivacyBinding? = null

    private var isPrivacyAllSet = false

    override fun getTheme(): Int {
        return R.style.RoadPrivacyDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_map_privacy, container, false)
        binding = FragmentDialogMapPrivacyBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        windowAnimations = savedInstanceState
            ?.getInt(KEY_WINDOW_ANIMATIONS, NO_WINDOW_ANIMATIONS)
            ?: dialog?.window?.attributes?.windowAnimations ?: NO_WINDOW_ANIMATIONS

        (arguments?.getSerializable(KEY_CURRENT_ROAD_PRIVACY_SETTING) as? SettingsUserTypeEnum)?.let {
            viewModel.setRoadPrivacy(it)
        }

        binding?.apply {
            ibEventRoadPrivacyClose.setThrottledClickListener {
                dismiss()
            }
            tvEventPrivacyPublish.setThrottledClickListener {
                viewModel.setRoadVisibilityToAll()
            }
            rbEventPrivacyEverybody.setThrottledClickListener { viewModel.setRoadPrivacy(SettingsUserTypeEnum.ALL) }
            rbEventPrivacyFriends.setThrottledClickListener { viewModel.setRoadPrivacy(SettingsUserTypeEnum.FRIENDS) }
            rbEventPrivacyNobody.setThrottledClickListener { viewModel.setRoadPrivacy(SettingsUserTypeEnum.NOBODY) }
        }
        viewModel.liveUiModel.observe(viewLifecycleOwner) { uiModel ->
            binding?.tvEventPrivacyPublish?.isEnabled = uiModel.isPublishEnabled
            when (uiModel.roadPrivacySettingValue) {
                SettingsUserTypeEnum.NOBODY -> R.id.rb_event_privacy_nobody
                SettingsUserTypeEnum.ALL -> R.id.rb_event_privacy_everybody
                SettingsUserTypeEnum.FRIENDS -> R.id.rb_event_privacy_friends
            }.let {
                binding?.rgEventPrivacySelector?.check(it)
            }
        }
        viewModel.eventFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach(::handleEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        restoreWindowAnimationsJob = doDelayed(100) {
            dialog?.window?.setWindowAnimations(windowAnimations)
        }
    }

    override fun onPause() {
        super.onPause()
        restoreWindowAnimationsJob?.cancel()
        dialog?.window?.setWindowAnimations(NO_WINDOW_ANIMATIONS)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isPrivacyAllSet.not()) {
            (parentFragment as? EventRoadPrivacyDialogHost)?.onEventRoadDialogPrivacyCancelled()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_WINDOW_ANIMATIONS, windowAnimations)
        super.onSaveInstanceState(outState)
    }

    private fun handleEvent(event: EventRoadPrivacyEvent) {
        when (event) {
            EventRoadPrivacyEvent.RoadPrivacyAllIsSet -> {
                isPrivacyAllSet = true
                (parentFragment as? EventRoadPrivacyDialogHost)?.onEventRoadDialogPrivacyAllIsSet()
                dismiss()
            }
        }
    }

    companion object {
        private const val KEY_CURRENT_ROAD_PRIVACY_SETTING = "KEY_CURRENT_ROAD_PRIVACY_SETTING"
        private const val KEY_WINDOW_ANIMATIONS = "KEY_WINDOW_ANIMATIONS"
        private const val NO_WINDOW_ANIMATIONS = -1

        fun getInstance(currentRoadPrivacySetting: SettingsUserTypeEnum): EventRoadPrivacyDialogFragment =
            EventRoadPrivacyDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_CURRENT_ROAD_PRIVACY_SETTING, currentRoadPrivacySetting)
                }
            }
    }
}
