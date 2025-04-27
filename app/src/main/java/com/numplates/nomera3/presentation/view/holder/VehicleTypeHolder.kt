package com.numplates.nomera3.presentation.view.holder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.callback.VehickeTypeCallback


class VehicleTypeHolder(itemView: View,
                        private val callback: VehickeTypeCallback) : RecyclerView.ViewHolder(itemView) {


    var llContent: ViewGroup = itemView.findViewById(R.id.llContent)
    var ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
    var tvText: TextView = itemView.findViewById(R.id.tvText)
    var cv_vehicle_type_container: CardView = itemView.findViewById(R.id.cv_vehicle_type_container)

    init {
        llContent.setOnClickListener { callback.onClick(this@VehicleTypeHolder) }
    }
}