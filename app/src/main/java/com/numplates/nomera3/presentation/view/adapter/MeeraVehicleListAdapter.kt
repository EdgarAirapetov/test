package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.VehiclePlateTypeSize
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemVehilceBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_AUTO
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_MOTO
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleModel

class MeeraVehicleListAdapter(
    private var data: MutableList<VehicleModel>,
    private var isNeedToShowMainVehicle: Boolean = true,
    private var clickListener: (VehicleModel) -> Unit = { _ -> }
) : RecyclerView.Adapter<MeeraVehicleListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MeeraItemVehilceBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun addItems(items: List<VehicleModel>) {
        val previousCount = itemCount
        data.addAll(items)
        notifyItemRangeInserted(previousCount, items.size)
    }

    fun updateItem(item: VehicleModel) {
        val index = data.indexOfFirst { it.vehicle?.vehicleId == item.vehicle?.vehicleId }
        if (index > -1) {
            data[index] = item
            notifyItemChanged(index)
        }
    }

    fun clearItems() {
        data.clear()
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: MeeraItemVehilceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: VehicleModel) {
            itemView.setOnClickListener {
                clickListener.invoke(model)
            }

            model.vehicle?.let { vehicle ->

                with(binding) {
                    val photoThumb = when (vehicle.type?.typeId?.toIntOrNull()) {
                        TYPE_AUTO -> R.drawable.meera_car_choice
                        TYPE_MOTO -> R.drawable.meera_moto_choice
                        else -> R.drawable.meera_car_choice
                    }
                    if (vehicle.type != null && vehicle.type?.hasMakes != 0 && vehicle.make != null) {
                        ivCarMaker.loadGlide(vehicle.make?.imageUrl)
                        ivCarMaker.visible()
                    } else {
                        ivCarMaker.gone()
                    }

                    if (vehicle.picture.isNullOrEmpty()) {
                        Glide.with(ivCarMaker.context).load(photoThumb).into(ivPhoto)
                    } else {
                        Glide.with(ivCarMaker.context).load(vehicle.picture).into(ivPhoto)
                    }

                    if (vehicle.number.isNullOrEmpty().not()) {
                        vehiclePlateView.visible()
                        val plateType =
                            if (vehicle.type?.typeId == VehicleUIModel.TYPE_AUTO.toString()) VehiclePlateTypeSize.MEDIUM_AUTO
                            else VehiclePlateTypeSize.MEDIUM_MOTO
                        vehiclePlateView.setTypeSize(plateType)
                        vehiclePlateView.text = vehicle.number ?: ""
                    } else {
                        vehiclePlateView.invisible()
                    }



                    if (vehicle.mainVehicle == 1 && isNeedToShowMainVehicle) {
                        tvMainVehicle.visibility = View.VISIBLE
                    } else {
                        tvMainVehicle.visibility = View.GONE
                    }

                    if (vehicle.make != null) {
                        tvMaker.text = vehicle.make?.name
                    }

                    if (vehicle.model != null) {
                        tvModel.text = vehicle.model?.name
                    }
                }
            }
        }
    }
}
