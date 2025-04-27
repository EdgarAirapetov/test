package com.numplates.nomera3.presentation.view.ui.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.numplates.nomera3.R
import com.meera.core.extensions.visible
import com.meera.core.extensions.invisible

class TextViewWithArrow @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var root: View
    var actionButton: View
    var hintText: TextView
    var errorText: TextView
    var headerText: TextView
    var contentText: TextView
    var contentContainer: FrameLayout

    init {
        LayoutInflater
                .from(context)
                .inflate(R.layout.action_edit_text, this, true)

        headerText = findViewById(R.id.header_text)
        errorText = findViewById(R.id.error_text)
        hintText = findViewById(R.id.hint_text)
        contentText = findViewById(R.id.content_text)
        contentContainer = findViewById(R.id.content_container)
        actionButton = findViewById(R.id.arrow)
        root = findViewById(R.id.root)

        context.withStyledAttributes(attrs, R.styleable.TextViewWithArrow) {
            if (hasValue(R.styleable.BaseProfileEditText_headerText)) {
                setHeader(getText(R.styleable.BaseProfileEditText_headerText).toString())
            }

            if (hasValue(R.styleable.BaseProfileEditText_hintText)) {
                setHint(getText(R.styleable.BaseProfileEditText_hintText).toString())
            }
        }
    }


    fun resetView() {
        headerText.visible()
        hintText.visible()

        errorText.invisible()
        contentText.invisible()

        contentContainer.background = ContextCompat.getDrawable(context, R.drawable.background_profile_custom_edittext)
    }

    fun setError(text: String) {
        headerText.invisible()
        errorText.visible()
        errorText.text = text

        contentContainer.background = ContextCompat.getDrawable(context, R.drawable.background_invalid_nickname_edit_text)
    }

    fun setHeader(text: String) {
        headerText.text = text

        headerText.visible()
        errorText.invisible()
    }

    fun setHint(text: String) {
        hintText.text = text

        hintText.visible()
        contentText.invisible()
    }

    fun setText(text: String) {
        contentText.text = text

        hintText.invisible()
        contentText.visible()
    }

    fun setArrowClickListener(arrowClickListener: () -> Unit) {
        actionButton.setOnClickListener {
            arrowClickListener.invoke()
        }
    }
}

