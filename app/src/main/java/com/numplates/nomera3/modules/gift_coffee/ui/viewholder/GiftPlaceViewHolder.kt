package com.numplates.nomera3.modules.gift_coffee.ui.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.gift_coffee.ui.entity.GiftPlaceEntity
import com.meera.core.extensions.click
import com.meera.core.extensions.loadGlideCircle

class GiftPlaceViewHolder(viewGroup: ViewGroup):
        BaseViewHolder(viewGroup, R.layout.item_gift_place) {

    private val ivLogo: ImageView = itemView.findViewById(R.id.iv_logo)
    private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    private val tvAddress: TextView = itemView.findViewById(R.id.tv_address)

    fun bind(place: GiftPlaceEntity?, clickListener: (GiftPlaceEntity?) -> Unit) {
        ivLogo.loadGlideCircle(place?.image)
        tvTitle.text = place?.title
        tvAddress.text = place?.address
        itemView.click { clickListener(place) }
    }

}