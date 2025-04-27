package com.numplates.nomera3.modules.posts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.textColor
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableApprovedIcon
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.ViewPostHeaderBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderEvent
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.posts.ui.model.isParentOfVipPost
import com.numplates.nomera3.modules.posts.ui.model.isParentPost
import com.numplates.nomera3.presentation.view.utils.NTime

private const val DISABLED_ALPHA = 0.5f
private const val FULL_ALPHA = 1f
class PostHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var eventListener: ((PostHeaderEvent) -> Unit)? = null

    private val binding: ViewPostHeaderBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_post_header, this, false)
        .apply(::addView)
        .let(ViewPostHeaderBinding::bind)

    fun bind(uiModel: PostHeaderUiModel) {
        setupNavigation(uiModel)
        setupEvents(uiModel)
        when {
            uiModel.post.isEmptyPost() -> setupAsStubHeader(uiModel)
            uiModel.post.isCommunityPost() && uiModel.isCommunityHeaderEnabled -> setupAsCommunityHeader(uiModel)
            else -> setupAsUserHeader(uiModel)
        }
    }

    fun setEventListener(listener: (PostHeaderEvent) -> Unit) {
        eventListener = listener
    }

    fun hideFollowButton() {
        if(binding.tvFollowUser.text == resources.getString(R.string.post_read)){
            binding.tvFollowUser.gone()
        }
    }

    fun updateUserAvatar(post: PostUIEntity) {
        setupUserAvatar(post)
    }

    private fun setupNavigation(uiModel: PostHeaderUiModel) {
        val navigationMode = uiModel.navigationMode
        val navigationColorResId = when {
            uiModel.post.isVipPost() -> R.color.ui_yellow_tint
            uiModel.isLightNavigation -> R.color.ui_gray_80
            else -> R.color.colorDarkGrey
        }
        binding.ivPostHeaderClose.setTint(navigationColorResId)
        binding.ivPostHeaderBack.setTint(navigationColorResId)
        binding.ivPostHeaderOptions.setTint(navigationColorResId)
        binding.ivPostHeaderBack.isVisible = navigationMode == PostHeaderNavigationMode.BACK
        binding.ivPostHeaderClose.isVisible = navigationMode == PostHeaderNavigationMode.CLOSE
    }

    private fun setupEvents(uiModel: PostHeaderUiModel) {
        binding.ivPostHeaderOptions.setThrottledClickListener { eventListener?.invoke(PostHeaderEvent.OptionsClicked) }
        binding.vvPostHeaderAvatar.setThrottledClickListener { handleAvatarClick(uiModel.post) }
        uiModel.post.groupId?.let { communityId ->
            binding.ivPostHeaderCommunityAvatar.setThrottledClickListener {
                eventListener?.invoke(PostHeaderEvent.CommunityClicked(communityId))
            }
        }
        binding.vPostHeaderProfileArea.setThrottledClickListener { eventListener?.invoke(PostHeaderEvent.UserClicked) }
        binding.tvPostHeaderName.setThrottledClickListener {
            if (uiModel.post.isCommunityPost() && uiModel.isCommunityHeaderEnabled) {
                uiModel.post.groupId?.let { communityId ->
                    eventListener?.invoke(PostHeaderEvent.CommunityClicked(communityId))
                }
            } else {
                eventListener?.invoke(PostHeaderEvent.UserClicked)
            }
        }
        binding.tvPostHeaderGroupUserName.setThrottledClickListener {
            eventListener?.invoke(PostHeaderEvent.UserClicked)
        }
        binding.tvFollowUser.setThrottledClickListener { eventListener?.invoke(PostHeaderEvent.FollowClicked) }
        binding.ivPostHeaderBack.setThrottledClickListener { eventListener?.invoke(PostHeaderEvent.BackClicked) }
        binding.ivPostHeaderClose.setThrottledClickListener { eventListener?.invoke(PostHeaderEvent.CloseClicked) }
    }

    private fun handleAvatarClick(post: PostUIEntity) {
        val userId = post.getUserId() ?: return
        eventListener?.invoke(
            if (post.isUserHasMoments()) {
                PostHeaderEvent.UserMomentsClicked(
                    userId = userId,
                    fromView = binding.vvPostHeaderAvatar,
                    hasNewMoments = post.user?.moments?.hasNewMoments
                    )
            } else {
                PostHeaderEvent.UserClicked
            }
        )
    }

    private fun setupAsCommunityHeader(uiModel: PostHeaderUiModel) {
        setupCommunityLocation()
        setupCommunityAvatar(uiModel.post)
        setupCommunityName(uiModel)
        setupCommunityMyRoadIcon()
        setupCommunityFollowButton()

        setupBackground(uiModel)
        setupPostAction(uiModel.post)
        setupPostTime(uiModel.post)
        setupOptionsButton(uiModel)
        setupRepostIcon(uiModel)
    }

    private fun setupCommunityLocation() {
        binding.tvGeoLocation.gone()
    }

    private fun setupCommunityAvatar(post: PostUIEntity) {
        binding.ivPostHeaderCommunityAvatar.isVisible = true
        binding.vvPostHeaderAvatar.isInvisible = true
        binding.vPostHeaderProfileArea.isVisible = false
        Glide.with(context)
            .load(post.groupAvatar)
            .fallback(R.drawable.community_cover_image_placeholder_new)
            .error(R.drawable.community_cover_image_placeholder_new)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivPostHeaderCommunityAvatar)
    }

    private fun setupCommunityName(uiModel: PostHeaderUiModel) {
        binding.tvPostHeaderName.visible()
        binding.tvPostHeaderName.enableApprovedIcon(enabled = false)
        binding.tvPostHeaderName.text = uiModel.post.groupName
        val nameColorResId = when {
            uiModel.post.isVipPost() -> R.color.ui_yellow
            uiModel.isParentPost() && uiModel.isParentOfVipPost() -> R.color.ui_white
            else -> R.color.ui_black
        }
        binding.tvPostHeaderName.setTextColor(ContextCompat.getColor(context, nameColorResId))
        binding.tvPostHeaderGroupUserName.text = uiModel.post.user?.name
        binding.tvPostHeaderGroupUserName.setTextColor(ContextCompat.getColor(context, nameColorResId))
        binding.tvPostHeaderGroupUserName.visible()
    }

    private fun setupCommunityMyRoadIcon() {
        binding.ivPostHeaderMyRoad.gone()
    }

    private fun setupCommunityFollowButton() {
        binding.tvFollowUser.gone()
    }

    private fun setupAsUserHeader(uiModel: PostHeaderUiModel) {
        setupUserLocation(uiModel.post)
        setupUserAvatar(uiModel.post)
        setupUserName(uiModel)
        setupUserMyRoadIcon(uiModel.post)
        setupUserFollowButton(uiModel.post)

        setupBackground(uiModel)
        setupPostAction(uiModel.post)
        setupPostTime(uiModel.post)
        setupOptionsButton(uiModel)
        setupRepostIcon(uiModel)
    }

    private fun setupUserLocation(post: PostUIEntity) {
        val city = if (post.city?.name.isNullOrEmpty()) {
            post.user?.city?.name
        } else {
            post.city?.name
        }
        val location = city ?: resources.getString(R.string.road_unknown_geo)
        binding.tvGeoLocation.text = location
        binding.tvGeoLocation.visible()
    }

    private fun setupUserAvatar(post: PostUIEntity) {
        binding.ivPostHeaderCommunityAvatar.isVisible = false
        binding.vvPostHeaderAvatar.isInvisible = false
        binding.vPostHeaderProfileArea.isVisible = true
        binding.vvPostHeaderAvatar.tag = (post.postId + System.currentTimeMillis()).toString()
        post.user?.let { user ->
            binding.vvPostHeaderAvatar.setUp(
                context = context,
                avatarLink = user.avatarSmall,
                accountType = user.accountType.value,
                frameColor = user.accountColor ?: INetworkValues.COLOR_RED,
                hasMoments = user.moments?.hasMoments ?: false,
                hasNewMoments = user.moments?.hasNewMoments ?: false
            )
        }
    }

    private fun setupUserName(uiModel: PostHeaderUiModel) {
        val post = uiModel.post
        binding.tvPostHeaderName.visible()
        binding.tvPostHeaderGroupUserName.gone()
        initApprovedUser(post)
        val nameColorResId = when {
            uiModel.post.isVipPost() && uiModel.isParentPost().not() -> R.color.ui_yellow
            uiModel.isParentOfVipPost() -> R.color.ui_white
            else -> R.color.ui_black
        }
        binding.tvPostHeaderName.setTextColor(ContextCompat.getColor(context, nameColorResId))
        binding.tvPostHeaderName.text = post.user?.name
    }

    private fun setupUserMyRoadIcon(post: PostUIEntity) {
        binding.ivPostHeaderMyRoad.isVisible = post.privacy == PostPrivacy.PRIVATE
    }

    private fun setupUserFollowButton(post: PostUIEntity) {
        val isSubscribed = post.user?.subscriptionOn?.isTrue() ?: return
        if (!post.needToShowFollowButton) {
            binding.tvFollowUser.gone()
            return
        } else {
            binding.tvFollowUser.visible()
        }
        val followColorResId = when {
            isSubscribed -> R.color.ui_gray
            post.isVipPost() -> R.color.ui_yellow_tint
            else -> R.color.ui_purple
        }
        binding.tvFollowUser.textColor(followColorResId)
        if (isSubscribed) {
            binding.tvFollowUser.text = resources.getString(R.string.post_read)
        } else {
            binding.tvFollowUser.text = resources.getString(R.string.post_start_follow)
        }
    }

    private fun setupPostAction(post: PostUIEntity) {
        when (post.type) {
            PostTypeEnum.AVATAR_VISIBLE, PostTypeEnum.AVATAR_HIDDEN -> {
                binding.tvPostHeaderAction.text = resources.getString(R.string.profile_avatar_updated)
                binding.tvPostHeaderAction.isVisible = true
            }
            else -> {
                binding.tvPostHeaderAction.isVisible = false
            }
        }
    }

    private fun setupPostTime(post: PostUIEntity) {
        binding.tvPostHeaderDate.visible()
        val dateText = post.date?.let { return@let NTime.timeAgo(it, true) }
            ?: String.empty()
        binding.tvPostHeaderDate.text = dateText
    }

    private fun setupOptionsButton(uiModel: PostHeaderUiModel) {
        binding.ivPostHeaderOptions.isVisible = uiModel.isOptionsAvailable && uiModel.post.isEmptyPost().not()
        with(binding.ivPostHeaderOptions) {
            alpha = if (uiModel.editInProgress) DISABLED_ALPHA else FULL_ALPHA
            isClickable = !uiModel.editInProgress
            isFocusable = !uiModel.editInProgress
        }
    }

    private fun initApprovedUser(post: PostUIEntity) {
        binding.tvPostHeaderName.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = post.user?.approved.toBoolean(),
                customIconTopContent = R.drawable.ic_approved_author_gold_10,
                isVip = post.user?.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                interestingAuthor = post.user?.topContentMaker.toBoolean(),
                approvedIconSize = ApprovedIconSize.SMALL
            )
        )
    }

    private fun setupRepostIcon(uiModel: PostHeaderUiModel) {
        binding.ivPostHeaderRepost.setTint(R.color.ui_gray)
        binding.ivPostHeaderRepost.isVisible = uiModel.isParentPost()
    }

    private fun setupBackground(uiModel: PostHeaderUiModel) {
        val backgroundColorResId = if (
            uiModel.isParentPost().not() && uiModel.post.isVipPost()
            || uiModel.isParentPost() && uiModel.isParentOfVipPost()
        ) {
            R.color.colorVipPostGoldBlack
        } else {
            R.color.ui_white
        }
        binding.vgPostHeaderRoot.setBackgroundColor(ContextCompat.getColor(binding.root.context, backgroundColorResId))
    }

    private fun setupAsStubHeader(uiModel: PostHeaderUiModel) {
        binding.vvPostHeaderAvatar.invisible()
        binding.ivPostHeaderCommunityAvatar.gone()
        binding.ivPostHeaderRepost.gone()
        binding.tvPostHeaderName.gone()
        binding.tvFollowUser.gone()
        binding.tvPostHeaderGroupUserName.gone()
        binding.tvPostHeaderAction.gone()
        binding.tvPostHeaderDate.gone()
        binding.ivPostHeaderMyRoad.gone()
        binding.tvGeoLocation.gone()

        setupOptionsButton(uiModel)
    }
}
