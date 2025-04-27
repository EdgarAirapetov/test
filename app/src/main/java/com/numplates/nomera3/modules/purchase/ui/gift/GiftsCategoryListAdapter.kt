package com.numplates.nomera3.modules.purchase.ui.gift

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.purchase.ui.model.GiftCategoryUiModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_COFFEE_LIKE
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY

class GiftsCategoryListAdapterNew(
    private val mData: MutableList<GiftCategoryUiModel> = mutableListOf(),
    private val clickListener: (GiftItemUiModel, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<GiftsCategoryListAdapterNew.GiftViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder =
        GiftViewHolder(parent)

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    fun addItems(items: List<GiftCategoryUiModel>?) {
        items?.let {
            val pos = itemCount
            mData.addAll(it)
            notifyItemRangeInserted(pos, items.size)
        }
    }

    inner class GiftViewHolder(
        viewGroup: ViewGroup
    ) : BaseViewHolder(viewGroup, R.layout.item_gift_category) {

        private val tvHeader: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvItemsCount: TextView = itemView.findViewById(R.id.tvCategorySize)
        private val rvItems: RecyclerView = itemView.findViewById(R.id.rvGifts)

        private val adapter: GiftsListAdapter

        init {
            rvItems.layoutManager =
                LinearLayoutManager(
                    rvItems.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            adapter = GiftsListAdapter(clickListener = clickListener)
            rvItems.adapter = adapter
        }

        fun bind(data: GiftCategoryUiModel) {
            tvHeader.text = data.categoryName
            val shouldShowCount = data.gifts.any { gift ->
                gift.type == TYPE_GIFT_COFFEE_LIKE || gift.type == TYPE_GIFT_HOLIDAY
            }
            if (shouldShowCount) {
                tvItemsCount.gone()
            } else {
                tvItemsCount.text = itemView.context.getString(R.string.gifts_gift_category_size, data.gifts.size)
                tvItemsCount.visible()
            }
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
}
