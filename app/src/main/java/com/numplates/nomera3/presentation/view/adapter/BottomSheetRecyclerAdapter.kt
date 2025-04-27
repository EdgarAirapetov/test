package com.numplates.nomera3.presentation.view.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.loadGlideCircle
import com.meera.core.extensions.visible
import kotlin.properties.Delegates


class BottomSheetRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    internal var collection: List<BottomSheetItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var clickListener: (BottomSheetItem) -> Unit = { _ -> }


    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.bottom_sheet_recycler_item))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder

        vh.bind(collection[position], clickListener)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.findViewById(R.id.item_image)
        val title: TextView = itemView.findViewById(R.id.item_title)
        val separator: ImageView = itemView.findViewById(R.id.item_separator)

        fun bind(item: BottomSheetItem, clickListener: (BottomSheetItem) -> Unit) {
            item.image?.let { image.loadGlideCircle(it) }
            title.text = item.title
            itemView.setOnClickListener { clickListener(item) }
            if (itemCount - 1 == this.bindingAdapterPosition){
                separator.gone()
            } else {
                separator.visible()
            }

        }
    }


    data class BottomSheetItem(
            val id: Int?,
            val image: String?,
            val title: String?,

            // 0 - vehicles
            // 1 - countries
            val itemType: Int = 0
    )

}