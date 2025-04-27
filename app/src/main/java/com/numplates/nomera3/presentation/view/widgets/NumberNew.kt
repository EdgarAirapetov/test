package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dpToPx
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues


class NumberNew : ConstraintLayout {

    private lateinit var tvName: TextView
    private lateinit var tvModel: TextView
    private lateinit var clContainer: ConstraintLayout
    private lateinit var vFrame: View
    private lateinit var cvCard: CardView

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.layout_number, this)
        tvName = findViewById(R.id.tv_vehicle_name)
        tvModel = findViewById(R.id.tv_vehicle_model)
        clContainer = findViewById(R.id.cl_container_number)
        vFrame = findViewById(R.id.v_frame_view_vehicle)
        cvCard = findViewById(R.id.cv_number_card)

        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.NumberNew,
                0, 0)
        val modelTextSize = typedArray.getDimensionPixelSize(R.styleable.NumberNew_textModelSize, dpToPx(12))
        val nameTextSize = typedArray.getDimensionPixelSize(R.styleable.NumberNew_textNameSize, dpToPx(14))
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameTextSize.toFloat())
        tvModel.setTextSize(TypedValue.COMPLEX_UNIT_PX, modelTextSize.toFloat())

    }

    fun setName(name: String) {
        tvName.text = name
    }

    fun setModel(model: String) {
        tvModel.text = model
    }

    fun setType(type: Int, color: Int? = null) {
        when (type) {
            INetworkValues.ACCOUNT_TYPE_REGULAR -> {
                setUpCommon()
            }
            INetworkValues.ACCOUNT_TYPE_PREMIUM -> {
                setUpCommonColored()
                color?.let { setColorBackground(NGraphics.getColorResourceId(type, color)) }
            }
            INetworkValues.ACCOUNT_TYPE_VIP -> {
                setUpVipGold()
            }
        }
    }

    private fun setUpVipGold() {
        clContainer.setBackgroundResource(R.color.colorBlack)
        tvModel.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        tvName.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        vFrame.setBackgroundResource(R.drawable.bg_number_stroke_gold)
    }

    private fun setUpCommonColored() {
        clContainer.setBackgroundResource(R.color.colorWhite)
        tvModel.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        tvName.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        vFrame.setBackgroundResource(R.drawable.bg_number_stroke_white)
        cvCard.elevation = dpToPx(2).toFloat()
    }

    private fun setUpCommon() {
        clContainer.setBackgroundResource(R.color.colorWhite)
        tvModel.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        tvName.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        vFrame.setBackgroundResource(R.drawable.bg_number_stroke_black)
    }

    private fun setColorBackground(color: Int) {
        clContainer.setBackgroundResource(color)
    }

    fun setUpElevation(elevation: Int) {
        cvCard.cardElevation = dpToPx(elevation).toFloat()
    }

    fun setNameHint(hint: Int){
        context?.let {
            tvName.hint = it.getString(hint)
        }
    }

    fun setModelHint(hint: Int){
        context?.let {
            tvModel.hint = it.getString(hint)
        }
    }
}
