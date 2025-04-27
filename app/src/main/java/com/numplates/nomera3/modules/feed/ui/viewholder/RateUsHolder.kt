package com.numplates.nomera3.modules.feed.ui.viewholder

import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.rateus.presentation.RatingPage

private const val CLEAR_STATE_DELAY = 1500L

class RateUsHolder(
    view: View,
    private val callback: PostCallback?
) : RecyclerView.ViewHolder(view), IDividedPost {
    private val close: ImageView = view.findViewById(R.id.iv_item_close)
    private val ratingBar: RatingBar = view.findViewById(R.id.rb_rate_us_rating_bar_item)
    private val tilText: TextInputLayout = view.findViewById(R.id.til_textInputLayout_item)
    private val continueButtonTitle: TextView = view.findViewById(R.id.tv_continue_btn)
    private val containerButton: CardView = view.findViewById(R.id.cv_vehicle_continue)
    private val rateUsImage: ImageView = view.findViewById(R.id.iv_rate_us_image_item)
    private val titleText: TextView = view.findViewById(R.id.tv_title_item)
    private val descriptionText: TextView = view.findViewById(R.id.tv_desc_item)
    private val commentTextInput: TextInputEditText = view.findViewById(R.id.tet_comment_item)

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
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            this.rating = rating.toInt()
            val isContinueButtonEnable = this.rating > 0

            proceedRatingChange()
            setEnabledContinueBtn(isContinueButtonEnable)
        }

        close.setThrottledClickListener {
            proceedCloseAnalytic()
            callback?.onHideRateUsPostClicked(bindingAdapterPosition)
            Handler().postDelayed({
                clearState()
            }, CLEAR_STATE_DELAY)
        }

        continueButtonTitle.setThrottledClickListener {
            proceedFirstAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
            showCommentFields()
        }
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
            RatingPage.SECOND ->{
                amplitudeRatingChange = rating != ratingFirstStep
            }
            else -> Unit
        }
    }

    private fun showCommentFields() {
        ratingPage = RatingPage.SECOND

        rateUsImage.gone()
        tilText.visible()
        continueButtonTitle.setThrottledClickListener {
            proceedSecondAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
            callback?.onRateUsClicked(rating, commentTextInput.text.toString(), bindingAdapterPosition)
            showMarketFields()
        }
    }

    private fun showMarketFields() {
        ratingPage = RatingPage.THIRD

        tilText.gone()
        ratingBar.gone()
        titleText.text = titleText.context.getString(R.string.rate_us_at_google)
        descriptionText.text = descriptionText.context.getString(R.string.ready_to_rate_desc)
        continueButtonTitle.text = continueButtonTitle.context.getString(R.string.rate_not_caps)
        continueButtonTitle.setThrottledClickListener {
            proceedThirdAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
            callback?.onRateUsGoToGoogleMarketClicked()
            callback?.onHideRateUsPostClicked(bindingAdapterPosition)
            Handler().postDelayed({
                clearState()
            }, CLEAR_STATE_DELAY)
        }
    }

    private fun setEnabledContinueBtn(enabled: Boolean) {
        isButtonEnabled = enabled
        if (enabled) {
            continueButtonTitle.isClickable = true
            continueButtonTitle.isFocusable = true
            continueButtonTitle.background = continueButtonTitle.context.getDrawable(R.drawable.btnviolet)
            containerButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    continueButtonTitle.context,
                    R.color.colorPrimary
                )
            )
        } else {
            continueButtonTitle.isClickable = false
            continueButtonTitle.isFocusable = false
            continueButtonTitle.background = continueButtonTitle.context.getDrawable(R.drawable.btngray)
            containerButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    continueButtonTitle.context,
                    R.color.ui_gray
                )
            )

        }
    }

    private fun clearState() {
        rateUsImage.visible()
        setEnabledContinueBtn(false)
        ratingBar.visible()
        ratingBar.rating = 0f
        titleText.text = titleText.context.getString(R.string.do_you_like_nomera)
        descriptionText.text = descriptionText.context.getString(R.string.rate_us_message)
        commentTextInput.setText("")
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
                rawReviewText = commentTextInput.text.toString(),
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
