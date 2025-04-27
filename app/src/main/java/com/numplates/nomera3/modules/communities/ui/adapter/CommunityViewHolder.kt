package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity.Companion.USER_STATUS_NOT_YET_APPROVED
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.entity.CommunityMemberRole
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel

class CommunityViewHolder(viewGroup: ViewGroup) :
    BaseViewHolder(viewGroup, R.layout.user_community_list_item) {

    // views
    private var communityImage: ImageView? = itemView.findViewById(R.id.ivPicture)
    private var communityName: TextView? = itemView.findViewById(R.id.tvName)
    private var communityUserCount: TextView? = itemView.findViewById(R.id.tvUserCount)
    private var userCommunityStatus: TextView? = itemView.findViewById(R.id.tv_user_status)
    private var joinCommunityButton: ImageView? = itemView.findViewById(R.id.iv_join_icon)
    private var communityPrivacyTypeIcon: ImageView? =
        itemView.findViewById(R.id.iv_groups_holder_privacy)
    private var rootContainer: ConstraintLayout? = itemView.findViewById(R.id.clContent)

    // other
    private var innerCommunityUIModel: CommunityListItemUIModel? = null

    fun bind(
        community: CommunityListUIModel.Community?,
        itemClickListener: ((CommunityListItemUIModel?) -> Unit)?,
        subscriptionClickListener: ((community: CommunityListItemUIModel, position: Int) -> Unit)? = null
    ) {
        val communityUIModel = community?.community
        if (communityUIModel != null) {
            innerCommunityUIModel = communityUIModel
            showCommunityCoverImage(communityUIModel.coverImage)
            showPrivateCommunityIcon(communityUIModel.isPrivate)
            showMemberCommunityIcon(
                communityUIModel.isMember,
                communityUIModel.isUserApproved,
                communityUIModel.userStatus
            )
            showMemberStatus(communityUIModel.memberRole)
            showCommunityName(communityUIModel.name)
            showCommunityMemberCount(communityUIModel.memberCount)

            rootContainer?.click {
                itemClickListener?.invoke(communityUIModel)
            }
            joinCommunityButton?.click {
                subscriptionClickListener?.invoke(communityUIModel, bindingAdapterPosition)
            }
        }
    }

    private fun showCommunityMemberCount(memberCount: Int) {
        communityUserCount?.text = communityUserCount
            ?.context
            ?.pluralString(R.plurals.group_members_plural, memberCount)
    }

    private fun showCommunityName(name: String) {
        communityName?.text = name
    }

    private fun showMemberStatus(memberRole: CommunityMemberRole) {
        when (memberRole) {
            CommunityMemberRole.CREATOR -> {
                userCommunityStatus?.text = itemView.context?.getString(R.string.author)
                userCommunityStatus?.visible()
            }
            CommunityMemberRole.MODERATOR -> {
                userCommunityStatus?.text = itemView.context?.getString(R.string.moderator)
                userCommunityStatus?.visible()
            }
            CommunityMemberRole.MEMBER -> {
                userCommunityStatus?.gone()
            }
            else -> {
                // do nothing
            }
        }
    }

    private fun showMemberCommunityIcon(isCommunityMember: Boolean, isUserApproved: Boolean, userStatus: Int) {
        val isUserNotApproved = userStatus == USER_STATUS_NOT_YET_APPROVED
        val isUserBannedInCommunity = userStatus == CommunityEntity.USER_STATUS_BANNED

        if (isCommunityMember && isUserApproved || isUserNotApproved || isUserBannedInCommunity) {
            joinCommunityButton?.gone()
        } else {
            joinCommunityButton?.visible()
        }
    }

    private fun showPrivateCommunityIcon(isPrivateCommunity: Boolean) {
        if (isPrivateCommunity) {
            communityPrivacyTypeIcon?.visible()
        } else {
            communityPrivacyTypeIcon?.gone()
        }
    }

    private fun showCommunityCoverImage(coverImageURL: String) {
        communityImage?.also { communityImageView: ImageView ->
            Glide.with(communityImageView.context)
                .load(coverImageURL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.community_cover_image_placeholder_new)
                .into(communityImageView)
        }
    }
}
