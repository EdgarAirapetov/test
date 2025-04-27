package com.numplates.nomera3.modules.purchase.ui.gift

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.click
import com.meera.core.extensions.loadGlide
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemCoffeeLikeGiftFloorBinding
import com.numplates.nomera3.databinding.MeeraGiftItemBinding
import com.numplates.nomera3.databinding.MeeraHolidayGiftItemBinding
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel

class MeeraGiftsListAdapter(
    private var type: Int = TYPE_INNER_DEF,
    private var clickListener: (GiftItemUiModel, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<GiftItemUiModel> = mutableListOf()

    override fun getItemViewType(position: Int) = type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_INNER_COFFEE_LIKE -> CoffeeLikeViewHolder(parent.toBinding())
            TYPE_INNER_HOLIDAY_GIFT -> HolidayGiftViewHolder(parent.toBinding())
            else -> DefViewHolder(parent.toBinding())
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holderDef: RecyclerView.ViewHolder, position: Int) {
        when (holderDef) {
            is CoffeeLikeViewHolder -> holderDef.bind(data[position])
            is DefViewHolder -> holderDef.bind(data[position])
            is HolidayGiftViewHolder -> holderDef.bind(data[position])
        }
    }

    fun refresh(items: List<GiftItemUiModel>, newType: Int = TYPE_INNER_DEF) {
        data.clear()
        data.addAll(items)
        type = newType
        notifyDataSetChanged()
    }

    inner class DefViewHolder(
        val binding: MeeraGiftItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(giftItem: GiftItemUiModel) {
            binding.ivGift.loadGlide(giftItem.image)
            binding.tvPrice.text = giftItem.price
            itemView.click { clickListener(giftItem, TYPE_INNER_DEF) }
        }
    }

    inner class CoffeeLikeViewHolder(
        val binding: ItemCoffeeLikeGiftFloorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GiftItemUiModel) {

            itemView.click { clickListener(data, TYPE_INNER_COFFEE_LIKE) }
            binding.gift.loadGlide(data.image)
            binding.price.text = data.price
            binding.title.text = data.customTitle
            binding.subTitle.text = itemView.context.getString(R.string.coffee_gift_text)
        }
    }

    inner class HolidayGiftViewHolder(
        val binding: MeeraHolidayGiftItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GiftItemUiModel) {
            itemView.click { clickListener(data, TYPE_INNER_HOLIDAY_GIFT) }
            binding.ivImg.loadGlide(data.image)
            binding.tvPrice.text = data.price
            binding.tvDesc.text = data.customDesc
            binding.tvTitle.text = data.customTitle
        }
    }
}
