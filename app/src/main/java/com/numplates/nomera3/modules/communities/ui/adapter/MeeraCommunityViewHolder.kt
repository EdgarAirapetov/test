package com.numplates.nomera3.modules.communities.ui.adapter

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserCommunityListItemBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity.Companion.USER_STATUS_NOT_YET_APPROVED
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.entity.CommunityMemberRole
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel

private const val GROUP_ITEM_MARGIN_START_DIVIDER = 8

class MeeraCommunityViewHolder(val binding: MeeraUserCommunityListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        community: CommunityListUIModel.Community?,
        lastPosition: Boolean = false,
        itemClickListener: ((CommunityListItemUIModel?) -> Unit)?,
        subscriptionClickListener: ((community: CommunityListItemUIModel, position: Int) -> Unit)? = null
    ) {
        if (lastPosition) binding.vGroupItem.cellPosition = CellPosition.BOTTOM
        binding.vGroupItem.setMarginStartDivider(GROUP_ITEM_MARGIN_START_DIVIDER.dp)
        community?.community?.let { communityUIModel ->
            binding.vGroupItem.setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = communityUIModel.coverImage.ifEmpty { null },
                    userAvatarRes = R.drawable.ic_empty_avatar,
                    userAvatarErrorPlaceholder = R.drawable.ic_empty_avatar,
                    scaleType = ImageView.ScaleType.CENTER_CROP
                )
            )
            showPrivateCommunityIcon(communityUIModel.isPrivate)
            showMemberCommunityIcon(
                communityUIModel.isMember,
                communityUIModel.isUserApproved,
                communityUIModel.userStatus
            )
            showMemberStatus(communityUIModel.memberRole)
            binding.vGroupItem.cellSubtitle = true
            binding.vGroupItem.setTitleValue(communityUIModel.name)
            showCommunityMemberCount(communityUIModel.memberCount)

            binding.clContent.setThrottledClickListener {
                itemClickListener?.invoke(communityUIModel)
            }
            binding.vGroupItem.cellRightIconClickListener = {
                subscriptionClickListener?.invoke(communityUIModel, bindingAdapterPosition)
            }
        }
    }

    private fun showCommunityMemberCount(memberCount: Int) {
        binding.vGroupItem.apply {
            context
                ?.pluralString(R.plurals.group_members_plural, memberCount)?.let {
                    setDescriptionValue(it)
                }
        }
    }

    private fun showMemberStatus(memberRole: CommunityMemberRole) {
        when (memberRole) {
            CommunityMemberRole.CREATOR -> {
                itemView.context?.getString(R.string.author)?.let {
                    binding.vGroupItem.setSubtitleValue(it)
                }
            }

            CommunityMemberRole.MODERATOR -> {
                itemView.context?.getString(R.string.moderator)?.let {
                    binding.vGroupItem.setSubtitleValue(it)
                }
            }

            else -> binding.vGroupItem.setSubtitleValue(String.empty())
        }
    }

    private fun showMemberCommunityIcon(isCommunityMember: Boolean, isUserApproved: Boolean, userStatus: Int) {
        val isUserNotApproved = userStatus == USER_STATUS_NOT_YET_APPROVED
        val isUserBannedInCommunity = userStatus == CommunityEntity.USER_STATUS_BANNED

        if (isCommunityMember && isUserApproved || isUserNotApproved || isUserBannedInCommunity) {
            binding.vGroupItem.cellRightElement = CellRightElement.NONE
        } else {
            binding.vGroupItem.cellRightElement = CellRightElement.ICON
        }
    }

    private fun showPrivateCommunityIcon(isPrivateCommunity: Boolean) {
        if (isPrivateCommunity) {
            binding.vGroupItem.cellDescriptionIcon = true
            binding.vGroupItem.setIconDescription(R.drawable.ic_outlined_lock_s)
        } else {
            binding.vGroupItem.cellDescriptionIcon = false
        }
    }
}
