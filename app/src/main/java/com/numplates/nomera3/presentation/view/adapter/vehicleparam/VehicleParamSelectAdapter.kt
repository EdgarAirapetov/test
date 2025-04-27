package com.numplates.nomera3.presentation.view.adapter.vehicleparam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.market.Value
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleParamSelectModel
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import timber.log.Timber

class VehicleParamSelectAdapter(
        val mData: MutableList<VehicleParamSelectModel>,
        val isCheckEnabled: Boolean = false
) : RecyclerView.Adapter<VehicleParamSelectAdapter.ViewHolder>() {

    private var checkedPos: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vehicle_param_select, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = mData.size

    fun replaceData(list: List<Value>){
        val res = mutableListOf<VehicleParamSelectModel>()
        list.forEach {
            res.add(VehicleParamSelectModel(it))
        }
        mData.clear()
        mData.addAll(res)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mData[position], position)
    }

    private fun setChecked(pos: Int){
        if (checkedPos == -1 ) {
            checkedPos = pos
            return
        }
        Timber.d("checked pos = $checkedPos, pos = $pos")
        mData[checkedPos].isChecked = false
        notifyItemChanged(checkedPos)
        checkedPos = pos
    }


    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val container = v.findViewById<ConstraintLayout>(R.id.cl_vehicle_param_select_container)
        val text = v.findViewById<TextView>(R.id.tv_vehicle_param_select)
        val checkImg = v.findViewById<ImageView>(R.id.iv_vehicle_color_checked)


        lateinit var param: VehicleParamSelectModel
        private var pos = -1


        fun bind(model: VehicleParamSelectModel, pos: Int) {
            this.pos = pos
            param = model
            text.text = param.value.name

            if (param.isChecked)
                checkImg.visible()
            else checkImg.gone()

            container.setOnClickListener {
                if (isCheckEnabled && !param.isChecked) {
                    checkImg.visible()
                    param.isChecked = true
                    setChecked(this.pos)
                }
            }
        }
    }
}