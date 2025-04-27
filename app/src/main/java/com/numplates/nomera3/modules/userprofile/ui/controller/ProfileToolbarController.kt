package com.numplates.nomera3.modules.userprofile.ui.controller

import android.animation.ArgbEvaluator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.lerp
import com.meera.core.extensions.preloadAndSet
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserInfoBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.userprofile.ui.model.ProfileToolbarModelUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import timber.log.Timber
import kotlin.math.abs

class ProfileToolbarController(
    private val profileProvider: () -> UserProfileUIModel?,
    private val setLightStatusBar: () -> Unit,
    private val setColorStatusBar: () -> Unit
) {

    @ColorInt
    private var colorExpanded = 0

    @ColorInt
    private var colorCollapsed = 0

    private var isCollapsedToolbar: Boolean = false
    private var lastOffset = -1
    private var nicknameMarginBottom = 0
    private val evaluator: ArgbEvaluator by lazy { ArgbEvaluator() }

    fun onOffsetChanged(
        appBarLayout: AppBarLayout?,
        verticalOffset: Int,
        binding: FragmentUserInfoBinding?
    ) {
        binding?.root?.context ?: return
        if (binding.vAvatarView.avatarIsReady) {
            if (verticalOffset == 0 && isAnimatedAvatar()) startParallax(binding)
            else stopParallax(binding)
        }

        if (verticalOffset == 0) {
            binding.srlUserProfile.isEnabled = true
        } else {
            binding.srlUserProfile.isRefreshing = false
            binding.srlUserProfile.isEnabled = false
        }

        val maxScroll = appBarLayout?.totalScrollRange?.toFloat() ?: 0f
        val percentage = abs(verticalOffset) / maxScroll

        if (percentage >= COLLAPSING_THRESHOLD && !isCollapsedToolbar) setCollapsedToolbar(binding)
        else if (percentage < COLLAPSING_THRESHOLD && isCollapsedToolbar) setExpandedToolbar(binding)

        if (lastOffset == verticalOffset) return else lastOffset = verticalOffset
        updateNickname(binding, percentage)
    }

    fun getLastOffset() = lastOffset

    fun isCollapsedToolbar() = isCollapsedToolbar

    fun setupGender(gender: Gender?, binding: FragmentUserInfoBinding?) {
        if (gender != null) {
            binding?.ivGender?.visible()
            binding?.ivGender?.let { ivGender ->
                if (gender == Gender.MALE) {
                    setImageDrawable(R.drawable.ic_sex_profile_info_male, ivGender)
                } else {
                    setImageDrawable(R.drawable.ic_sex_profile_info_female, ivGender)
                }
            }
        } else binding?.ivGender?.gone()
    }

    fun setupUserAvatar(
        avatarUrl: String?,
        avatarAnimation: String?,
        gender: Gender?,
        binding: FragmentUserInfoBinding?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding?.ivAvatar?.let { ivAvatar ->
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(ivAvatar)
                    .preloadAndSet(avatarUrl, ivAvatar)
            } else {
                if (gender != null) {
                    val ivG = if (gender == Gender.MALE) R.drawable.fill_8 else R.drawable.profile_picture1_female
                    setImageDrawable(ivG, ivAvatar)
                } else {
                    setImageDrawable(R.drawable.fill_8, ivAvatar)
                }
            }
        }
        if (avatarAnimation.isNullOrEmpty()) {
            binding?.vAvatarView?.invisible()
            binding?.ivAvatar?.visible()
        } else {
            binding?.vAvatarView?.visible()
            binding?.vAvatarView?.avatarIsReadyCallback = {
                binding?.vAvatarView?.startParallaxEffect()
                binding?.ivAvatar?.invisible()
            }
            binding?.vAvatarView?.setStateAsync(avatarAnimation, lifecycleOwner.lifecycleScope)
        }
    }

    fun setupCollapsingNickname(
        params: ProfileToolbarModelUIModel,
        binding: FragmentUserInfoBinding?
    ) {
        val context = binding?.root?.context ?: return
        nicknameMarginBottom = getNicknameMarginBottom(params)
        binding.nickname.text = params.name
        colorExpanded = ContextCompat.getColor(context, R.color.ui_white)
        colorCollapsed = ContextCompat.getColor(context, R.color.black_85)
        if (isCollapsedToolbar) {
            setCollapsedState(binding, params.topContentParams)
        } else {
            setExpandedState(binding, params.topContentParams)
        }
    }

    private fun setExpandedState(binding: FragmentUserInfoBinding, topContent: TopAuthorApprovedUserModel) {
        Timber.d("setExpandedState white")
        binding.nickname.setTextColor(colorExpanded)
        binding.nickname.enableTopContentAuthorApprovedUser(
            topContent.copy(
                customIconTopContent = R.drawable.ic_approved_author_gold_18,
                customIcon = R.drawable.ic_verified_white_24dp,
                isVip = false
            )
        )
        binding.nickname.setMargins(COLLAPSING_TITLE_LEFT_MARGIN_EXPANDED.dp, 0, 0, nicknameMarginBottom)
    }

    private fun setCollapsedState(binding: FragmentUserInfoBinding, topContent: TopAuthorApprovedUserModel) {
        Timber.d("setCollapsedState black")
        binding.nickname.setTextColor(colorCollapsed)
        binding.nickname.enableTopContentAuthorApprovedUser(topContent)
        binding.nickname.setMargins(getLeftNicknameMargin(binding), 0, 0, COLLAPSING_TITLE_BOTTOM_MARGIN_COLLAPSED.dp)

    }

    private fun getNicknameMarginBottom(params: ProfileToolbarModelUIModel) = when {
        params.profileDeleted -> PROFILE_DELETED_NICKNAME_MARGIN.dp
        params.blacklistedMe -> PROFILE_BLACKLISTED_BY_ME.dp
        params.isMe -> {
            if (params.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR) COLLAPSING_TITLE_DEFAULT_MARGIN.dp
            else COLLAPSING_TITLE_DEFAULT_MARGIN.dp
        }
        params.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR -> COLLAPSING_TITLE_DEFAULT_MARGIN.dp
        else -> COLLAPSING_TITLE_DEFAULT_MARGIN.dp
    }

    private fun isAnimatedAvatar() = !profileProvider.invoke()?.avatarDetails?.avatarAnimation.isNullOrEmpty()

    private fun setImageDrawable(@DrawableRes drawable: Int, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(drawable)
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .into(imageView)
    }

    private fun getLeftNicknameMargin(binding: FragmentUserInfoBinding?): Int {
        return if (binding?.ivBack?.isVisible == true && binding.ivPhoto.isVisible) {
            COLLAPSING_TITLE_LEFT_MARGIN_COLLAPSED_BIG.dp
        } else {
            COLLAPSING_TITLE_LEFT_MARGIN_COLLAPSED.dp
        }
    }

    private fun startParallax(binding: FragmentUserInfoBinding) {
        binding.vAvatarView.startParallaxEffect()
        binding.vAvatarView.visible()
        binding.ivAvatar.invisible()
    }

    private fun stopParallax(binding: FragmentUserInfoBinding) {
        binding.vAvatarView.stopParallaxEffect()
        binding.ivAvatar.visible()
        binding.vAvatarView.invisible()
    }

    private fun updateColors(colorInt: Int?, binding: FragmentUserInfoBinding) {
        colorInt?.let {
            binding.ivPhoto.setColorFilter(colorInt)
            binding.ivDots.setColorFilter(colorInt)
            binding.ivBack.setColorFilter(colorInt)
            binding.ibCollapse.setColorFilter(colorInt)
        } ?: run {
            binding.ivPhoto.colorFilter = null
            binding.ivDots.colorFilter = null
            binding.ivBack.colorFilter = null
            binding.ibCollapse.colorFilter = null
        }
    }

    private fun setCollapsedToolbar(binding: FragmentUserInfoBinding) {
        setLightStatusBar.invoke()
        updateColors(ContextCompat.getColor(binding.root.context, R.color.colorBlack), binding)
        isCollapsedToolbar = true
        binding.nickname.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = profileProvider.invoke()?.accountDetails?.isAccountApproved ?: false,
                interestingAuthor = profileProvider.invoke()?.accountDetails?.isTopContentMaker ?: false,
                customIconTopContent = R.drawable.ic_approved_author_gold_18,
                approvedIconSize = ApprovedIconSize.LARGE,
                isVip = profileProvider.invoke()?.accountDetails?.accountType !=
                    AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value
            )
      )
        binding.vgUserActions.animate()?.translationY(dpToPx(100).toFloat())
            ?.setInterpolator(DecelerateInterpolator())?.duration = 200
    }

    private fun setExpandedToolbar(binding: FragmentUserInfoBinding) {
        setColorStatusBar.invoke()
        updateColors(null, binding)
        isCollapsedToolbar = false
        binding.nickname.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = profileProvider.invoke()?.accountDetails?.isAccountApproved ?: false,
                interestingAuthor = profileProvider.invoke()?.accountDetails?.isTopContentMaker ?: false,
                customIconTopContent = R.drawable.ic_approved_author_gold_18,
                customIcon = R.drawable.ic_verified_white_24dp
            )
        )
        binding.vgUserActions.animate()?.translationY(0f)
            ?.setInterpolator(DecelerateInterpolator())?.duration = 200
    }

    private fun updateNickname(binding: FragmentUserInfoBinding, percentage: Float) = binding.nickname.apply {
        scaleX = lerp(1f, COLLAPSING_TITLE_SCALE_FACTOR, percentage)
        scaleY = lerp(1f, COLLAPSING_TITLE_SCALE_FACTOR, percentage)
        pivotX = 1f
        pivotY = height.toFloat() / 2
        setMargins(
            lerp(
                COLLAPSING_TITLE_LEFT_MARGIN_EXPANDED.dp.toFloat(),
                getLeftNicknameMargin(binding).toFloat(),
                percentage
            ).toInt(),
            0,
            0,
            lerp(
                nicknameMarginBottom.toFloat(),
                COLLAPSING_TITLE_BOTTOM_MARGIN_COLLAPSED.dp.toFloat(),
                percentage
            ).toInt()
        )
        setTextColor(
            evaluator.evaluate(percentage, colorExpanded, colorCollapsed) as Int
        )
    }

    companion object {
        private const val COLLAPSING_TITLE_SCALE_FACTOR = 0.625f
        private const val COLLAPSING_TITLE_DEFAULT_MARGIN = 72
        private const val COLLAPSING_TITLE_LEFT_MARGIN_EXPANDED = 16
        private const val COLLAPSING_TITLE_LEFT_MARGIN_COLLAPSED = 60
        private const val COLLAPSING_TITLE_LEFT_MARGIN_COLLAPSED_BIG = 104
        private const val COLLAPSING_TITLE_BOTTOM_MARGIN_COLLAPSED = 6
        private const val PROFILE_DELETED_NICKNAME_MARGIN = 14
        private const val PROFILE_BLACKLISTED_BY_ME = 46
        private const val COLLAPSING_THRESHOLD = 0.7f

    }
}
