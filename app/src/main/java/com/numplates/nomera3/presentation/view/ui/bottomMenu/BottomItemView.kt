package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.numplates.nomera3.R
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible

@Deprecated("Transited to CORE. This class must delete")
open class BottomItemView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private var view: View

    private var ivArrowBack: ImageView? = null
    private var tvBottomMenuHeader: TextView? = null
    private var ivBottomMenuIcon: AppCompatImageView? = null
    private var tvBottomMenuTitle: TextView? = null
    private var switchBottomMenu: SwitchCompat? = null
    private var tvNewItem: TextView? = null
    private var vTopSeparator: View? = null
    private var vBottomSeparator: View? = null
    private var ivArrow: ImageView? = null
    private var rbBottomMenuButton: RadioButton? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_bottom_menu, this)
        ivArrowBack = view.findViewById(R.id.iv_bottom_menu_arrow_back)
        tvBottomMenuHeader = view.findViewById(R.id.tv_bottom_menu_header)
        vTopSeparator = view.findViewById(R.id.v_top_separator)
        vBottomSeparator = view.findViewById(R.id.v_bottom_separator)
        tvNewItem = view.findViewById(R.id.tv_new_item)
        tvBottomMenuTitle = view.findViewById(R.id.tv_bottom_menu_title)
        ivBottomMenuIcon = view.findViewById(R.id.iv_bottom_menu_icon)
        switchBottomMenu = view.findViewById(R.id.switch_bottom_menu)
        ivArrow = view.findViewById(R.id.iv_bottom_menu_arrow)
        rbBottomMenuButton = view.findViewById(R.id.rg_bottom_menu_button)
    }

    fun createNewItemLabel() = tvNewItem?.visible()

    fun setHeader(header: String) {
        val arrowBack = ivArrowBack ?: return
        val bottomMenuHeader = tvBottomMenuHeader ?: return
        arrowBack.visible()
        bottomMenuHeader.text = header
        bottomMenuHeader.visible()
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

    fun setIconColor(@ColorRes color: Int) {
        ivBottomMenuIcon?.setColorFilter(
            ContextCompat.getColor(context, color),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    fun setTitleColor(colorHexCode: String) {
        Color.parseColor(colorHexCode).also {
            tvBottomMenuTitle?.setTextColor(it)
        }
    }

    fun createSwitchButton(title: String): SwitchCompat? {
        val bottomItemSwitch = switchBottomMenu ?: return null
        bottomItemSwitch.text = title
        bottomItemSwitch.visible()
        return bottomItemSwitch
    }

    fun showTopSeparator() = vTopSeparator?.visible()

    fun showBottomSeparator() = vBottomSeparator?.visible()

    fun showArrow() = ivArrow?.visible()

    fun setRadioButtonChecked(tag: String? = null) {
        val bottomMenuButton = rbBottomMenuButton ?: return
        bottomMenuButton.isVisible = bottomMenuButton.tag == tag
    }

    fun showRadioButton(isSelected: Boolean, tag: String? = null) {
        val bottomMenuButton = rbBottomMenuButton ?: return
        bottomMenuButton.isChecked = true
        bottomMenuButton.isVisible = isSelected
        tag?.let { bottomMenuButton.tag = it }
    }
}
