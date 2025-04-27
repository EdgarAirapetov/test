package com.numplates.nomera3.modules.redesign.fragments.main.map.events

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraDialogEventsAboutPopupBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventsInfoUiModel
import com.numplates.nomera3.modules.maps.ui.view.MapBottomSheetDialog

class MeeraEventsAboutPopupDialog(
    activity: FragmentActivity,
    private val eventsInfo: EventsInfoUiModel,
    private val onAboutClosed: (isConfirmed :Boolean) -> Unit,
    private val onRulesOpen: (() -> Unit)
) : MapBottomSheetDialog(activity) {

    private val binding: MeeraDialogEventsAboutPopupBinding = MeeraDialogEventsAboutPopupBinding.inflate(layoutInflater)
    private var isConfirmed: Boolean = false

    init {
        setContentView(binding.root)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.eivEventsAboutInfo.setModel(eventsInfo)
        binding.eivEventsAboutInfo.rulesOpenListener = onRulesOpen
        binding.tvEventsAboutOkay.setThrottledClickListener {
            isConfirmed = true
            dismiss()
        }
        binding.ibMapEventsAboutClose.setThrottledClickListener {
            dismiss()
        }
        setOnDismissListener {
            onAboutClosed.invoke(isConfirmed)
        }
    }
}
