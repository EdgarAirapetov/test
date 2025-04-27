package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideIfNullOrEmpty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.SearchResultUserItemBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.search.ui.fragment.AT_SIGN
import com.numplates.nomera3.presentation.utils.setSmallSize
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView

class UserSearchResultViewHolder(
    private val binding: SearchResultUserItemBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<UserSearchResultUiEntity, SearchResultUserItemBinding>(binding) {

    init {
        binding.apply {
            root.setThrottledClickListener {
                item?.let { user ->
                    actionListener.invoke(FriendsContentActions.SelectUserSearchResultUiAction(user))
                }
            }
            addButton.setThrottledClickListener {
                addButton.clickAnimate()
                item?.let { user ->
                    actionListener.invoke(FriendsContentActions.AddUserSearchResultUiAction(user))
                }
            }
            avatarView.setThrottledClickListener {
                item?.let { user->
                    if(user.hasMoments) {
                        actionListener.invoke(FriendsContentActions.OpenUserMomentsAction(avatarView, user))
                    } else {
                        actionListener.invoke(FriendsContentActions.SelectUserSearchResultUiAction(user))
                    }
                }
            }
        }
    }

    override fun bind(item: UserSearchResultUiEntity) {
        super.bind(item)
        renderVehicle(item)
        renderButton(item.buttonState)

        binding.avatarView.setUp(
            itemView.context,
            item.avatarImage,
            item.accountType.value,
            item.accountColor,
            hasMoments = item.hasMoments,
            hasNewMoments = item.hasNewMoments
        )

        binding.nameText.text = item.name
        setApprovedIcon(item)
        binding.tagText.hideIfNullOrEmpty("$AT_SIGN${item.tagName}")
        binding.additionalInfo.hideIfNullOrEmpty(
            if (item.isMyProfile) itemView.context.getString(R.string.search_my_profile) else item.additionalInfo
        )
    }

    private fun renderVehicle(item: UserSearchResultUiEntity) {
        val vehicle = item.vehicle

        if (vehicle?.number == null || vehicle.country == null) {
            binding.numberPlate.gone()
            return
        } else {
            binding.numberPlate.visible()
            NumberPlateEditView.Builder(binding.numberPlate)
                .setVehicleNew(vehicle.number, vehicle.country?.countryId, vehicle.type?.typeId)
                .build()

            binding.numberPlate.setBackgroundPlate(
                vehicle.type?.typeId,
                vehicle.country?.countryId,
                item.accountType.value,
                item.accountColor
            )

            binding.numberPlate.setSmallSize(vehicle.type?.typeId ?: 1)
            binding.numberPlate.visible()
        }
    }

    private fun renderButton(buttonState: UserSearchResultUiEntity.ButtonState) {
        when (buttonState) {
            UserSearchResultUiEntity.ButtonState.Hide -> {
                binding.addButton.gone()
            }

            UserSearchResultUiEntity.ButtonState.ShowAdd -> {
                binding.addButton.setImageResource(R.drawable.ic_add_friend_purple_32)
                binding.addButton.visible()
            }

            UserSearchResultUiEntity.ButtonState.ShowIncome -> {
                binding.addButton.setImageResource(R.drawable.ic_incoming_friend_purple)
                binding.addButton.visible()
            }
        }
    }

    private fun setApprovedIcon(item: UserSearchResultUiEntity) {
        binding.nameText.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = item.approved.toBoolean(),
                interestingAuthor = item.topContentMaker.toBoolean(),
                isVip = item.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                customIconTopContent = R.drawable.ic_approved_author_gold_10
            )
        )
    }

}
