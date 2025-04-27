package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.numplates.nomera3.R
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible

class BottomItemWithDescription @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private var view: View

    private var ivBottomMenuIcon: ImageView? = null
    private var tvBottomMenuTitle: TextView? = null
    private var tvBottomMenuDescription: TextView? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_bottom_menu_with_description, this)
        tvBottomMenuTitle = view.findViewById(R.id.tv_bottom_menu_title)
        ivBottomMenuIcon = view.findViewById(R.id.iv_bottom_menu_icon)
        tvBottomMenuDescription = view.findViewById(R.id.tv_bottom_menu_description)
    }

    fun setTitle(title: String) {
        val bottomItemTitle = tvBottomMenuTitle ?: return
        bottomItemTitle.text = title
        bottomItemTitle.visible()
    }

    fun setIcon(icon: Any) {
        val bottomItemIcon = ivBottomMenuIcon ?: return
        bottomItemIcon.loadGlide(icon)
        bottomItemIcon.visible()
    }

    fun setDescription(description: String) {
        val bottomItemDescription = tvBottomMenuDescription ?: return
        bottomItemDescription.text = description
        bottomItemDescription.visible()
    }
}