package com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders

import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoCheckboxItemBinding
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoAction
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoItemType
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer

class MeeraUserPersonalInfoCheckBoxItemHolder(
    val binding: MeeraUserPersonalInfoCheckboxItemBinding,
    val type: UserPersonalInfoItemType,
    val resources: Resources,
    private val actionListener: (UserPersonalInfoAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(userProfileContainer: UserPersonalInfoContainer) {
        if (userProfileContainer.isMale) {
            if (!binding.profileParamCheckboxMale.isCheckButton) {
                binding.profileParamCheckboxMale.toggleRightCheckbox()
            }
        } else {
            if (!binding.profileParamCheckboxFemale.isCheckButton) {
                binding.profileParamCheckboxFemale.toggleRightCheckbox()
            }
        }
        initClickListenerMale()
        initClickListenerFemale()
    }

    private fun initClickListenerMale(){
        binding.profileParamCheckboxFemale.apply {
            setRightElementContainerClickable(false)
            setThrottledClickListener {
                if(!binding.profileParamCheckboxFemale.isCheckButton){
                    binding.profileParamCheckboxFemale.setCellRightElementChecked(true)
                    binding.profileParamCheckboxMale.setCellRightElementChecked(false)
                    actionListener.invoke(
                        UserPersonalInfoAction.GenderItemClick(
                            isMale = !binding.profileParamCheckboxFemale.isCheckButton
                        )
                    )
                }
            }
        }
    }

    private fun initClickListenerFemale(){
        binding.profileParamCheckboxMale.apply {
            setRightElementContainerClickable(false)
            setThrottledClickListener {
                if(!binding.profileParamCheckboxMale.isCheckButton){
                    binding.profileParamCheckboxMale.setCellRightElementChecked(true)
                    binding.profileParamCheckboxFemale.setCellRightElementChecked(false)
                    actionListener.invoke(
                        UserPersonalInfoAction.GenderItemClick(
                            isMale = binding.profileParamCheckboxMale.isCheckButton
                        )
                    )
                }
            }
        }
    }
}
