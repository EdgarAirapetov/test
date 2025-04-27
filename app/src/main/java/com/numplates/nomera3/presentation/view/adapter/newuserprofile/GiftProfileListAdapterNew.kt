package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleGone
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.GiftUIModel
import kotlin.properties.Delegates

const val TYPE_GIFT_COFFEE_LIKE = 4
const val TYPE_GIFT_HOLIDAY = 5
const val TYPE_GIFT_HOLIDAY_NEW_YEAR = 6

class GiftProfileListAdapterNew(
        private val isMe: Boolean = true
) : RecyclerView.Adapter<GiftProfileListAdapterNew.GiftViewHolder>() {

    internal var collection: List<GiftUIModel> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var clickListener: (GiftUIModel) -> Unit = { _ -> }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder =
            GiftViewHolder(parent.inflate(R.layout.item_gift_profile))

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(collection[position], clickListener)
    }

    inner class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivGift: ImageView = itemView.findViewById(R.id.ivGift)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val labelNew: TextView = itemView.findViewById(R.id.label_new)

        fun bind(gift: GiftUIModel, clickListener: (GiftUIModel) -> Unit) {
            ivGift.loadGlide(gift.image)
            tvPrice.gone()
            itemView.click { clickListener(gift) }
            handleLabel(gift)
        }

        private fun handleLabel(gift: GiftUIModel) {
            if (gift.isReceived == true
                    && isMe
                    && gift.typeId == TYPE_GIFT_COFFEE_LIKE) {
                labelNew.text = labelNew.context.string(R.string.received)
                labelNew.background = ContextCompat.getDrawable(labelNew.context, R.drawable.round_gradient_gray)
                labelNew.visible()
            } else {
                labelNew.text = labelNew.context.string(R.string.new_gift)
                labelNew.background = ContextCompat.getDrawable(labelNew.context, R.drawable.rounded_gradient)
                labelNew.visibility = (isMe &&
                        gift.typeId == TYPE_GIFT_COFFEE_LIKE
                        && gift.isViewed.not()).visibleGone()
            }
        }
    }
}
