package com.numplates.nomera3.presentation.view.ui.customView

import android.content.Context
import android.content.res.ColorStateList
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.numplates.nomera3.R

class CustomCheckBox @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private val textView: TextView
    private var checkedChangeListener: ((CustomCheckBox, Boolean) -> Unit)? = null

    var isChecked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                imageView.isSelected = field
                textView.isSelected = field
                checkedChangeListener?.invoke(this, field)
            }
        }

    init {
        LayoutInflater
                .from(context)
                .inflate(R.layout.custom_checkbox, this, true)

        imageView = findViewById(R.id.checkbox_image)
        textView = findViewById(R.id.checkbox_text)
        textView.id = View.generateViewId()

        context.withStyledAttributes(attrs, R.styleable.CustomCheckBox) {
            setText(getText(R.styleable.CustomCheckBox_buttonText))

            if (hasValue(R.styleable.CustomCheckBox_textColor)) {
                getColorStateList(R.styleable.CustomCheckBox_textColor)?.let {
                    setTextColor(it)
                }
            }

            if (hasValue(R.styleable.CustomCheckBox_textColorLink)) {
                getColorStateList(R.styleable.CustomCheckBox_textColorLink)?.let {
                    setLinkTextColor(it)
                }
            }

            setImageResource(
                    getResourceId(
                            R.styleable.CustomCheckBox_boxImage,
                            R.drawable.custom_checkbox_selector
                    )
            )
        }
    }

    fun setText(text: CharSequence?) {
        textView.text = text
    }

    fun setTextColor(colors: ColorStateList) {
        textView.setTextColor(colors)
    }

    fun setLinkTextColor(colors: ColorStateList) {
        textView.setLinkTextColor(colors)
    }

    fun setImageResource(@DrawableRes resId: Int) {
        imageView.setImageResource(resId)
    }

    fun setMovementMethod(movement: MovementMethod?) {
        textView.movementMethod = movement
    }

    fun setOnCheckedChangeListener(checkedChangeListener: (CustomCheckBox, Boolean) -> Unit) {
        this.checkedChangeListener = checkedChangeListener
    }
}