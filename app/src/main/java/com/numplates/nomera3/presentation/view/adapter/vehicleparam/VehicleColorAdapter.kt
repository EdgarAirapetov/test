package com.numplates.nomera3.presentation.view.adapter.vehicleparam

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.market.Value
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleColorModel
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import timber.log.Timber
import kotlin.math.min
import kotlin.math.roundToInt

class VehicleColorAdapter(
        val mData: MutableList<VehicleColorModel>
) : RecyclerView.Adapter<VehicleColorAdapter.ViewHolder>() {

    private var selectedItemPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vehicle_color, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mData[position], position)
    }

    fun replaceData(list: List<Value>){
        val res = mutableListOf<VehicleColorModel>()
        list.forEach {
            res.add(VehicleColorModel(Color.parseColor("#${it.value}"), it.name, it))
        }
        mData.clear()
        mData.addAll(res)
        notifyDataSetChanged()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private lateinit var colorModel: VehicleColorModel
        private var pos = 0
        val container: ConstraintLayout = v.findViewById(R.id.cl_vehicle_color_container)
        private val colorCard: CardView = v.findViewById(R.id.cv_color_vehicle)
        private val innerCircle: CardView = v.findViewById(R.id.cv_inner_circle)
        private val colorName: TextView = v.findViewById(R.id.tv_color_item_name)
        val check: ImageView = v.findViewById(R.id.iv_vehicle_color_checked)

        init {
            colorCard.setOnClickListener {
                mData[selectedItemPosition].isSelected = false
                notifyItemChanged(selectedItemPosition)
                selectedItemPosition = pos
                colorModel.isSelected = true
                check.visible()
                innerCircle.visibleAppearAnimate()
                innerCircle.setCardBackgroundColor(manipulateColor(colorModel.color))
            }
        }

        fun bind(model: VehicleColorModel, position: Int) {
            colorModel = model
            pos = position
            colorName.text = colorModel.colorName
            colorCard.setCardBackgroundColor(model.color)
            if (colorModel.isSelected) {
                check.visible()
                innerCircle.visible()
                innerCircle.setCardBackgroundColor(manipulateColor(model.color))
            } else {
                check.invisible()
                innerCircle.invisible()
            }
        }

        private fun manipulateColor(color: Int): Int { //немного затемняет цвет..
            val factor = 0.8F
            val a = Color.alpha(color)
            val r = (Color.red(color) * factor).roundToInt()
            val g = (Color.green(color) * factor).roundToInt()
            val b = (Color.blue(color) * factor).roundToInt()
            val res = Color.argb(a,
                    min(r, 255),
                    min(g, 255),
                    min(b, 255))
            Timber.d("Start color = $color End color = $res")
            return res
        }
    }
}