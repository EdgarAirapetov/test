package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.empty
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.getListRangeSpannableColored
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.longClick
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleGone
import com.meera.db.models.userprofile.GiftSenderUser
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_REGULAR
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.tags.ui.MovementMethod
import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.widgets.VipView

const val NOOMEERA_ACCOUNT_ID = 3041692L

interface UserGiftsListAdapterClickListener {
    fun onClickToCoffeeLikeGift(gift: UserGiftsUiEntity)
    fun onChooseCoffee(gift: UserGiftsUiEntity)
    fun onLongClick(position: Int, data: UserGiftsUiEntity?)
    fun onBirthdayTextClicked()
}

class UserGiftsListAdapter(
    private val zeroDataText: String,
    private val ownUserId: Long,
    private val requestedUserId: Long?,
    private val adapterListener: UserGiftsListAdapterClickListener
) : ListAdapter<UserGiftsUiEntity, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<UserGiftsUiEntity>() {
    override fun areItemsTheSame(oldItem: UserGiftsUiEntity, newItem: UserGiftsUiEntity): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: UserGiftsUiEntity, newItem: UserGiftsUiEntity): Boolean {
        return newItem == oldItem
    }

}) {

    var longClickListener: (Int, UserGiftsUiEntity?) -> Unit = { _, _ -> }
    var avatarClickListener: (Long) -> Unit = {}
    var sendGiftBackClickListener: ((userId: Long?, userName: String?, dateOfBirth: Long) -> Unit)? = null

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).giftViewType == 1) ITEM_TYPE_COMMON else ITEM_TYPE_ZERO

    fun getItemForPosition(position: Int): UserGiftsUiEntity = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ITEM_TYPE_COMMON -> PhotoProfileViewHolder(parent)

            ITEM_TYPE_ZERO -> ZeroItemViewHolder(parent)

            else -> ZeroItemViewHolder(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PhotoProfileViewHolder ->
                holder.bind(getItem(position), ownUserId, requestedUserId)

            is ZeroItemViewHolder -> holder.bind(zeroDataText)
        }
    }

    inner class PhotoProfileViewHolder(
        viewGroup: ViewGroup
    ) : BaseViewHolder(viewGroup, R.layout.item_gift_large) {

        private val ivGift: ImageView = itemView.findViewById(R.id.ivGift)
        private val tvSentGiftDate: TextView = itemView.findViewById(R.id.tv_date_when_sent_gift)
        private val tvMessageGift: TextView = itemView.findViewById(R.id.tv_message_gift)
        private val vvUserAvatar: VipView = itemView.findViewById(R.id.iv_user_who_sent_gift)
        private val tvUser: TextView = itemView.findViewById(R.id.tv_user_who_sent_gift)
        private val btnAction = itemView.findViewById<TextView>(R.id.tv_btn_action)
        private val tvCustomTitle = itemView.findViewById<TextView>(R.id.tv_custom_title)
        private val tvHolidayTitle = itemView.findViewById<TextView>(R.id.tv_holiday_title)
        private val vSenderClickArea = itemView.findViewById<View>(R.id.vSenderClickArea)

        fun bind(gift: UserGiftsUiEntity, ownUserId: Long, requestedUserId: Long?) {
            itemView.longClick { longClickListener(bindingAdapterPosition, gift) }

            if (gift.giftEntity.imageBig.isNotEmpty()) {
                ivGift.loadGlide(gift.giftEntity.imageBig)
            }

            tvSentGiftDate.text = NTime.timeAgo(gift.giftEntity.addedAt)
            gift.giftEntity.comment?.let { comment ->
                if (!gift.birthdayTextRanges.isNullOrEmpty()) {
                    tvMessageGift.text = getSpannableIfBirthday(gift)
                    tvMessageGift.movementMethod = MovementMethod
                } else {
                    tvMessageGift.text = comment
                }
                tvMessageGift.isVisible = comment.isNotEmpty()
            } ?: kotlin.run {
                tvMessageGift.text = String.empty()
                tvMessageGift.gone()
            }
            handleLabel(gift)
            handleHolidayGift(gift)

            when {
                ownUserId == requestedUserId && gift.giftEntity.typeId == TYPE_GIFT_COFFEE_LIKE -> {
                    handleCoffeeType(gift)
                }

                gift.giftEntity.senderUser?.userId != null
                    && ownUserId != gift.giftEntity.senderUser?.userId -> {
                    setActionButtonSendBack(gift)
                }
            }

            // Handle show avatar and name depends on user type
            gift.giftEntity.senderUser?.let { senderUser ->    // Not anonymous
                // own profile
                if (ownUserId == requestedUserId) showSenderInfo(senderUser)
                else if (ownUserId == senderUser.userId) showSenderInfo(senderUser)

                vSenderClickArea.click {
                    senderUser.userId?.let { userId ->
                        avatarClickListener(userId)
                    }
                }
            } ?: kotlin.run {   // Anonymous
                vSenderClickArea.click { }
                if (ownUserId == requestedUserId) {
                    showSenderAnonymousInfo()
                } else {
                    vvUserAvatar.invisible()
                    tvUser.gone()
                    tvMessageGift.gone()
                }

            }
        }

        private fun getSpannableIfBirthday(entity: UserGiftsUiEntity): SpannableStringBuilder {
            return SpannableStringBuilder(entity.giftEntity.comment)
                .getListRangeSpannableColored(
                    rangeList = entity.birthdayTextRanges,
                    color = itemView.context.color(R.color.ui_purple)
                ) {
                    adapterListener.onBirthdayTextClicked()
                }
        }

        private fun hideCustomViewsAndClear() {
            tvCustomTitle.text = String.empty()
            tvHolidayTitle.text = String.empty()
            tvCustomTitle.gone()
            tvHolidayTitle.gone()
            btnAction.gone()
        }

        private fun handleHolidayGift(gift: UserGiftsUiEntity) {
            if (gift.giftEntity.typeId != TYPE_GIFT_HOLIDAY && gift.giftEntity.typeId != TYPE_GIFT_HOLIDAY_NEW_YEAR) {
                hideCustomViewsAndClear()
                return
            }
            tvCustomTitle.isVisible = gift.giftEntity.customTitle != null
            tvCustomTitle.text = gift.giftEntity.customTitle.orEmpty()
            tvHolidayTitle.isVisible = gift.giftEntity.holidayTitle != null
            tvHolidayTitle.text = gift.giftEntity.holidayTitle.orEmpty()
            if (gift.giftEntity.senderUser?.userId != null
                && ownUserId != gift.giftEntity.senderUser?.userId
                && gift.giftEntity.senderUser?.userId != NOOMEERA_ACCOUNT_ID
            ) {
                setActionButtonSendBack(gift)
            } else {
                btnAction.gone()
            }
        }

        private fun setActionButtonSendBack(gift: UserGiftsUiEntity) {
            btnAction.text = itemView.context.getString(R.string.gifts_send_gift_back)
            btnAction.click {
                sendGiftBackClickListener?.invoke(
                    gift.giftEntity.senderUser?.userId,
                    gift.giftEntity.senderUser?.name,
                    gift.giftEntity.senderUser?.birthday ?: 0
                )
            }
            btnAction.visible()
        }

        private fun handleCoffeeType(gift: UserGiftsUiEntity) {
            val isExistCode = gift.giftEntity.metadata?.coffeeCode?.isNotEmpty() ?: false
            if (isExistCode) {
                itemView.click {
                    if (gift.giftEntity.metadata?.isReceived == true) return@click
                    adapterListener.onClickToCoffeeLikeGift(gift)
                }
                btnAction.text = itemView.context.string(R.string.show_code)
            } else {
                itemView.click {
                    if (gift.giftEntity.metadata?.isReceived == true) return@click
                    adapterListener.onChooseCoffee(gift)
                }
                btnAction.text = itemView.context.string(R.string.to_choose_coffee)
            }

            if (gift.giftEntity.hasCustomCoffeeDrawable()) {
                ivGift.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context, gift.giftEntity.metadata?.coffeeCustomDrawable!!
                    )
                )
            }
        }

        private fun handleLabel(gift: UserGiftsUiEntity) {
            if (gift.giftEntity.metadata?.isReceived == true
                && ownUserId == requestedUserId
                && gift.giftEntity.typeId == TYPE_GIFT_COFFEE_LIKE
            ) {
                btnAction.gone()
            } else {
                btnAction.visibility =
                    (gift.giftEntity.typeId == TYPE_GIFT_COFFEE_LIKE).visibleGone()
            }
        }

        private fun showSenderInfo(sender: GiftSenderUser) {
            if (sender.accountColor != null && sender.accountType != null) {
                vvUserAvatar.setUp(
                    vvUserAvatar.context,
                    sender.avatar?.avatarSmall,
                    sender.accountType ?: 1,
                    sender.accountColor ?: ACCOUNT_TYPE_REGULAR,
                    hasShadow = false
                )

                // в макете фото отправиля размером 44х44
                // но VipView откусывает внутренним паддингом 6 dp
                // даже если в атрибутах VipView ставим Size_45 то
                // будет в районе 38 dp а не 44. Todo что-то надо делоть с этим (с VipView) 🥲
                vvUserAvatar.resetUserPhotoPadding()
                vvUserAvatar.visible()
            } else {
                vvUserAvatar.invisible()
            }
            tvUser.isVisible = sender.name != null
            tvUser.text = if (sender.name != null) sender.name else String.empty()
        }

        private fun showSenderAnonymousInfo() {
            vvUserAvatar.setUp(
                vvUserAvatar.context,
                itemView.context.getDrawableCompat(R.drawable.incognita),
                ACCOUNT_TYPE_REGULAR,
                ACCOUNT_TYPE_REGULAR,
                hasShadow = false
            )

            vvUserAvatar.resetUserPhotoPadding()
            vvUserAvatar.visible()
            tvUser.text = itemView.context.getString(R.string.anonymously)
            tvUser.visible()
        }
    }

    inner class ZeroItemViewHolder(
        viewGroup: ViewGroup
    ) : BaseViewHolder(viewGroup, R.layout.item_zero_gifts) {

        private val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        private val tvEmptyGarage: TextView = itemView.findViewById(R.id.tvEmptyGarage)

        fun bind(zeroDataText: String) {
            ivPicture.loadGlide(R.drawable.empty_gifts)
            tvEmptyGarage.text = zeroDataText
        }
    }

    companion object {
        const val ITEM_TYPE_COMMON = 1
        const val ITEM_TYPE_ZERO = 0
    }
}
