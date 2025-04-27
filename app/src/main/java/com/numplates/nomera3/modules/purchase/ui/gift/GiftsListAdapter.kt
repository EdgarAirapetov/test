package com.numplates.nomera3.modules.purchase.ui.gift

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.loadGlide
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel

const val TYPE_INNER_COFFEE_LIKE = 0
const val TYPE_INNER_DEF = 1
const val TYPE_INNER_HOLIDAY_GIFT = 2

class GiftsListAdapter(
    private var type: Int = TYPE_INNER_DEF,
    private var clickListener: (GiftItemUiModel, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<GiftItemUiModel> = mutableListOf()

    override fun getItemViewType(position: Int) = type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_INNER_COFFEE_LIKE -> CoffeeLikeViewHolder(parent)
            TYPE_INNER_HOLIDAY_GIFT -> HolidayGiftViewHolder(parent)
            else -> DefViewHolder(parent)
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
        viewGroup: ViewGroup
    ) : BaseViewHolder(viewGroup, R.layout.item_gift) {

        private val ivGiftImage: ImageView = itemView.findViewById(R.id.ivGift)
        private val tvGiftPrice: TextView = itemView.findViewById(R.id.tvPrice)

        fun bind(giftItem: GiftItemUiModel) {
            ivGiftImage.loadGlide(giftItem.image)
            tvGiftPrice.text = giftItem.price
            itemView.click { clickListener(giftItem, TYPE_INNER_DEF) }
        }
    }

    inner class CoffeeLikeViewHolder(
        viewGroup: ViewGroup
    ) : BaseViewHolder(viewGroup, R.layout.item_coffee_like_gift_floor) {

        private val price = itemView.findViewById<TextView>(R.id.price)
        private val subTitle = itemView.findViewById<TextView>(R.id.subTitle)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val giftIcon = itemView.findViewById<ImageView>(R.id.gift)

        fun bind(data: GiftItemUiModel) {
            itemView.click { clickListener(data, TYPE_INNER_COFFEE_LIKE) }
            giftIcon.loadGlide(data.image)
            price.text = data.price
            title.text = data.customTitle
            subTitle.text = itemView.context.getString(R.string.coffee_gift_text)
        }
    }

    inner class HolidayGiftViewHolder(
        viewGroup: ViewGroup
    ) : BaseViewHolder(viewGroup, R.layout.item_holiday_gift) {

        private val tvPrice = itemView.findViewById<TextView>(R.id.tv_price)
        private val tvDesc = itemView.findViewById<TextView>(R.id.tv_desc)
        private val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
        private val ivGiftImage = itemView.findViewById<ImageView>(R.id.iv_img)

        fun bind(data: GiftItemUiModel) {
            itemView.click { clickListener(data, TYPE_INNER_HOLIDAY_GIFT) }
            ivGiftImage.loadGlide(data.image)
            tvPrice.text = data.price
            tvDesc.text = data.customDesc
            tvTitle.text = data.customTitle
        }
    }
}
