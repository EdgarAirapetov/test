package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGiftsFloor
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.GiftProfileListAdapterNew
import com.numplates.nomera3.presentation.view.ui.OrientationAwareRecyclerView
import com.meera.core.extensions.applyColorFilter
import com.meera.core.extensions.setBackgroundTint
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction


class GiftsFloorViewHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityGiftsFloor>(parent, R.layout.item_gift_floor) {

    private val profileTitleGifts = itemView.findViewById<TextView>(R.id.profile_title_gifts)
    private val tvGiftsAmount = itemView.findViewById<TextView>(R.id.tvGiftsAmount)
    private val tvGiftsNew = itemView.findViewById<TextView>(R.id.tvGiftsNew)
    private val tvAddGift = itemView.findViewById<TextView>(R.id.tvAddGift)
    private val tvSendGift = itemView.findViewById<TextView>(R.id.tv_send_gift)
    private val ivSendGift = itemView.findViewById<ImageView>(R.id.iv_send_gift)

    private val rvGifts = itemView.findViewById<OrientationAwareRecyclerView>(R.id.rvGifts)
    private val containerSendGiftBtn = itemView.findViewById<LinearLayout>(R.id.container_send_gift_btn)

    private val layoutEmptyGifts = itemView.findViewById<LinearLayout>(R.id.layout_empty_gifts)
    private val ivGiftsPlaceholder = itemView.findViewById<ImageView>(R.id.iv_gifts_placeholder)
    private val tvGiftsPlaceholder = itemView.findViewById<TextView>(R.id.tv_gifts_placeholder)
    private val tvBtnSendGift = itemView.findViewById<TextView>(R.id.tv_btn_send_gift)
    private val tvGiftsPlaceholderTwo = itemView.findViewById<TextView>(R.id.tv_gifts_placeholder_two)

    private val separator = itemView.findViewById<View>(R.id.v_separator_gift)

    override fun bind(data: UserEntityGiftsFloor) {
        when (data.accountTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR -> setupCommonTheme()

            AccountTypeEnum.ACCOUNT_TYPE_VIP -> setupVipTheme()

            else -> Unit
        }

        setupGiftsList(data)
        initClickListeners()
        initSendGiftText(data)
        if (data.isSeparable) separator?.visible()
        else separator?.gone()
    }

    private fun initClickListeners() {
        tvAddGift?.click {
            tvGiftsNew?.text = null
            profileUIActionHandler(UserProfileUIAction.OnGiftClick)
        }

        containerSendGiftBtn?.click {
            profileUIActionHandler(UserProfileUIAction.OnGiftsListClick)
        }

        tvBtnSendGift?.click {
            profileUIActionHandler(UserProfileUIAction.OnGiftsListClick)
        }
    }

    private fun initSendGiftText(data: UserEntityGiftsFloor) {
        if (data.isMe) {
            tvSendGift?.text = itemView.context.getString(R.string.gifts_send_gift_to_me_btn_label)
        } else {
            tvSendGift?.text = itemView.context.getString(R.string.gifts_send_gift_btn_label)
        }
    }

    private fun setupGiftsList(data: UserEntityGiftsFloor) {
        if (data.listGiftEntity.isEmpty()) showEmptyPlaceholder()
        else {
            hideEmptyPlaceholder()
            initRecycler(data)
        }
    }

    private fun initRecycler(data: UserEntityGiftsFloor) {
        tvGiftsAmount?.text = data.listGiftEntity.size.toString()
        if (data.isMe && data.giftsNewCount > 0) tvGiftsNew?.text = "(+${data.giftsNewCount})"
        else tvGiftsNew?.text = null
        rvGifts?.visible()

        val layoutManager = LinearLayoutManager(
                itemView.context,
                RecyclerView.HORIZONTAL,
                false
        )

        rvGifts?.setHasFixedSize(true)
        rvGifts?.isNestedScrollingEnabled = false
        rvGifts?.layoutManager = layoutManager
        val adapter = GiftProfileListAdapterNew(data.isMe)
        rvGifts?.adapter = adapter
        adapter.collection = data.listGiftEntity

        adapter.clickListener = {
            tvGiftsNew?.text = null
            profileUIActionHandler(UserProfileUIAction.OnGiftClick)
        }
    }

    private fun hideEmptyPlaceholder() {
        tvAddGift?.visible()
        containerSendGiftBtn.visible()
        rvGifts.visible()
        layoutEmptyGifts.gone()
        separator?.setMargins(top = 9.dp)
    }

    private fun showEmptyPlaceholder() {
        tvAddGift?.gone()
        containerSendGiftBtn.gone()
        rvGifts.gone()
        layoutEmptyGifts.visible()
        tvGiftsAmount?.text = itemView.context.getString(R.string.zero)
        separator?.setMargins(top = 16.dp)
    }

    private fun setupVipTheme() {
        val context = itemView.context
        profileTitleGifts?.setTextColor(ContextCompat.getColor(context, R.color.white_1000))
        tvGiftsAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_light_gray))
        tvAddGift?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        layoutEmptyGifts?.setBackgroundTint(R.color.ui_yellow)
        ivGiftsPlaceholder?.applyColorFilter(context, R.color.ui_yellow)
        tvGiftsPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
        tvBtnSendGift?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        tvGiftsPlaceholderTwo?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
        ivSendGift?.applyColorFilter(context, R.color.ui_yellow)
        tvSendGift?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
    }

    private fun setupCommonTheme() {
        val context = itemView.context
        profileTitleGifts?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        tvGiftsAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        tvAddGift?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
        layoutEmptyGifts?.setBackgroundTint(R.color.ui_gray)
        ivGiftsPlaceholder?.applyColorFilter(context, R.color.ui_active_filter)
        tvGiftsPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        tvBtnSendGift?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
        tvGiftsPlaceholderTwo?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        ivSendGift?.applyColorFilter(context, R.color.ui_purple)
        tvSendGift?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
    }
}
