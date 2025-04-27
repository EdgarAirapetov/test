package com.numplates.nomera3.modules.communities.ui.fragment.dashboard.holder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraCommunitySettingsDeleteGroupItemBinding
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.MeeraBaseCommunityDashboardAction

class MeeraBaseCommunitySettingsDeleteGroupHolder(
    private val binding: MeeraCommunitySettingsDeleteGroupItemBinding,
    private val clickListener: (listener: MeeraBaseCommunityDashboardAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(deleteIsVisible: Boolean) {
        if (!deleteIsVisible) {
            binding.vBandMembers.gone()
        } else {
            binding.vBandMembers.apply {
                cellCityText = false
                setRightElementContainerClickable(false)
                setThrottledClickListener {
                    clickListener.invoke(MeeraBaseCommunityDashboardAction.DeleteGroup(
                        clickableState = {
                            isClickable = it
                        },
                        progressBarState = {
                            if (it) binding.pbDeleteGroup.visible() else binding.pbDeleteGroup.gone()
                        }
                    ))
                }
            }
        }

    }
}
