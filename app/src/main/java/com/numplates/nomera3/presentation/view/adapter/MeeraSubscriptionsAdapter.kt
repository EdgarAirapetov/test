package com.numplates.nomera3.presentation.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.people.UiKitUsernameView
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSubscribersUserItemBinding

private const val MARGIN_START_DIVIDER = 8

/**
 * This adapter is used for both subscribers and subscriptions list
 * as they have same ui
 * */
class MeeraSubscriptionsAdapter(
    private val listener: (action: MeeraSubscriptionAction) -> Unit
) : ListAdapter<SubscriptionAdapterModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SubscriptionViewHolder(parent.toBinding())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SubscriptionViewHolder).bind(currentList[position], position)
    }

    /**
     * This viewHolder is used for both subscribers and subscriptions list
     * */
    inner class SubscriptionViewHolder(val binding: MeeraSubscribersUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            model: SubscriptionAdapterModel,
            position: Int
        ) {
            binding.vUserItem.cellCityText = true
            binding.vUserItem.setMarginStartDivider(MARGIN_START_DIVIDER.dp)
            initUserAvatar(model.user.avatarSmall, model.user.gender)

            if (position == currentList.lastIndex) {
                binding.vUserItem.cellPosition = CellPosition.BOTTOM
            } else {
                binding.vUserItem.cellPosition = CellPosition.MIDDLE
            }

            if (!model.isSubscription) {
                binding.vUserItem.setSubtitleValue(binding.root.context.getString(R.string.general_subscribe))
                binding.vUserItem.setSubtitleTextColor(R.color.uiKitColorAccentPrimary)
            } else {
                binding.vUserItem.setSubtitleValue(binding.root.context.getString(R.string.reading))
                binding.vUserItem.setSubtitleTextColor(R.color.uiKitColorForegroundSecondary)
            }
            binding.ivDeleteBtn.gone()

            binding.vUserItem.setThrottledClickListener {
                listener.invoke(MeeraSubscriptionAction.ProfileAreaClick(model))
            }
            binding.vUserItem.setSubtitleClickListener {
                if (model.isSubscription) {
                    listener.invoke(MeeraSubscriptionAction.UnsubscribeBtnClick(model))
                } else {
                    listener.invoke(MeeraSubscriptionAction.SubscribeBtnClick(model))
                }
            }
            binding.vUserItem.setTitleValue(model.user.name ?: "")
            binding.vUserItem.cellTitleVerified = model.user.approved.toBoolean()
            binding.vUserItem.findViewById<UiKitUsernameView>(R.id.tv_title)?.apply {
                enableTopContentAuthorApprovedUser(
                    params = TopAuthorApprovedUserModel(
                        customIconTopContent = R.drawable.ic_approved_author_gold_10,
                        approvedIconSize = ApprovedIconSize.SMALL,
                        approved = model.user.approved.toBoolean(),
                        interestingAuthor = model.user.topContentMaker.toBoolean()
                    )
                )
            }

            model.user.city?.let {
                binding.vUserItem.setCityValue(it.name ?: "")
            }

            model.user.uniqueName?.let { uName: String ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.vUserItem.setDescriptionValue(formattedUniqueName)
                }
            } ?: kotlin.run {
                binding.vUserItem.cellDescription = false
            }
        }

        private fun initUserAvatar(avatarSmall: String?, gender: Int?) {
            avatarSmall?.let {
                binding.vUserItem.setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarUrl = avatarSmall,
                        userAvatarErrorPlaceholder = identifyAvatarByGender(gender)
                    )
                )
            } ?: {
                binding.vUserItem.setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarRes = identifyAvatarByGender(gender)
                    )
                )
            }
        }

        private fun identifyAvatarByGender(gender: Int?): Int {
            return if (gender.toBoolean()) {
                R.drawable.ic_man_avatar_placeholder
            } else {
                R.drawable.ic_woman_avatar_placeholder
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SubscriptionAdapterModel>() {
    override fun areContentsTheSame(
        oldItem: SubscriptionAdapterModel,
        newItem: SubscriptionAdapterModel
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: SubscriptionAdapterModel,
        newItem: SubscriptionAdapterModel
    ): Boolean {
        return oldItem == newItem
    }
}
