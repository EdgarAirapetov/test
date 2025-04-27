package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.empty
import com.meera.core.extensions.getListRangeSpannableColored
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.GiftSenderUser
import com.meera.uikit.widgets.cell.CellLeftElement
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserGiftsItemBinding
import com.numplates.nomera3.modules.tags.ui.MovementMethod
import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity
import com.numplates.nomera3.presentation.view.utils.NTime

class MeeraUserGiftsListAdapter(
    private val ownUserId: Long,
    private val requestedUserId: Long?,
    private val adapterListener: MeeraUserGiftsListAdapterClickListener
) : ListAdapter<UserGiftsUiEntity, MeeraUserGiftsListAdapter.PhotoProfileViewHolder>(DiffCallback()) {

    var longClickListener: (Int, UserGiftsUiEntity?) -> Unit = { _, _ -> }
    var avatarClickListener: (Long) -> Unit = {}
    var sendGiftBackClickListener: ((userId: Long?, userName: String?, dateOfBirth: Long) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoProfileViewHolder {
        return PhotoProfileViewHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: PhotoProfileViewHolder, position: Int) {
        holder.bind(currentList[position], ownUserId, requestedUserId)
    }

    inner class PhotoProfileViewHolder(
        private val binding: MeeraUserGiftsItemBinding,
        private val resource: Resources
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(gift: UserGiftsUiEntity, ownUserId: Long, requestedUserId: Long?) {
            itemView.longClick { longClickListener(bindingAdapterPosition, gift) }

            if (gift.giftEntity.metadata?.isReceived == true) binding.vGiftStateNew.visible()

            if (gift.giftEntity.imageBig.isNotEmpty()) {
                binding.ivImageGift.loadGlide(gift.giftEntity.imageBig)
            }

            binding.tvHeaderGiftTime.text = NTime.timeAgo(gift.giftEntity.addedAt)
            gift.giftEntity.comment?.let { comment ->
                if (!gift.birthdayTextRanges.isNullOrEmpty()) {
                    binding.tvGiftComment.text = getSpannableIfBirthday(gift)
                    binding.tvGiftComment.movementMethod = MovementMethod
                } else {
                    binding.tvGiftComment.text = comment
                }
                binding.tvGiftComment.isVisible = comment.isNotEmpty()
            } ?: kotlin.run {
                binding.tvGiftComment.text = String.empty()
                binding.tvGiftComment.gone()
            }
            handleLabel(gift)
            handleHolidayGift(gift)

            if (
                gift.giftEntity.senderUser?.userId != null
                && ownUserId != gift.giftEntity.senderUser?.userId
            ) {
                setActionButtonSendBack(gift)
            }

            gift.giftEntity.senderUser?.let { senderUser ->
                if (ownUserId == requestedUserId) showSenderInfo(senderUser)
                else if (ownUserId == senderUser.userId) showSenderInfo(senderUser)

                binding.vHeaderGiftCell.setThrottledClickListener {
                    senderUser.userId?.let { userId ->
                        avatarClickListener(userId)
                    }
                }
            } ?: kotlin.run {
                if (ownUserId == requestedUserId) {
                    showSenderAnonymousInfo()
                } else {
                    binding.tvGiftComment.gone()
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
            binding.vBtnSendGiftReturn.gone()
        }

        private fun handleHolidayGift(gift: UserGiftsUiEntity) {
            if (gift.giftEntity.typeId != TYPE_GIFT_HOLIDAY && gift.giftEntity.typeId != TYPE_GIFT_HOLIDAY_NEW_YEAR) {
                hideCustomViewsAndClear()
                return
            }
            if (gift.giftEntity.customTitle != null) binding.tvGiftComment.text = gift.giftEntity.customTitle.orEmpty()
            if (gift.giftEntity.holidayTitle != null) binding.tvGiftComment.text =
                gift.giftEntity.holidayTitle.orEmpty()

            if (gift.giftEntity.senderUser?.userId != null
                && ownUserId != gift.giftEntity.senderUser?.userId
                && gift.giftEntity.senderUser?.userId != NOOMEERA_ACCOUNT_ID
            ) {
                setActionButtonSendBack(gift)
            } else {
                binding.vBtnSendGiftReturn.gone()
            }
        }

        private fun setActionButtonSendBack(gift: UserGiftsUiEntity) {
            binding.vBtnSendGiftReturn.click {
                sendGiftBackClickListener?.invoke(
                    gift.giftEntity.senderUser?.userId,
                    gift.giftEntity.senderUser?.name,
                    gift.giftEntity.senderUser?.birthday ?: 0
                )
            }
            binding.vBtnSendGiftReturn.visible()
        }

        private fun handleLabel(gift: UserGiftsUiEntity) {
            if (gift.giftEntity.metadata?.isReceived == true
                && ownUserId == requestedUserId
            ) {
                binding.vBtnSendGiftReturn.gone()
            }
        }

        private fun showSenderInfo(sender: GiftSenderUser) {
            binding.vHeaderGiftCell.cellLeftElement = CellLeftElement.USER_PIC_40
            binding.vHeaderGiftCell.setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = sender.avatar?.avatarSmall
                )
            )
            binding.vHeaderGiftCell.setTitleValue(sender.name ?: "") //
            binding.vHeaderGiftCell.cellCityText = true
            binding.vHeaderGiftCell.setCityValue(
                (resource.getString(R.string.uniquename_prefix) + sender.uniqueName)
            )
        }

        private fun showSenderAnonymousInfo() {
            binding.vHeaderGiftCell.cellLeftElement = CellLeftElement.ICON
            binding.vHeaderGiftCell.setLeftIcon(R.drawable.ic_outlined_user_m)
            binding.vHeaderGiftCell.setTitleValue(itemView.context.getString(R.string.meera_anonymously))
            binding.vHeaderGiftCell.cellCityText = false
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<UserGiftsUiEntity>() {
        override fun areItemsTheSame(oldItem: UserGiftsUiEntity, newItem: UserGiftsUiEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserGiftsUiEntity, newItem: UserGiftsUiEntity): Boolean {
            return oldItem == newItem
        }
    }
}
