package com.numplates.nomera3.presentation.view.holder

import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.VehicleType
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_CAR
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_MOTO
import com.numplates.nomera3.databinding.MeeraItemVehicleTypeBinding


class MeeraVehicleTypeHolder(
    private val binding: MeeraItemVehicleTypeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: VehicleType?) {
        if (item == null) {
            binding.civVehicleTypeImage.setImageDrawable(null)
            binding.tvVehicleTypeTitle.text = null
            return
        }

        val typeImage = when (item.typeId) {
            VEHICLE_TYPE_CAR.toString() -> R.drawable.meera_car_choice
            VEHICLE_TYPE_MOTO.toString() -> R.drawable.meera_moto_choice
            else -> null
        }

        typeImage?.let { src ->
            binding.civVehicleTypeImage.setImageResource(src)
        }

        val title = StringBuilder()
        title.append(binding.root.context.getString(R.string.general_add))
        title.append(binding.root.context.getString(R.string.space))
        title.append(item.name?.lowercase())
        binding.tvVehicleTypeTitle.text = title.toString()
    }

}
