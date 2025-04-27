package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.data.network.VehicleType
import com.numplates.nomera3.databinding.MeeraItemVehicleTypeBinding
import com.numplates.nomera3.presentation.view.holder.MeeraVehicleTypeHolder

class MeeraVehicleTypeAdapter(
) : ListAdapter<VehicleType, MeeraVehicleTypeHolder>(object : DiffUtil.ItemCallback<VehicleType>() {
    override fun areItemsTheSame(oldItem: VehicleType, newItem: VehicleType): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: VehicleType, newItem: VehicleType): Boolean {
        return oldItem == newItem
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraVehicleTypeHolder {
        val binding = MeeraItemVehicleTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeeraVehicleTypeHolder(binding)
    }

    override fun onBindViewHolder(holder: MeeraVehicleTypeHolder, position: Int) {
        holder.onBind(getItem(position))
    }


    fun getVehicleItem(position: Int): VehicleType {
        return getItem(position)
    }
}
