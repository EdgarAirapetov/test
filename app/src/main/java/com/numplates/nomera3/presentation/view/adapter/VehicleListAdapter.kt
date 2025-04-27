package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.VipStatus
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleModel
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.numplates.nomera3.presentation.view.utils.NGraphics.getVehicleTypeMap
import com.numplates.nomera3.presentation.view.widgets.NumberNew
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import java.util.*

class VehicleListAdapter(
        private var vipStatus: VipStatus?,
        private var data: MutableList<VehicleModel>,
        private var isNeedToShowMainVehicle: Boolean = true,
        private var clickListener: (VehicleModel) -> Unit = { _ -> }
): RecyclerView.Adapter<VehicleListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vehilce, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun addItems(items: List<VehicleModel>?) {
        items?.let {
            val previousCount = itemCount
            data.addAll(it)
            notifyItemRangeInserted(previousCount, it.size)
        }
    }

    fun updateItem(item: VehicleModel) {
        val index = data.indexOfFirst { it.vehicle?.vehicleId == item.vehicle?.vehicleId }
        if (index > -1) {
            data[index] = item
            notifyItemChanged(index)
        }
    }

    fun clearItems(){
        data.clear()
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val tvMainVehicle: TextView = itemView.findViewById(R.id.tvMainVehicle)
        private val ivCarMaker: ImageView = itemView.findViewById(R.id.ivCarMaker)
        private val tvMaker: TextView = itemView.findViewById(R.id.tvMaker)
        private val tvModel: TextView = itemView.findViewById(R.id.tvModel)
        private val nvNumber: NumberPlateEditView = itemView.findViewById(R.id.nvNumber)
        private val nvNumberNew: NumberNew = itemView.findViewById(R.id.nv_number_new)
        private val cvCarMakerContainer: CardView = itemView.findViewById(R.id.cv_car_maker_container)


        fun bind(model: VehicleModel) {
            itemView.setOnClickListener {
                clickListener.invoke(model)
            }

            model.vehicle?.let { vehicle ->

                if (vehicle.type != null && vehicle.type?.hasMakes != 0 && vehicle.make != null) {
                    ivCarMaker.loadGlide(vehicle.make?.imageUrl)
                    cvCarMakerContainer.visible()
                } else {
                    cvCarMakerContainer.gone()
                }

                if (vehicle.type != null) {
                    vehicle.type?.getSelectedIcon(getVehicleTypeMap())?.let {
                        Glide.with(ivCarMaker.context)
                                .load(vehicle.picture)
                                .apply(RequestOptions.placeholderOf(it))
                                .apply(RequestOptions.circleCropTransform())
                                .into(ivPhoto)
                    }
                } else {
                    Glide.with(ivCarMaker.context)
                            .load(vehicle.picture)
                            .apply(RequestOptions.placeholderOf(R.drawable.vehicle_car))
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivPhoto)
                }

                if (vehicle.type != null && vehicle.type?.hasNumber != 0) {
                    nvNumberNew.visibility = View.GONE
                    nvNumber.visibility = View.VISIBLE
                    NumberPlateEditView.Builder(nvNumber)
                            .setVehicle(vehicle)
                            .build()
                    nvNumber.setBackgroundPlate(vehicle, vipStatus?.accountType, vipStatus?.accountColor)
                    if (vehicle.type?.typeId == "1") {
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(
                                (-74).dp,
                                (-16).dp,
                                (-74).dp,
                                (-16).dp
                        )
                        nvNumber.layoutParams = params
                    } else if (vehicle.type?.typeId == "2") {
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(
                                (-47).dp,
                                (-26).dp,
                                (-47).dp,
                                (-26).dp
                        )
                        nvNumber.layoutParams = params
                    }
                    nvNumber.scaleX = 0.5f
                    nvNumber.scaleY = 0.5f
                } else {
                    nvNumber.visibility = View.GONE
                    nvNumberNew.visibility = View.VISIBLE
                    if (vehicle.make != null) {
                        nvNumberNew.setName(Objects.requireNonNull<String>(vehicle.make?.name))
                    }
                    if (vehicle.model != null) {
                        nvNumberNew.setModel(Objects.requireNonNull<String>(vehicle.model?.name))
                    }
                    vipStatus?.let {
                        if (it.accountType != null && it.accountColor != null) {
                            nvNumberNew.setType(it.accountType!!, it.accountColor!!)
                        }
                    }
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
