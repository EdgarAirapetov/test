package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityRoadFloor
import com.meera.core.extensions.clickAnimate
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class RoadFloorViewHolder(
    val parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityRoadFloor>(parent, R.layout.item_road_floor) {

    private val profileTitlePosts = itemView.findViewById<TextView>(R.id.profile_title_posts)
    private val tvPostAmount = itemView.findViewById<TextView>(R.id.tvPostAmount)

    private val llLayoutNewPost = itemView.findViewById<ConstraintLayout>(R.id.add_group_post)
    private val tvFieldAddPost = itemView.findViewById<TextView>(R.id.tv_field_add_post)

    private val layoutEmptyRoad = itemView.findViewById<LinearLayout>(R.id.layout_empty_road)
    private val ivRoadEmpty = itemView.findViewById<ImageView>(R.id.iv_road_empty)
    private val tvRoadPlaceholder = itemView.findViewById<TextView>(R.id.tv_road_placeholder)
    private val tvWritePostPlaceholder =
        itemView.findViewById<TextView>(R.id.tv_write_post_placeholder)

    private val separator = itemView.findViewById<View>(R.id.separator_two)

    override fun bind(data: UserEntityRoadFloor) {
        separator?.setMargins(top = 16.dp, bottom = 0.dp)
        tvFieldAddPost?.setMargins(start = 16.dp)
        when (data.userTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM -> {
                setupCommonTheme()
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                setupVipTheme()
            }
            else -> {}
        }

        tvPostAmount?.text = data.postCount.toString()

        if (data.isMe) {
            setupHolderForOwner(data)
        } else {
            setupHolderForUserAction(data)
        }

        initClickListeners()
    }

    private fun initClickListeners() {
        tvFieldAddPost?.click {
            it.clickAnimate()
            profileUIActionHandler.invoke(
                UserProfileUIAction.OnNewPostClicked(false)
            )
        }
        tvWritePostPlaceholder?.click {
            it.clickAnimate()
            profileUIActionHandler.invoke(
                UserProfileUIAction.OnNewPostClicked(false)
            )
        }
    }

    private fun setupHolderForUserAction(data: UserEntityRoadFloor) {
        llLayoutNewPost?.gone()
        tvWritePostPlaceholder?.gone()
        if (data.postCount == 0) {
            itemView.gone()
        } else {
            itemView.visible()
            layoutEmptyRoad?.gone()
        }
    }

    private fun setupHolderForOwner(data: UserEntityRoadFloor) {
        itemView.visible()
        llLayoutNewPost?.visible()
        tvWritePostPlaceholder?.visible()
        tvRoadPlaceholder?.text = itemView.context.getText(R.string.place_holder_user_post_list)
        if (data.postCount == 0) {
            layoutEmptyRoad?.visible()
            llLayoutNewPost?.gone()
        } else {
            layoutEmptyRoad?.gone()
            llLayoutNewPost?.visible()
        }
    }

    private fun setupVipTheme() {
        val context = itemView.context
        profileTitlePosts?.setTextColor(ContextCompat.getColor(context, R.color.white_1000))
        tvPostAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_light_gray))
        ivRoadEmpty?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.road_empty_vip))
        tvRoadPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_gray))
        tvWritePostPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        llLayoutNewPost?.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.colorUpgradeBlack
            )
        )
        tvFieldAddPost?.setTextColor(ContextCompat.getColor(context, R.color.ui_white_50))
        tvFieldAddPost?.background = ContextCompat.getDrawable(context, R.drawable.gray_field_vip)
    }

    private fun setupCommonTheme() {
        val context = itemView.context
        profileTitlePosts?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        tvPostAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        ivRoadEmpty?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.road_empty))
        tvRoadPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        tvWritePostPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
        llLayoutNewPost?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
        tvFieldAddPost?.setTextColor(ContextCompat.getColor(context, R.color.colorGrayA7A5))
        tvFieldAddPost?.background = ContextCompat.getDrawable(context, R.drawable.gray_field)
    }
}
