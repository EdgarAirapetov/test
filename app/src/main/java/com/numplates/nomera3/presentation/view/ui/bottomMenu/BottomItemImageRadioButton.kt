package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.numplates.nomera3.R
import com.meera.core.extensions.gone
import com.meera.core.extensions.string
import com.meera.core.extensions.visible

class BottomItemImageRadioButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr) {

    private var view: View

    private var ivImageRadioButtonIcon: ImageView? = null
    private var tvImageRadioButtonTitle: TextView? = null
    private var tvImageRadioButtonDescription: TextView? = null
    private var rbImageRadioButton: RadioButton? = null
    private var vImageRadioButtonSeparator: View? = null

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_bottom_image_radio_button, this)
        ivImageRadioButtonIcon = view.findViewById(R.id.iv_image_radio_button_icon)
        tvImageRadioButtonTitle = view.findViewById(R.id.tv_image_radio_button_title)
        tvImageRadioButtonDescription = view.findViewById(R.id.tv_image_radio_button_description)
        rbImageRadioButton = view.findViewById(R.id.rb_image_radio_button)
        vImageRadioButtonSeparator = view.findViewById(R.id.v_image_radio_button_separator)
        this.setOnClickListener {
            setChecked(true)
        }
    }

    fun setTitle(@StringRes title: Int) {
        val radioButtonTitle = tvImageRadioButtonTitle ?: return
        radioButtonTitle.text = context.string(title)
    }

    fun setDescription(@StringRes description: Int) {
        val radioButtonDescription = tvImageRadioButtonDescription ?: return
        radioButtonDescription.text = context.string(description)
    }

    fun setIcon(@DrawableRes icon: Int) {
        val radioButtonIcon = ivImageRadioButtonIcon ?: return
        radioButtonIcon.setImageResource(icon)
    }

    fun setChecked(checked: Boolean) {
        val radioButton = rbImageRadioButton ?: return
        val radioButtonTitle = tvImageRadioButtonTitle ?: return
        radioButton.isChecked = checked
        if (checked) {
            radioButtonTitle.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
        } else {
            radioButtonTitle.setTextColor(Color.BLACK)
        }
        onCheckedChangeListener?.onCheckedChanged(checked)
    }

    fun setSeparatorVisibility(visible: Boolean) {
        val radioButtonSeparator = vImageRadioButtonSeparator ?: return
        if (visible) {
            radioButtonSeparator.visible()
        } else {
            radioButtonSeparator.gone()
        }
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(isChecked: Boolean)
    }
}