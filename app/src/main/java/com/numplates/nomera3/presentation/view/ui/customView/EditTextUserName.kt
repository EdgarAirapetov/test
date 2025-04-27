package com.numplates.nomera3.presentation.view.ui.customView

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.numplates.nomera3.R
import com.meera.core.extensions.visible
import com.meera.core.extensions.invisible

class EditTextUserName @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BaseProfileEditText(context, R.layout.user_name_edit_text, attrs, defStyleAttr) {

    override fun reset() {
        headerText.visible()
        errorText.invisible()
        mainContainer?.background = ContextCompat.getDrawable(context, R.drawable.background_profile_custom_edittext)
    }

    override fun setError(text: String) {
        headerText.invisible()
        errorText.visible()
        errorText.text = text
        mainContainer?.background = ContextCompat.getDrawable(context, R.drawable.background_invalid_nickname_edit_text)
    }
}

