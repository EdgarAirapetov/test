package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.dp
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.BannerType
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityBannerFloor
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class BannerFloorViewHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityBannerFloor>(parent, R.layout.banner_profile_send_gift) {

    private val BANNER_TYPE_BLOCKED_ME_MARGIN = 20
    private val clBannerRoot = itemView.findViewById<ConstraintLayout>(R.id.cl_banner_root)
    private val clBannerContent = itemView.findViewById<ConstraintLayout>(R.id.cl_banner_content)
    private val ivBannerImg = itemView.findViewById<ImageView>(R.id.iv_banner_img)

    private val tvHeader = itemView.findViewById<TextView>(R.id.textView31)
    private val tvDescription = itemView.findViewById<TextView>(R.id.textView33)

    private val ivBannerImgTopLeft = itemView.findViewById<ImageView>(R.id.imageView40)

    override fun bind(data: UserEntityBannerFloor) {
        clBannerRoot.visible()
        handleBannerType(data.bannerType, data.userType)
    }

    private fun handleBannerType(bannerType: BannerType, userType: AccountTypeEnum) {
        when (bannerType) {
            BannerType.BANNER_TYPE_BLOCKED_ME -> {
                itemView.setMargins(top = BANNER_TYPE_BLOCKED_ME_MARGIN.dp)
                setBlockedText()
                showBannerBlockedMe(userType)
                itemView.click {
                    it.clickAnimate()
                    it.context.vibrate()
                }
            }

            BannerType.BANNER_TYPE_GIFT -> {
                itemView.setMargins(top = 8.dp)
                setGiftText()
                showBannerGift(userType)
                itemView.click { clickedView ->
                    clickedView.clickAnimate()
                    profileUIActionHandler(UserProfileUIAction.OnGiftsListClick)
                }
            }
        }
    }

    private fun setGiftText() {
        tvHeader.setText(R.string.gifts_banner_title)
        tvDescription.setText(R.string.gifts_banner_description)
    }

    private fun setBlockedText() {
        tvHeader.setText(R.string.blocked_user)
        tvDescription.setText(R.string.blocked_me_descr)
    }

    private fun makeTextBlack() {
        tvHeader.setTextColor(getColor(itemView.context, R.color.black_banner_text_header))
        tvDescription.setTextColor(getColor(itemView.context, R.color.black_banner_text_descr))
    }

    private fun makeTextWhite() {
        tvHeader.setTextColor(getColor(itemView.context, R.color.ui_white))
        tvDescription.setTextColor(getColor(itemView.context, R.color.ui_white_80))
    }

    private fun showBannerGift(userType: AccountTypeEnum) {
        val drawable: Drawable? = when (userType) {
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN -> {
                makeTextWhite()
                Glide.with(itemView)
                        .load(R.drawable.ic_gift_banner)
                        .override(87.dp, 67.dp)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivBannerImg)
                ivBannerImgTopLeft.loadGlide(R.drawable.send_gift_banner_user)
                ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.send_gift_banner_background_regular
                )
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                makeTextBlack()
                Glide.with(itemView)
                    .load(R.drawable.ic_gift_banner_vip)
                    .override(87.dp, 67.dp)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivBannerImg)
                ivBannerImgTopLeft.loadGlide(R.drawable.send_gift_banner_user_vip)
                ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.send_gift_banner_background_gold
                )
            }

        }
        clBannerContent?.background = drawable
    }

    private fun showBannerBlockedMe(userType: AccountTypeEnum) {
        val drawable: Drawable? = when (userType) {
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN -> {
                makeTextWhite()
                Glide.with(itemView)
                        .load(R.drawable.ic_privacy_lock_regular)
                        .override(78.dp, 102.dp)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivBannerImg)
                ivBannerImgTopLeft.loadGlide(R.drawable.ic_blocked_user_new)
                ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.send_gift_banner_background_regular
                )
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                makeTextBlack()
                Glide.with(itemView)
                        .load(R.drawable.ic_privacy_lock)
                        .override(78.dp, 102.dp)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivBannerImg)
                ivBannerImgTopLeft.loadGlide(R.drawable.ic_blocked_new_user_gold)
                ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.send_gift_banner_background_gold
                )
            }
        }
        clBannerContent?.background = drawable
    }

}
