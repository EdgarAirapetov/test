package com.numplates.nomera3.presentation.view.adapter.vehicleparam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleBrandModel
import com.meera.core.extensions.loadGlide

class VehicleBrandAdapter(
        val mData: MutableList<VehicleBrandModel>
): RecyclerView.Adapter<VehicleBrandAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle_brand, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mData[position])
    }


    class ViewHolder(v: View):RecyclerView.ViewHolder(v) {

        val brandName = v.findViewById<TextView>(R.id.tv_brand)
        val brandImg = v.findViewById<ImageView>(R.id.iv_brand_img)

        fun bind(model: VehicleBrandModel){
            brandName.text = model.brand.name
            if (model.brand.avatar.isNotEmpty())
                brandImg.loadGlide(model.brand.avatar)
            else brandImg.loadGlide(R.drawable.car_choice)
        }
    }
}