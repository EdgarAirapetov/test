package com.noomeera.nmravatarssdk.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.noomeera.nmravatarssdk.R
import com.noomeera.nmravatarssdk.extensions.dp

class CustomSwitch : ConstraintLayout {

    private val view: View = LayoutInflater.from(context).inflate(R.layout.view_custom_switch, this)
    private val switchView = view.findViewById<View>(R.id.switchView)
    private val ivMaleIcon = view.findViewById<AppCompatImageView>(R.id.vMaleIcon)
    private val ivFemaleIcon = view.findViewById<AppCompatImageView>(R.id.vFemaleIcon)
    private val vFemale = view.findViewById<View>(R.id.vFemale)
    private val vMale = view.findViewById<View>(R.id.vMale)
    private var isMale = true
    var checkedChangeListener: CustomSwitchCheckedChangeListener? = null

    init {
        init()
    }

    fun init() {

        vFemale.setOnClickListener {
            if (isMale) {
                setFemaleAction()
            }
        }

        vMale.setOnClickListener {
            if (isMale.not()) {
                setMaleAction()
            }
        }

    }

    private fun setMaleAction() {
        switchView.animate().translationY(0f).setDuration(100).start()
        ivMaleIcon.setImageResource(R.drawable.ic_male_selected)
        ivFemaleIcon.setImageResource(R.drawable.ic_female)
        isMale = true
        checkedChangeListener?.isMale(isMale)

    }

    private fun setFemaleAction() {
        switchView.animate().translationY(56.dp.toFloat()).setDuration(100).start()
        ivMaleIcon.setImageResource(R.drawable.ic_male)
        ivFemaleIcon.setImageResource(R.drawable.ic_female_selected)
        isMale = false
        checkedChangeListener?.isMale(isMale)
    }

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init()}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun isMan() = isMale

    fun setMale () {
        setMaleAction()
    }

    fun setFemale() {
        setFemaleAction()
    }


    interface CustomSwitchCheckedChangeListener {
        fun isMale(isMale: Boolean)
    }
}