package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.NoShowOnMapPlaceholderLayoutBinding

enum class NoShowOnMapPlaceholderType {
    OWN_PROFILE, OTHER_PROFILE
}

class NoShowOnMapPlaceholder @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): LinearLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.no_show_on_map_placeholder_layout, this, true)
        .let(NoShowOnMapPlaceholderLayoutBinding::bind)

    init {
        gone()
    }

    fun setPlaceholderType(type: NoShowOnMapPlaceholderType) {
        if (type == NoShowOnMapPlaceholderType.OTHER_PROFILE) {
            configureOtherProfilePlaceholderUi()
        } else {
            configureOwnProfilePlaceholderUi()
        }
    }

    private fun configureOwnProfilePlaceholderUi() {
        binding.apply {
            tvNoShowOnMapOtherProfileDescription.gone()
            ivNoShowOnMap.setImageResource(R.drawable.no_show_on_map_placeholder)
            tvNoShowOnMapOwnProfileDescription.visible()
            btnGotoMapSettings.visible()
        }
    }

    private fun configureOtherProfilePlaceholderUi() {
        binding.apply {
            ivNoShowOnMap.setImageResource(R.drawable.meera_ic_empty_post_comments)
            tvNoShowOnMapOwnProfileDescription.gone()
            btnGotoMapSettings.gone()
            tvNoShowOnMapOtherProfileDescription.visible()
        }
    }

    fun initSettingsButton() {
        binding.btnGotoMapSettings.apply {
            post { requestLayout() }
        }
    }

    fun clickActionButton(onClick: () -> Unit) {
        binding.btnGotoMapSettings.setThrottledClickListener {
            onClick.invoke()
        }
    }

}
