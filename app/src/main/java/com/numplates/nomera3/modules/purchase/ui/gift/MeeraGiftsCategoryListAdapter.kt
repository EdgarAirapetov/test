package com.numplates.nomera3.modules.purchase.ui.gift

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.databinding.MeeraGiftCategoryItemBinding
import com.numplates.nomera3.modules.purchase.ui.model.GiftCategoryUiModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_COFFEE_LIKE
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY

class MeeraGiftsCategoryListAdapter(
    private val clickListener: (GiftItemUiModel, Int) -> Unit = { _, _ -> }
) : ListAdapter<GiftCategoryUiModel, MeeraGiftsCategoryListAdapter.GiftViewHolder>(FriendDiffUtils)
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder =
        GiftViewHolder(parent.toBinding())

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class GiftViewHolder(
        val binding: MeeraGiftCategoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val adapter: MeeraGiftsListAdapter

        init {
            binding.rvGifts.layoutManager =
                LinearLayoutManager(
                    binding.rvGifts.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            adapter = MeeraGiftsListAdapter(clickListener = clickListener)
            binding.rvGifts.adapter = adapter
        }

        fun bind(data: GiftCategoryUiModel) {
            binding.tvCategoryName.text = data.categoryName
            val shouldShowCount = data.gifts.any { gift ->
                gift.type == TYPE_GIFT_COFFEE_LIKE || gift.type == TYPE_GIFT_HOLIDAY
            }
            binding.tvCategorySize.text = data.gifts.size.toString()
            binding.tvCategorySize.isGone = shouldShowCount
            when (data.gifts.getOrNull(0)?.type ?: -1L) {
                TYPE_GIFT_COFFEE_LIKE -> {
                    adapter.refresh(data.gifts, TYPE_INNER_COFFEE_LIKE)
                }
                TYPE_GIFT_HOLIDAY -> {
                    adapter.refresh(data.gifts, TYPE_INNER_HOLIDAY_GIFT)
                }
                else -> {
                    adapter.refresh(data.gifts)
                }
            }
        }
    }

    private object FriendDiffUtils: DiffUtil.ItemCallback<GiftCategoryUiModel>() {
        override fun areItemsTheSame(oldItem: GiftCategoryUiModel, newItem: GiftCategoryUiModel): Boolean {
            return oldItem.categoryName == newItem.categoryName
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: GiftCategoryUiModel, newItem: GiftCategoryUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
