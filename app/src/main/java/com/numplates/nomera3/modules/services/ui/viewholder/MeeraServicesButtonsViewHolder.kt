package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemServicesButtonsBinding
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction

class MeeraServicesButtonsViewHolder(
    private val binding: MeeraItemServicesButtonsBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ViewHolder(binding.root) {

    init {
        binding.vgServicesEvents.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.EventsClick) }
        binding.vgServicesPeople.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.PeoplesClick) }
        binding.vgServicesCommunities.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.CommunitiesClick) }
        binding.vgServicesSettings.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.SettingsClick) }
    }

}
