package com.numplates.nomera3.modules.communities.ui.fragment.holder

import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.MeeraEditGroupSwitchItemBinding
import com.numplates.nomera3.modules.communities.ui.fragment.MeeraCommunityEditAction

class MeeraCommunityEditSwitchHolder(
    val binding: MeeraEditGroupSwitchItemBinding,
    val listener: (action: MeeraCommunityEditAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(private: Boolean? = null, royalty: Boolean? = null) {

        private?.let {
            binding.vCloseCommunity.setCellRightElementChecked(it)
            binding.vOpenCommunity.setCellRightElementChecked(!it)
            if (it) {
                listener.invoke(MeeraCommunityEditAction.CloseCommunity(!it))
            } else {
                listener.invoke(MeeraCommunityEditAction.OpenCommunity(it))
            }
        }
        royalty?.let {
            binding.vOnlyAdministrationWrites.setCellRightElementChecked(it)
            listener.invoke(MeeraCommunityEditAction.OnlyAdministrationWrites(it))
        }

        binding.vOpenCommunity.apply {
            cellCityText = true
            setCellRightElementClickable(false)
            cellRightIconClickListener = {
                listener.invoke(
                    MeeraCommunityEditAction.OpenCommunity(!binding.vOpenCommunity.isCheckButton)
                )
                binding.vCloseCommunity.setCellRightElementChecked(binding.vOpenCommunity.isCheckButton)
            }
        }

        binding.vCloseCommunity.apply {
            cellCityText = true
            setCellRightElementClickable(false)
            cellRightIconClickListener = {
                listener.invoke(
                    MeeraCommunityEditAction.CloseCommunity(!binding.vCloseCommunity.isCheckButton)
                )
                binding.vOpenCommunity.setCellRightElementChecked(binding.vCloseCommunity.isCheckButton)
            }
        }


        binding.vOnlyAdministrationWrites.apply {
            cellCityText = true
            setCellRightElementClickable(false)
            cellRightIconClickListener = {
                setCellRightElementChecked(!isSwitchButton)
                listener.invoke(
                    MeeraCommunityEditAction.OnlyAdministrationWrites(binding.vOnlyAdministrationWrites.isSwitchButton)
                )
            }
        }
    }
}
