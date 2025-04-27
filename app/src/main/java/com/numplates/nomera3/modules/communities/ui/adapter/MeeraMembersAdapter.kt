package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.MeeraUserInfoModel
import com.numplates.nomera3.databinding.MeeraCommunityMemberItemBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.members.MeeraMembersActionClick


class MeeraMembersAdapter(
    private val userId: Long,
    private val listType: Int,
    val actionListener: ((action: MeeraMembersActionClick) -> Unit)? = null
) : BaseAsyncAdapter<String, UserInfoModelRecyclerData>() {

    private var userRole: Int = 0

    fun updateUserRole(role: Int) {
        userRole = role
    }

    fun hasItems() = currentList.isNotEmpty()

    fun setAdmin(position: Int) {
        if (position in 0..currentList.lastIndex) {
            (currentList[position] as UserInfoModelRecyclerData.UserInfoData).userInfoModel.isModerator = true.toInt()
            notifyItemChanged(position)
        }
    }

    fun setNotAdmin(position: Int) {
        if (position in 0..currentList.lastIndex) {
            (currentList[position] as UserInfoModelRecyclerData.UserInfoData).userInfoModel.isModerator = false.toInt()
            notifyItemChanged(position)
        }
    }

    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<UserInfoModelRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MemberViewHolder(
                binding = parent.toBinding()
            )

            else -> throw RuntimeException("Missing data adapter type")
        }
    }

    inner class MemberViewHolder(
        val binding: MeeraCommunityMemberItemBinding,
    ) : BaseVH<UserInfoModelRecyclerData, MeeraCommunityMemberItemBinding>(binding) {

        override fun bind(data: UserInfoModelRecyclerData) {
            data as UserInfoModelRecyclerData.UserInfoData
            binding.vUserItem.setMarginStartDivider(8.dp)
            binding.vUserItem.setCityTextColor(R.color.uiKitColorForegroundPrimary)
            currentList.filterIndexed { index, userInfoModelRecyclerData ->
                val localData = userInfoModelRecyclerData as UserInfoModelRecyclerData.UserInfoData
                if (localData.userInfoModel.user.userId == data.userInfoModel.user.userId){
                    when{
                        index == 0 -> binding.vUserItem.cellPosition = CellPosition.TOP
                        index == currentList.lastIndex -> binding.vUserItem.cellPosition = CellPosition.BOTTOM
                        else -> binding.vUserItem.cellPosition = CellPosition.MIDDLE
                    }
                }
                true
            }

            binding.vUserItem.setTitleValue(data.userInfoModel.user.name ?: "")
            binding.vUserItem.cellTitleVerified = data.userInfoModel.user.approved.toBoolean()

            setupUniqueName(data.userInfoModel)
            setupAvatar(data.userInfoModel)
            setupAgeAndCity(data.userInfoModel)
            setupActionIcon(data.userInfoModel)
            userRole(data.userInfoModel)
            userStatus(data.userInfoModel)
            binding.vUserItem.setThrottledClickListener {
                actionListener?.invoke(MeeraMembersActionClick.MemberClicked(data.userInfoModel))
            }
        }

        private fun setupAvatar(user: MeeraUserInfoModel) {
            binding.vUserItem.setLeftUserPicConfig(
                user.user.avatarSmall?.let { avatarUrl ->
                    UserpicUiModel(
                        userAvatarUrl = avatarUrl,
                        userAvatarErrorPlaceholder = R.drawable.ic_empty_avatar
                    )
                } ?: UserpicUiModel(
                    userAvatarRes = R.drawable.ic_empty_avatar
                )
            )
        }

        private fun setupUniqueName(user: MeeraUserInfoModel) {
            val uniqueName = user.user.uniqname
            if (!uniqueName.isNullOrEmpty()) {
                binding.vUserItem.setDescriptionValue("@$uniqueName")
                binding.vUserItem.cellDescription = true
            } else {
                binding.vUserItem.cellDescription = false
            }
        }

        private fun setupAgeAndCity(user: MeeraUserInfoModel) {
            user.user.city?.name?.let {
                binding.vUserItem.cellCityText = true
                binding.vUserItem.setCityValue(it)
            }
        }

        private fun setupActionIcon(user: MeeraUserInfoModel) {
            when {
                needToHideActionButton(user) -> {
                    binding.vUserItem.cellRightElement = CellRightElement.NONE
                }

                listType == CommunityMemberState.APPROVED -> {
                    binding.vUserItem.cellRightElement = CellRightElement.ICON
                    binding.vUserItem.setRightIcon(R.drawable.ic_outlined_kebab_m)
                    binding.vUserItem.cellRightIconClickListener = {
                        actionListener?.invoke(
                            MeeraMembersActionClick.MemberActionClicked(
                                user,
                                bindingAdapterPosition
                            )
                        )
                    }

                }

                listType == CommunityMemberState.NOT_APPROVED -> {
                    binding.vUserItem.cellRightElement = CellRightElement.ICON
                    binding.vUserItem.setRightIcon(R.drawable.ic_outlined_user_request_m)
                    binding.vUserItem.setRightColorIcon(R.color.uiKitColorForegroundPrimary)
                    binding.vUserItem.cellRightIconClickListener = {
                        actionListener?.invoke(
                            MeeraMembersActionClick.MembershipApproveClicked(
                                user,
                                bindingAdapterPosition
                            )
                        )
                    }
                }
            }
        }

        private fun needToHideActionButton(user: MeeraUserInfoModel): Boolean {
            return when {
                userId == user.user.userId -> true
                userRole == CommunityUserRole.REGULAR -> true
                user.isAuthor.toBoolean() -> true
                else -> false
            }
        }

        private fun userRole(user: MeeraUserInfoModel) {
            when {
                user.isAuthor.toBoolean() -> {
                    binding.vUserItem.setSubtitleValue(binding.root.context.getString(R.string.author))
                    binding.vUserItem.cellSubtitle = true
                }

                user.isModerator.toBoolean() -> {
                    binding.vUserItem.setSubtitleValue(binding.root.context.getString(R.string.moderator))
                    binding.vUserItem.cellSubtitle = true
                }

                else -> {
                    binding.vUserItem.setSubtitleValue(String.empty())
                    binding.vUserItem.cellSubtitle = false
                }
            }
        }

        private fun userStatus(user: MeeraUserInfoModel) {
            if (user.user.approved.toBoolean()) {
                binding.vUserItem.cellTitleVerified = true
            }
        }
    }
}

private const val TYPE_DATA = 1

sealed interface UserInfoModelRecyclerData : RecyclerData<String, UserInfoModelRecyclerData> {
    data class UserInfoData(
        val id: Long,
        val userInfoModel: MeeraUserInfoModel,
    ) : UserInfoModelRecyclerData {

        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: UserInfoModelRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }
}
