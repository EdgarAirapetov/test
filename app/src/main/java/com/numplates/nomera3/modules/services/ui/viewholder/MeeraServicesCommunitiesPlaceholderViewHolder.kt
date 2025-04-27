package com.numplates.nomera3.modules.services.ui.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemServicesCommunitiesPlaceholderBinding
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction

class MeeraServicesCommunitiesPlaceholderViewHolder(
    private val binding: MeeraItemServicesCommunitiesPlaceholderBinding,
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ViewHolder(binding.root) {

    init {
        binding.btnFindCommunity.setThrottledClickListener { actionListener.invoke(MeeraServicesUiAction.CommunitiesClick) }
    }

}
