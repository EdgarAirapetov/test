package com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders

import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.cell.CellLeftElement
import com.meera.uikit.widgets.cell.CellRightElement
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoSelectItemBinding
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoAction
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoItemType
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer
import timber.log.Timber

class MeeraUserPersonalInfoSelectItemHolder(
    val binding: MeeraUserPersonalInfoSelectItemBinding,
    val type: UserPersonalInfoItemType,
    val resources: Resources,
    private val actionListener: (UserPersonalInfoAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(userProfileContainer: UserPersonalInfoContainer) {
        when (type) {
            UserPersonalInfoItemType.BIRTHDAY -> {
                initSelectItem(
                    titleItem = resources.getString(R.string.user_personal_info_birth_header),
                    valueItem = userProfileContainer.birthdayStr,
                    rightElement = CellRightElement.NONE,
                    leftElement = CellLeftElement.NONE
                )
                binding.personalInfoSelectItem.setThrottledClickListener {
                    actionListener.invoke(
                        UserPersonalInfoAction.BirthdayItemClick(
                            tag = userProfileContainer.birthday
                        )
                    )
                }
            }

            UserPersonalInfoItemType.CITY -> {
                initSelectItem(
                    titleItem = resources.getString(R.string.city),
                    textError = userProfileContainer.cityNameTextError,
                    valueItem = userProfileContainer.cityName ?: "",
                    leftElement = CellLeftElement.NONE,
                    tag = userProfileContainer.cityId
                )
                binding.personalInfoSelectItem.apply {
                    setRightElementContainerClickable(false)
                    setTitleHintValue(
                        binding.root.resources.getString(R.string.meera_user_personal_info_city_hint)
                    )
                    setThrottledClickListener {
                        actionListener.invoke(
                            UserPersonalInfoAction.CityItemClick(
                                countryId = userProfileContainer.countryId
                            ) { city ->
                                initSelectItem(valueItem = city.title_, tag = city.cityId)
                            })
                    }
                }
            }

            UserPersonalInfoItemType.COUNTRY -> {
                binding.cvCountryFlagContainer.visible()
                initSelectItem(
                    titleItem = resources.getString(R.string.country),
                    valueItem = userProfileContainer.countryName,
                    flag = userProfileContainer.countryFlag,
                    tag = userProfileContainer.countryId
                )
                binding.personalInfoSelectItem.apply {
                    setRightElementContainerClickable(false)
                    setThrottledClickListener {
                        actionListener.invoke(UserPersonalInfoAction.CountryItemClick { country ->
                            initSelectItem(
                                flag = country.flag,
                                tag = country.id,
                                valueItem = country.name
                            )
                        })
                    }
                }
            }

            UserPersonalInfoItemType.ACCOUNT_MANAGEMENT -> {
                initSelectItem(
                    titleItem = resources.getString(R.string.account_management),
                    valueItem = resources.getString(R.string.account_remove),
                    rightElement = CellRightElement.NONE
                )
                binding.personalInfoSelectItem.setLeftIcon(R.drawable.ic_outlined_delete_m)
                binding.personalInfoSelectItem.cellLeftIconAndTitleColor = R.color.profile_statistics_red
                binding.personalInfoSelectItem.setThrottledClickListener {
                    actionListener.invoke(UserPersonalInfoAction.DeleteItemClick())
                }
            }

            else -> {
                Timber.i("Unknown field type")
            }
        }
    }

    private fun initSelectItem(
        titleItem: String? = null,
        valueItem: String? = null,
        rightElement: CellRightElement? = null,
        leftElement: CellLeftElement? = null,
        flag: String? = null,
        tag: Any? = null,
        textError: String? = null
    ){
        titleItem?.let { binding.tvPersonalInfoInputHeader.text = titleItem }
        valueItem?.let { binding.personalInfoSelectItem.setTitleValue(valueItem) }
        rightElement?.let { binding.personalInfoSelectItem.cellRightElement = rightElement }
        leftElement?.let { binding.personalInfoSelectItem.cellLeftElement =  leftElement}
        flag?.let { binding.ivCountryFlag.loadGlide(flag) }
        tag?.let { binding.personalInfoSelectItem.tag = tag }
        textError?.let {text ->
            binding.vgErrorContainer.visible()
            binding.tvErrorMessage.text = text
        } ?: binding.vgErrorContainer.gone()
    }
}
