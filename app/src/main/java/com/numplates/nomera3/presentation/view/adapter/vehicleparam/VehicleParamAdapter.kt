package com.numplates.nomera3.presentation.view.adapter.vehicleparam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleParamModel

class VehicleParamAdapter(private val mData: MutableList<VehicleParamModel>) : RecyclerView.Adapter<VehicleParamAdapter.ViewHolder>() {

    var interactor: IVehicleParamAdapterInteractor? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vehicle_param, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    fun add(data: MutableList<VehicleParamModel>) {
        mData.addAll(data)
        notifyDataSetChanged()
    }


    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private var contentText = v.findViewById<TextView>(R.id.tv_vehicle_param_txt)
        private var llConatainer = v.findViewById<LinearLayout>(R.id.cv_vehicle_container)
        private lateinit var param: VehicleParamModel

        init {
            llConatainer.setOnClickListener {
                interactor?.onParamClicked(param)
            }
        }

        fun bind(model: VehicleParamModel) {
            param = model
            contentText.text = param.index.toString()
        }
    }

    interface IVehicleParamAdapterInteractor {
        fun onParamClicked(param: VehicleParamModel)
    }
}