package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideIfNullOrEmpty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.VehicleEntity
import com.meera.db.models.userprofile.VehicleType
import com.meera.uikit.widgets.VehiclePlateTypeSize
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemSearchResultUserBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.search.ui.fragment.AT_SIGN
import com.numplates.nomera3.presentation.view.widgets.numberplateview.getNumberPlateEnum

class MeeraUserSearchResultViewHolder(
    private val binding: MeeraItemSearchResultUserBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<UserSearchResultUiEntity, MeeraItemSearchResultUserBinding>(binding) {

    init {
        binding.apply {
            root.setThrottledClickListener {
                item?.let { user ->
                    actionListener.invoke(FriendsContentActions.SelectUserSearchResultUiAction(user))
                }
            }
            btnSearchResultAdd.setThrottledClickListener {
                btnSearchResultAdd.clickAnimate()
                item?.let { user ->
                    actionListener.invoke(FriendsContentActions.AddUserSearchResultUiAction(user))
                }
            }
            upiSearchResultUser.setThrottledClickListener {
                item?.let { user->
                    if(user.hasMoments) {
                        actionListener.invoke(FriendsContentActions.OpenUserMomentsAction(upiSearchResultUser, user))
                    } else {
                        actionListener.invoke(FriendsContentActions.SelectUserSearchResultUiAction(user))
                    }
                }
            }
        }
    }

    override fun bind(item: UserSearchResultUiEntity) {
        super.bind(item)
        binding.tvSearchResultUserName.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                customIconTopContent = R.drawable.ic_approved_author_gold_10,
                approvedIconSize = ApprovedIconSize.SMALL,
                approved = item.approved.toBoolean(),
                interestingAuthor = item.topContentMaker.toBoolean()
            )
        )
        renderButton(item.buttonState)
        val storiesState = when {
            item.hasMoments && item.hasNewMoments -> UserpicStoriesStateEnum.NEW
            item.hasMoments -> UserpicStoriesStateEnum.VIEWED
            else -> UserpicStoriesStateEnum.NO_STORIES
        }
        binding.upiSearchResultUser.setConfig(
            UserpicUiModel(
                userAvatarUrl = item.avatarImage,
                userName = item.name,
                storiesState = storiesState
            )
        )
        binding.tvSearchResultUserName.text = item.name
        binding.tvSearchResultUserTag.hideIfNullOrEmpty("$AT_SIGN${item.tagName}")
        binding.tvSearchResultUserAdditionalInfo.hideIfNullOrEmpty(
            if (item.isMyProfile) itemView.context.getString(R.string.search_my_profile) else item.additionalInfo
        )
        setupPlate(item.vehicle)
    }

    private fun renderButton(buttonState: UserSearchResultUiEntity.ButtonState) {
        when (buttonState) {
            UserSearchResultUiEntity.ButtonState.Hide -> {
                binding.btnSearchResultAdd.gone()
            }

            UserSearchResultUiEntity.ButtonState.ShowAdd -> {
                binding.btnSearchResultAdd.src = R.drawable.ic_outlined_user_add_l
                binding.btnSearchResultAdd.visible()
            }

            UserSearchResultUiEntity.ButtonState.ShowIncome -> {
                binding.btnSearchResultAdd.src = R.drawable.ic_outlined_user_response_l
                binding.btnSearchResultAdd.visible()
            }
        }
    }

    private fun setupPlate(vehicle: VehicleEntity?) {
        if (vehicle == null || vehicle.number.isNullOrEmpty()) {
            binding.vpvSearchResult.gone()
        } else {
            val type = vehicle.type ?: return
            val country = vehicle.country ?: return
            val plateEnum = getNumberPlateEnum(type.typeId, country.countryId) ?: return
            val numberMaskCount = StringBuilder().apply {
                plateEnum.prefixPattern?.let { append(it) }
                append(plateEnum.numPattern)
                plateEnum.suffixPattern?.let { append(it) }
            }.toString().length
            var number = vehicle.number?.take(numberMaskCount) + " " + vehicle.number?.drop(numberMaskCount)
            val typeSize = when (type.typeId) {
                VehicleType.TYPE_ID_CAR -> VehiclePlateTypeSize.SMALL_AUTO
                VehicleType.TYPE_ID_MOTO -> VehiclePlateTypeSize.SMALL_MOTO
                else -> return
            }
            if (type.typeId == VehicleType.TYPE_ID_MOTO) {
                val numberCount = numberMaskCount - (plateEnum.suffixPattern?.length ?: 0)
                number = number.substring(0, numberCount) + "\n" + number.substring(numberCount)
            }
            binding.vpvSearchResult.setTypeSize(typeSize)
            binding.vpvSearchResult.visible()
            binding.vpvSearchResult.text = number
        }
    }

}

