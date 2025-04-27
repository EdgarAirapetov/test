package com.meera.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.meera.core.R

class BottomItemProgressView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private var view: View
    private var tvProgress: TextView? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_progress_bottom_menu, this)
        tvProgress = view.findViewById(R.id.tv_bottom_menu_progress_text)
        setProgress(0)
    }

    fun setProgress(progress: Int) {
        tvProgress?.text = context.getString(R.string.general_progress_percent, progress)
    }

}
