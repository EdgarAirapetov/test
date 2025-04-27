package com.numplates.nomera3.presentation.view.ui.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.meera.core.extensions.clearText
import com.numplates.nomera3.R
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible

abstract class BaseProfileEditText @JvmOverloads constructor(
        context: Context,
        @LayoutRes layoutId: Int,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var headerText: TextView
    var errorText: TextView
    var editText: AppCompatEditText
    var mainContainer: LinearLayout? = null

    init {
        LayoutInflater
                .from(context)
                .inflate(layoutId, this, true)

        headerText = findViewById(R.id.header_text)
        errorText = findViewById(R.id.error_text)
        editText = findViewById(R.id.edit_text)
        mainContainer = findViewById(R.id.main_container)

        context.withStyledAttributes(attrs, R.styleable.BaseProfileEditText) {
            if (hasValue(R.styleable.BaseProfileEditText_headerText)) {
                setHeader(getText(R.styleable.BaseProfileEditText_headerText).toString())
            }

            if (hasValue(R.styleable.BaseProfileEditText_hintText)) {
                setHint(getText(R.styleable.BaseProfileEditText_hintText).toString())
            }
        }
    }

    open fun reset() {
        headerText.visible()
        errorText.invisible()
        editText.background = ContextCompat.getDrawable(context, R.drawable.background_profile_custom_edittext)
    }

    open fun setError(text: String) {
        headerText.invisible()
        errorText.visible()
        errorText.text = text
        editText.background = ContextCompat.getDrawable(context, R.drawable.background_invalid_nickname_edit_text)
    }

    fun setHeader(text: String) {
        headerText.text = text
    }

    fun setHint(text: String) {
        editText.hint = text
    }

    fun setText(text: String) {
        editText.setText(text)
    }

    fun clearText() {
        editText.clearText()
    }

    fun blockEdittext() {
        editText.isEnabled = false
    }

    fun unblockEdittext() {
        editText.isEnabled = true
    }
}
