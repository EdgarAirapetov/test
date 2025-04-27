package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.numplates.nomera3.R

class VideoRetryView : ConstraintLayout{
    constructor(context: Context): super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init(){
        inflate(context, R.layout.layout_retry_player_menu, this)
    }
}
