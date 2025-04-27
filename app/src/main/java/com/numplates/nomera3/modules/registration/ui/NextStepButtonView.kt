package com.numplates.nomera3.modules.registration.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.setTint

class NextStepButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val card: CardView
    private val arrowIcon: AppCompatImageView
    private val continueText: AppCompatTextView
    private val progressBar: LottieAnimationView


    private var isTextVisible = false

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_registration_next_step_button, this, true)
        card = findViewById(R.id.continueButtonCard)
        arrowIcon = findViewById(R.id.ivContinueArrow)
        continueText = findViewById(R.id.tvContinueText)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            card.elevation = resources.getDimension(R.dimen.cardview_default_elevation)
            alpha = CONTINUE_BUTTON_ALPHA_ENABLED
            arrowIcon.setTint(R.color.ui_purple)
        } else {
            card.elevation = CONTINUE_BUTTON_ELEVATION_DISABLED
            alpha = CONTINUE_BUTTON_ALPHA_DISABLED
            arrowIcon.setTint(R.color.ui_gray)
        }
        setClick(enabled)
    }

    fun setFinishButton() {
        card.radius = FINISH_CORNER_RADIUS.dp
        arrowIcon.gone()
        continueText.visible()
        isTextVisible = true
    }

    fun showProgress(inProgress: Boolean) {
        if (inProgress) showProgress()
        else hideProgress()
    }

    private fun showProgress() {
        isClickable = false
        setClick(false)
        if (isTextVisible) continueText.invisible()
        else arrowIcon.invisible()
        progressBar.visible()
    }

    private fun hideProgress() {
        isClickable = true
        setClick(true)
        if (isTextVisible) continueText.visible()
        else arrowIcon.visible()
        progressBar.gone()
    }

    private fun setClick(isClickable: Boolean) {
        if (isClickable) {
            card.click {
                this.clickAnimateScaleUp()
                performClick()
            }
        } else {
            card.click { }
        }
    }

    companion object {
        const val CONTINUE_BUTTON_ELEVATION_DISABLED = 0F
        const val CONTINUE_BUTTON_ALPHA_DISABLED = .6F
        const val CONTINUE_BUTTON_ALPHA_ENABLED = 1F
        const val FINISH_CORNER_RADIUS = 6F
    }
}