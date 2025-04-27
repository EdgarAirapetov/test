package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.numplates.nomera3.R
import com.meera.core.extensions.visible

class BottomItemTitleView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private var view: View

    private var tvReactionsStatisticTitle: TextView? = null
    private var tvReactionsStatisticTitleLabel: TextView? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_reactions_statistic_title, this)
        tvReactionsStatisticTitle = view.findViewById(R.id.tv_reactions_statistic_title)
        tvReactionsStatisticTitleLabel = view.findViewById(R.id.tv_reactions_statistic_title_label)
    }

    fun setTitle(title: String) {
        val reactionsStatisticTitle = tvReactionsStatisticTitle ?: return
        reactionsStatisticTitle.text = title
        reactionsStatisticTitle.visible()
    }

    fun setReactionsNumber(label: String?) {
        val incomingLabel = label ?: return
        val reactionsStatisticLabel = tvReactionsStatisticTitleLabel ?: return
        reactionsStatisticLabel.text = incomingLabel
        reactionsStatisticLabel.visible()
    }

    fun setTitleColor(colorHexCode: String) {
        Color.parseColor(colorHexCode).also {
            tvReactionsStatisticTitle?.setTextColor(it)
        }
    }

    fun setLabelColor(colorHexCode: String) {
        Color.parseColor(colorHexCode).also {
            tvReactionsStatisticTitleLabel?.setTextColor(it)
        }
    }
}
