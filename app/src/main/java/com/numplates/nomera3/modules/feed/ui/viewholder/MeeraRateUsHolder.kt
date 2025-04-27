package com.numplates.nomera3.modules.feed.ui.viewholder

import android.animation.ValueAnimator
import android.os.Handler
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.clearText
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.UiKitCellInput
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.rateus.presentation.RatingPage
import com.numplates.nomera3.presentation.view.fragments.meerasettings.RateUsStarsView

private const val CLEAR_STATE_DELAY = 1500L
private const val MAX_CHARS_COUNT = 240
private const val CHARS_TO_VISIBLE_LAST_COUNT = 231
private const val VIEWS_ANIMATION_DURATION = 200L

class MeeraRateUsHolder(
    view: View,
    private val callback: MeeraPostCallback?
) : RecyclerView.ViewHolder(view), IDividedPost {
    private val close: FrameLayout = view.findViewById(R.id.iv_item_close)
    private val cancelButton: UiKitButton = view.findViewById(R.id.btn_cancel_rate)
    private val continueButton: UiKitButton = view.findViewById(R.id.btn_continue_rate)
    private val ratingBar: RateUsStarsView = view.findViewById(R.id.rb_rate_us_rating_bar_item)
    private val rateUsImage: ImageView = view.findViewById(R.id.iv_rate_us_image_item)
    private val titleText: TextView = view.findViewById(R.id.tv_title_item)
    private val descriptionText: TextView = view.findViewById(R.id.tv_desc_item)
    private val lastCharsText: TextView = view.findViewById(R.id.tv_last_chars_count)
    private val commentTextInput: UiKitCellInput = view.findViewById(R.id.input_comment_item)
    private val rateUsTexts: LinearLayout = view.findViewById(R.id.ll_rate_us_texts)

    private val commentInputHeight = 96.dp
    private var rateUsTextsHeight = 0

    private var rating = 0
    private var ratingFirstStep = 0

    private var isButtonEnabled = false
    private var post: PostUIEntity? = null

    private var ratingPage: RatingPage = RatingPage.FIRST
    private var amplitudeRatingChange: Boolean = false

    fun bind(post: PostUIEntity) {
        this.post = post

        initListeners()
        setEnabledContinueBtn(isButtonEnabled)
    }

    override fun isVip(): Boolean = false

    private fun initListeners() {
        commentTextInput.etInput.addTextChangedListener { text ->
            initLastCountInput(text)
        }
        commentTextInput.etInput.visible()

        val isRatingSet = ratingBar.rating > 0
        commentTextInput.isVisible = isRatingSet
        rateUsTexts.isVisible = !isRatingSet

        if (!isRatingSet && rateUsTextsHeight != 0) {
            val layoutParams: ViewGroup.LayoutParams = rateUsTexts.layoutParams
            layoutParams.height = rateUsTextsHeight
            rateUsTexts.setLayoutParams(layoutParams)
        }

        if (ratingPage == RatingPage.THIRD) showMarketFields()

        ratingBar.setStarsListener { rate ->
            if (this.rating == 0) proceedFirstAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)

            this.rating = rate.rate
            val isContinueButtonEnable = this.rating > 0

            proceedRatingChange()
            showCommentFields()
            animateHideTexts()
            setEnabledContinueBtn(isContinueButtonEnable)
        }

        close.setThrottledClickListener {
            cancelRate()
        }

        cancelButton.setThrottledClickListener {
            cancelRate()
        }
    }

    private fun initLastCountInput(text: Editable?) {
        val charsCount = text.toString().count()
        if (charsCount >= CHARS_TO_VISIBLE_LAST_COUNT) {
            lastCharsText.visible()
            val lastCount = MAX_CHARS_COUNT - charsCount
            lastCharsText.text = lastCharsText.context.pluralString(
                R.plurals.meera_rate_us_char_reminder, lastCount
            )
        } else {
            lastCharsText.clearText()
            lastCharsText.gone()
        }
    }

    private fun cancelRate() {
        proceedCloseAnalytic()
        callback?.onHideRateUsPostClicked(bindingAdapterPosition)
        Handler().postDelayed({
            clearState()
        }, CLEAR_STATE_DELAY)
    }

    private fun proceedCloseAnalytic() {
        when (ratingPage) {
            RatingPage.FIRST -> proceedFirstAnalytic(actionType = AmplitudePropertyRatingActionType.CLOSE)
            RatingPage.SECOND -> proceedSecondAnalytic(actionType = AmplitudePropertyRatingActionType.CLOSE)
            RatingPage.THIRD -> proceedThirdAnalytic(actionType = AmplitudePropertyRatingActionType.CLOSE)
        }
    }

    private fun proceedRatingChange() {
        when (ratingPage) {
            RatingPage.FIRST -> {
                ratingFirstStep = rating
            }

            RatingPage.SECOND -> {
                amplitudeRatingChange = rating != ratingFirstStep
            }

            else -> Unit
        }
    }

    private fun showCommentFields() {
        if (commentTextInput.isVisible) return
        ratingPage = RatingPage.SECOND
        commentTextInput.visible()
        animateShowCommentInput()
        continueButton.setThrottledClickListener {
            proceedSecondAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
            callback?.onRateUsClicked(rating, commentTextInput.etInput.text.toString(), bindingAdapterPosition)
            showMarketFields()
        }
    }

    private fun animateShowCommentInput() {
        ValueAnimator.ofInt(0, commentInputHeight).apply {
            addUpdateListener { valueAnimator ->
                val currentHeight = valueAnimator.animatedValue as Int
                val layoutParams: ViewGroup.LayoutParams = commentTextInput.layoutParams
                layoutParams.height = currentHeight
                commentTextInput.setLayoutParams(layoutParams)
            }
            setDuration(VIEWS_ANIMATION_DURATION)
            start()
        }
    }

    private fun animateHideTexts() {
        if (rateUsTexts.isGone) return
        rateUsTextsHeight = rateUsTexts.measuredHeight
        ValueAnimator.ofInt(rateUsTextsHeight, 0).apply {
            addUpdateListener { valueAnimator ->
                val currentHeight = valueAnimator.animatedValue as Int
                val layoutParams: ViewGroup.LayoutParams = rateUsTexts.layoutParams
                layoutParams.height = currentHeight
                rateUsTexts.setLayoutParams(layoutParams)
                if (currentHeight == 0) {
                    rateUsTexts.gone()
                }
            }
            setDuration(VIEWS_ANIMATION_DURATION)
            start()
        }
    }

    private fun showMarketFields() {
        ratingPage = RatingPage.THIRD

        commentTextInput.gone()
        lastCharsText.gone()
        rateUsImage.setImageResource(R.drawable.meera_rate_us_google_play)
        ratingBar.gone()
        cancelButton.visible()
        titleText.text = titleText.context.getString(R.string.meera_rate_us_goole_title)
        descriptionText.text = descriptionText.context.getString(R.string.meera_rate_us_google_description)
        continueButton.text = continueButton.context.getString(R.string.meera_rate_us_google_move_to_google_btn)

        rateUsTexts.visible()

        continueButton.setThrottledClickListener {
            proceedThirdAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
            callback?.onRateUsGoToGoogleMarketClicked()
            callback?.onHideRateUsPostClicked(bindingAdapterPosition)
            Handler().postDelayed({
                clearState()
            }, CLEAR_STATE_DELAY)
        }
    }

    private fun setEnabledContinueBtn(enabled: Boolean) {
        continueButton.isEnabled = enabled
        isButtonEnabled = enabled
    }

    private fun clearState() {
        rateUsImage.setImageResource(R.drawable.meera_rate_us_main_image)
        rateUsImage.visible()
        cancelButton.gone()
        setEnabledContinueBtn(false)
        ratingBar.visible()
        ratingBar.skipStars()
        titleText.text = titleText.context.getString(R.string.do_you_like_nomera)
        descriptionText.text = descriptionText.context.getString(R.string.rate_us_message)
        commentTextInput.etInput.setText("")
    }

    private fun proceedFirstAnalytic(actionType: AmplitudePropertyRatingActionType) {
        callback?.onRateUsProcessAnalytic(
            RateUsAnalyticsRating.First(
                rating = rating,
                amplitudeWhere = AmplitudePropertyRatingWhere.FEED,
                actionType = actionType
            )
        )
    }

    private fun proceedSecondAnalytic(actionType: AmplitudePropertyRatingActionType) {
        callback?.onRateUsProcessAnalytic(
            RateUsAnalyticsRating.Second(
                rating = rating,
                rawReviewText = commentTextInput.etInput.text.toString(),
                amplitudeRatingChange = amplitudeRatingChange,
                amplitudeWhere = AmplitudePropertyRatingWhere.FEED,
                actionType = actionType
            )
        )
    }

    private fun proceedThirdAnalytic(actionType: AmplitudePropertyRatingActionType) {
        callback?.onRateUsProcessAnalytic(
            RateUsAnalyticsRating.Third(
                amplitudeWhere = AmplitudePropertyRatingWhere.FEED,
                actionType = actionType
            )
        )
    }
}
