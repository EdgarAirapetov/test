package com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoTextItemBinding
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoAction
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoItemType
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer
import timber.log.Timber

class MeeraUserPersonalInfoTextItemHolder(
    val binding: MeeraUserPersonalInfoTextItemBinding,
    val type: UserPersonalInfoItemType,
    val resources: Resources,
    private val actionListener: (UserPersonalInfoAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(userProfileContainer: UserPersonalInfoContainer) {
        when (type) {
            UserPersonalInfoItemType.PHONE_NUMBER -> {
                userProfileContainer.phoneNumber?.let { phone ->
                    fillingFields(
                        title = phone,
                        subtitle = resources.getString(R.string.account_phone_number),
                        icon = null
                    )
                    binding.ivEyeContainer.setThrottledClickListener {
                        actionListener.invoke(UserPersonalInfoAction.PhoneItemClick { phone ->
                            phone.phoneNumber?.let {
                                checkVisibilityContactInformation(!phone.isHidden, it)
                            }
                        })
                    }
                }
            }

            UserPersonalInfoItemType.EMAIL -> {
                userProfileContainer.email?.let { mail ->
                    fillingFields(title = mail, subtitle = resources.getString(R.string.account_mail), icon = null)
                    binding.ivEyeContainer.setThrottledClickListener {
                        actionListener.invoke(UserPersonalInfoAction.MailItemClick { userEmail ->
                            userEmail.email?.let {
                                checkVisibilityContactInformation(!userEmail.isHidden, it)
                            }
                        }
                        )
                    }
                }
            }

            else -> {
                Timber.i("Unknown field type")
            }
        }
    }

    private fun checkVisibilityContactInformation(isHidden: Boolean, contact: String) {
        if (isHidden) {
            fillingFields(title = contact, subtitle = null, icon = R.drawable.ic_outlined_eye_m)
        } else {
            fillingFields(title = contact, subtitle = null, icon = R.drawable.ic_outlined_eye_off_m)
        }
    }

    private fun fillingFields(title: String?, subtitle: String?, icon: Int?) {
        title?.let {
            binding.personalInfoTextItem.setTitleValue(title)
        }
        subtitle?.let {
            binding.personalInfoTextItem.setDescriptionValue(subtitle)
        }
        icon?.let {
            binding.ivEye.setImageResource(icon)
        }
        binding.personalInfoTextItem.visibility = View.VISIBLE
    }
}
