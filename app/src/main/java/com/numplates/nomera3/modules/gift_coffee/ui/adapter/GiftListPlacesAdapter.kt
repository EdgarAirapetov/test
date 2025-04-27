package com.numplates.nomera3.modules.gift_coffee.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.gift_coffee.ui.entity.GiftPlaceEntity
import com.numplates.nomera3.modules.gift_coffee.ui.viewholder.GiftPlaceViewHolder


class GiftListPlacesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val collection = mutableListOf<GiftPlaceEntity?>()

    fun addData(data: List<GiftPlaceEntity?>) {
        collection.addAll(data)
        notifyItemRangeInserted(collection.size - 1, data.size)
    }

    fun clearList() {
        collection.clear()
        notifyDataSetChanged()
    }

    internal var clickListener: (GiftPlaceEntity?) -> Unit = { _ -> }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            GiftPlaceViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GiftPlaceViewHolder).bind(collection[position], clickListener)
    }

}