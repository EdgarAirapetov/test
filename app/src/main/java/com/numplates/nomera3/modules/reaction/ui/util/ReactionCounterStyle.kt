package com.numplates.nomera3.modules.reaction.ui.util

import androidx.annotation.StyleRes
import com.numplates.nomera3.R

enum class ReactionCounterStyle(@StyleRes val style: Int) {
    Init(R.style.ReactionCounterOther),
    Other(R.style.ReactionCounterOther),
    Mine(R.style.ReactionCounterMine)
}