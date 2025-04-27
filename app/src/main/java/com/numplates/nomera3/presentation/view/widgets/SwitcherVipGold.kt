package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import timber.log.Timber

class SwitcherVipGold: ConstraintLayout {

    private lateinit var cardVip: CardView
    private lateinit var cardGold: CardView
    private lateinit var cardBack: CardView
    private lateinit var textGold: TextView
    private lateinit var textVip: TextView

    var clickListener: (Int) -> Unit =  { _ -> }

    private var isVipClicked = true

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?){
        val v = inflate(context, R.layout.layout_silver_gold, this)
        initView(v)
        initClickListeners()
        setUpVip()
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SwitcherVipGold,
                0, 0)
        val radius = typedArray.getDimension(R.styleable.SwitcherVipGold_cornerRadius, -1f)
        if (radius != -1f){
            cardBack.radius = radius
            cardGold.radius = radius
            cardVip.radius = radius
        }else {
            v.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                setupCorners()
            }
        }
    }

    private fun setupCorners() {
        val radius = (height / 2).toFloat()
        Timber.d("corner radius = $radius")
        cardBack.radius = radius
        cardGold.radius = radius
        cardVip.radius = radius
    }

    private fun initClickListeners() {
        cardVip.setOnClickListener {
            if(isVipClicked)
                return@setOnClickListener
            isVipClicked = true
            setUpVip()
            clickListener.invoke(TYPE_VIP)
        }

        cardGold.setOnClickListener {
            if (!isVipClicked){
                return@setOnClickListener
            }
            isVipClicked = false
            setUpGold()
            clickListener.invoke(TYPE_GOLD)
        }
    }

    fun switch(type: Int) {
        when (type) {
            TYPE_VIP -> setUpVip()
            TYPE_GOLD -> setUpGold()
        }
    }

    private fun setUpGold() {
        Timber.d("Setup Gold called")
        cardGold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ui_yellow))
        cardVip.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))
        cardVip.cardElevation = 0f
        cardGold.cardElevation = dpToPx(8).toFloat()
        cardBack.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorBlack))
        textVip.setTextColor(ContextCompat.getColor(context, R.color.ui_white_50))
        textGold.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
    }

    private fun setUpVip() {
        Timber.d("Setup VIP called")
        cardGold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))
        cardVip.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ui_white))
        cardGold.cardElevation = 0f
        cardVip.cardElevation = dpToPx(8).toFloat()
        cardBack.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_switcher))
        textVip.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        textGold.setTextColor(ContextCompat.getColor(context, R.color.ui_black_50))
    }

    private fun initView(v: View) {
        cardVip = v.findViewById(R.id.cv_silver_gold_left)
        cardGold = v.findViewById(R.id.cv_silver_gold_right)
        cardBack = v.findViewById(R.id.cv_silver_gold_back)
        textVip = v.findViewById(R.id.tv_vip_switcher)
        textGold = v.findViewById(R.id.tv_gold_switcher)

    }

    companion object{
        const val TYPE_VIP = 0
        const val TYPE_GOLD = 1
    }
}
