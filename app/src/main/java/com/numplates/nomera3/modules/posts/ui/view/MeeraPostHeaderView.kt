package com.numplates.nomera3.modules.posts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
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
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewPostHeaderBinding
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderEvent
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.posts.ui.model.isParentPost
import com.numplates.nomera3.presentation.view.utils.NTime

private const val DISABLED_ALPHA = 0.5f
private const val FULL_ALPHA = 1f

class MeeraPostHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var eventListener: ((PostHeaderEvent) -> Unit)? = null

    private val binding: MeeraViewPostHeaderBinding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_post_header, this, false)
        .apply(::addView)
        .let(MeeraViewPostHeaderBinding::bind)

    fun bind(uiModel: PostHeaderUiModel) {
        setupNavigation(uiModel)
        setupEvents(uiModel)
        when {
            uiModel.post.isEmptyPost() -> setupAsStubHeader(uiModel)
            else -> setupAsUserHeader(uiModel)
        }

        if(uiModel.post.isCommunityPost() && uiModel.isCommunityHeaderEnabled) {
            setupCommunity(uiModel)
        } else {
            binding.llCommunityHeader.gone()
        }
    }

    fun updateTimeAgo(post: PostUIEntity) {
        setupPostTime(post = post, needChangeVisibility = false)
    }

    private fun setupCommunity(uiModel: PostHeaderUiModel) {
        binding.apply {
            llCommunityHeader.visible()
            tvCommunityTitle.text = uiModel.post.groupName
            uiModel.post.groupId?.let { communityId ->
                tvCommunityTitle.setThrottledClickListener {
                    eventListener?.invoke(PostHeaderEvent.CommunityClicked(communityId))
                }
            }
        }
    }

    fun setEventListener(listener: ((PostHeaderEvent) -> Unit)?) {
        eventListener = listener
    }

    fun hideFollowButton() {
        if(binding.tvFollowUser.text == resources.getString(R.string.post_read)){
            binding.tvFollowUser.gone()
        }
    }

    fun hideOptionsButton() {
        binding.ivPostHeaderOptions.gone()
    }

    fun updateUserAvatar(post: PostUIEntity, bigAvatar: Boolean = false) {
        setupUserAvatar(post, bigAvatar)
    }

    fun clearResources() {
        eventListener = null
        with(binding){
            ivPostHeaderOptions.setOnClickListener(null)
            uiPostHeaderAvatar.setOnClickListener(null)
            ivPostHeaderCommunityAvatar.setOnClickListener(null)
            vPostHeaderProfileArea.setOnClickListener(null)
            tvPostHeaderName.setOnClickListener(null)
            tvPostHeaderGroupUserName.setOnClickListener(null)
            tvFollowUser.setOnClickListener(null)
            ivPostHeaderBack.setOnClickListener(null)
            ivPostHeaderClose.setOnClickListener(null)
            tvCommunityTitle.setOnClickListener(null)
            binding.uiPostHeaderAvatar.setConfig(UserpicUiModel())
        }
    }

    private fun setupNavigation(uiModel: PostHeaderUiModel) {
        val navigationMode = uiModel.navigationMode
        binding.ivPostHeaderBack.isVisible = navigationMode == PostHeaderNavigationMode.BACK
        binding.ivPostHeaderClose.isVisible = navigationMode == PostHeaderNavigationMode.CLOSE
    }

    private fun setupEvents(uiModel: PostHeaderUiModel) {
        binding.ivPostHeaderOptions.setThrottledClickListener { eventListener?.invoke(
            PostHeaderEvent.OptionsClicked
        ) }
        binding.uiPostHeaderAvatar.setThrottledClickListener { handleAvatarClick(uiModel.post) }
        uiModel.post.groupId?.let { communityId ->
            binding.ivPostHeaderCommunityAvatar.setThrottledClickListener {
                eventListener?.invoke(PostHeaderEvent.CommunityClicked(communityId))
            }
        }
        binding.vPostHeaderProfileArea.setThrottledClickListener { eventListener?.invoke(
            PostHeaderEvent.UserClicked
        ) }
        binding.tvPostHeaderName.setThrottledClickListener {
            eventListener?.invoke(PostHeaderEvent.UserClicked)
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
                    fromView = binding.uiPostHeaderAvatar,
                    hasNewMoments = post.user?.moments?.hasNewMoments
                )
            } else {
                PostHeaderEvent.UserClicked
            }
        )
    }

    private fun setupAsUserHeader(uiModel: PostHeaderUiModel) {
        setupUserLocation(uiModel.post)
        setupUserAvatar(uiModel.post, uiModel.bigAvatar)
        setupUserName(uiModel)
        setupUserMyRoadIcon(uiModel.post)
        setupUserFollowButton(uiModel.post)

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
        binding.ivPostHeaderLocationDivider.visible()
        binding.tvGeoLocation.visible()
    }

    private fun setupUserAvatar(post: PostUIEntity, bigAvatar: Boolean) {
        binding.ivPostHeaderCommunityAvatar.isVisible = false
        binding.uiPostHeaderAvatar.isInvisible = false
        binding.vPostHeaderProfileArea.isVisible = true
        binding.uiPostHeaderAvatar.tag = (post.postId + System.currentTimeMillis()).toString()
        post.user?.let { user ->
            val userMoments = user.moments
            val storiesState = when {
                userMoments?.hasMoments == true && userMoments.hasNewMoments -> UserpicStoriesStateEnum.NEW
                userMoments?.hasMoments == true -> UserpicStoriesStateEnum.VIEWED
                else -> UserpicStoriesStateEnum.NO_STORIES
            }

            val avatarSize = if (bigAvatar) UserpicSizeEnum.Size56 else UserpicSizeEnum.Size40

            binding.uiPostHeaderAvatar.setConfig(
                UserpicUiModel(
                    storiesState = storiesState,
                    userAvatarUrl = user.avatarSmall,
                    size = avatarSize
                )
            )
        }
    }

    private fun setupUserName(uiModel: PostHeaderUiModel) {
        val post = uiModel.post
        binding.tvPostHeaderName.visible()
        binding.tvPostHeaderGroupUserName.gone()
        initApprovedUser(post)
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
            isSubscribed -> R.color.uiKitColorForegroundSecondary
            else -> R.color.uiKitColorForegroundLink
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
                binding.ivPostHeaderActionDivider.visible()
            }
            else -> {
                binding.tvPostHeaderAction.isVisible = false
                binding.ivPostHeaderActionDivider.gone()
            }
        }
    }

    private fun setupPostTime(post: PostUIEntity, needChangeVisibility: Boolean = true) {
        if (needChangeVisibility) binding.tvPostHeaderDate.visible()
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
                interestingAuthor = post.user?.topContentMaker.toBoolean(),
                approvedIconSize = ApprovedIconSize.MEDIUM
            )
        )
    }

    private fun setupRepostIcon(uiModel: PostHeaderUiModel) {
        binding.ivPostHeaderRepost.setTint(R.color.ui_gray)
        binding.ivPostHeaderRepost.isVisible = uiModel.isParentPost()
    }

    private fun setupAsStubHeader(uiModel: PostHeaderUiModel) {
        binding.llCommunityHeader.gone()
        binding.uiPostHeaderAvatar.invisible()
        binding.ivPostHeaderCommunityAvatar.gone()
        binding.ivPostHeaderRepost.gone()
        binding.tvPostHeaderName.gone()
        binding.tvFollowUser.gone()
        binding.tvPostHeaderGroupUserName.gone()
        binding.tvPostHeaderAction.gone()
        binding.tvPostHeaderDate.gone()
        binding.ivPostHeaderMyRoad.gone()
        binding.ivPostHeaderLocationDivider.gone()
        binding.tvGeoLocation.gone()

        setupOptionsButton(uiModel)
    }
}
