package com.numplates.nomera3.modules.rateus.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber

private const val DEF_RATING = 0

class PopUpRateUsDialogFragment : AppCompatDialogFragment() {

    private val viewModel: PopUpRateUsDialogViewModel by viewModels { App.component.getViewModelFactory() }

    var onRateSuccess: () -> Unit = { }
    var onRateError: () -> Unit = { }
    var onDismissListener: () -> Unit = { }

    private lateinit var dialog: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog
    private lateinit var rateBtn: TextView
    private lateinit var cancelBtn: TextView
    private lateinit var rateBar: RatingBar
    private lateinit var comment: TextInputEditText
    private lateinit var ivRate: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var textInputLayout: TextInputLayout

    private var rating: Int = DEF_RATING
    private var ratingFirstStep: Int = DEF_RATING
    private var ratingPage: RatingPage? = null
    private var isSizeConfigured = false
    private var disposables = CompositeDisposable()
    private var amplitudeWhere: AmplitudePropertyRatingWhere? = null
    private var amplitudeRatingChange: Boolean = false
    private var isRatingCompleted: Boolean = false

    init {
        App.component.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        ratingPage = RatingPage.FIRST
        dialog = AlertDialog.Builder(context)
        val mView = LayoutInflater.from(context).inflate(R.layout.dialog_pop_up_rate_us, null)
        dialog.setView(mView)
        dialog.setCancelable(false)
        initViews(mView)
        initListeners()
        alertDialog = dialog.create()
        viewModel.saveLastShow()
        return alertDialog
    }

    override fun onResume() {
        super.onResume()
        if (!isSizeConfigured) {
            val displayMetrics = DisplayMetrics()
            (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(
                displayMetrics
            )
            val displayWidth = displayMetrics.widthPixels
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(alertDialog.window?.attributes)
            val dialogWindowWidth = (displayWidth * 0.85f).toInt()
            layoutParams.width = dialogWindowWidth
            alertDialog.window?.attributes = layoutParams
        }
        isSizeConfigured = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResources()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) != null) return
        super.show(manager, tag)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isRatingCompleted) return

        when (ratingPage) {
            RatingPage.FIRST -> proceedFirstAnalytic(actionType = AmplitudePropertyRatingActionType.CLOSE)
            RatingPage.SECOND -> proceedSecondAnalytic(actionType = AmplitudePropertyRatingActionType.CLOSE)
            RatingPage.THIRD -> proceedThirdAnalytic(actionType = AmplitudePropertyRatingActionType.CLOSE)
            else -> Unit
        }
    }

    fun setAmplitudePropertyWhere(amplitudeWhere: AmplitudePropertyRatingWhere) {
        this.amplitudeWhere = amplitudeWhere
    }

    private fun clearResources() {
        disposables.dispose()
    }

    private fun initViews(v: View) {
        rateBtn = v.findViewById(R.id.pop_up_rate_us_btn)
        cancelBtn = v.findViewById(R.id.pop_up_rate_us_later)
        rateBar = v.findViewById(R.id.pop_up_rate_us_rating_bar)
        comment = v.findViewById(R.id.rate_us_comment)
        ivRate = v.findViewById(R.id.iv_rate_us_image)
        textInputLayout = v.findViewById(R.id.rate_us_textInputLayout)
        tvTitle = v.findViewById(R.id.pop_up_title)
        tvDescription = v.findViewById(R.id.pop_up_desc)
        rateBar.rating = rating.toFloat()
    }

    private fun initListeners() {
        rateBtn.setOnClickListener {
            when (ratingPage) {
                RatingPage.FIRST -> {
                    ratingPage = RatingPage.SECOND
                    ivRate.gone()
                    textInputLayout.visible()
                    tvTitle.text = getString(R.string.we_become_better)
                    rateBtn.text = getString(R.string.general_send)
                    tvDescription.text = getString(R.string.ready_to_rate_desc)
                    val lParams = tvDescription.layoutParams as ConstraintLayout.LayoutParams
                    lParams.setMargins(
                        lParams.marginStart, lParams.topMargin + 4.dp,
                        lParams.marginEnd, lParams.bottomMargin
                    )
                    proceedFirstAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
                }
                RatingPage.SECOND -> {
                    ratingPage = RatingPage.THIRD
                    viewModel.writeIsRated()
                    proceedSecondAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
                    rate()
                }
                else -> Unit
            }
        }
        cancelBtn.setOnClickListener {
            viewModel.writeIsNotRated()
            onDismissListener.invoke()
            alertDialog.dismiss()
        }
        comment.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) = clearError()

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            }
        )
        rateBar.setOnRatingBarChangeListener { _, rating, _ ->
            when (ratingPage) {
                RatingPage.FIRST -> ratingFirstStep = rating.toInt()
                RatingPage.SECOND -> amplitudeRatingChange = rating.toInt() != ratingFirstStep
                else -> Unit
            }
            this.rating = rating.toInt()
        }
    }

    private fun rate() {
        lifecycleScope.launch {
            kotlin.runCatching {
                viewModel.rateUs(rating = rating, comment = getReviewText()).join()
            }.onSuccess {
                onRateSuccess.invoke()

                hideKeyboard()
                showMarketDialog()
            }.onFailure { throwable ->
                Timber.e(throwable)
                onRateError.invoke()

                showErrorUnableSentMessage()
                hideKeyboard()
                showMarketDialog()
            }
        }
    }

    /**
     * Очистка ошибки при вводе символов.
     */
    private fun clearError() {
        textInputLayout.isErrorEnabled = false
    }

    private fun getReviewText(): String = comment.text.toString()

    private fun showToastyMessage(string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorUnableSentMessage() {
        showToastyMessage(getString(R.string.cant_send_message))
    }

    private fun hideKeyboard() {
        try {
            val im = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(comment.windowToken, 0)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun sendToMarket() {
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
        dismiss()
    }

    private fun proceedFirstAnalytic(actionType: AmplitudePropertyRatingActionType) {
        val where = amplitudeWhere ?: return

        viewModel.rateUsAnalytic(
            RateUsAnalyticsRating.First(
                rating = rating,
                amplitudeWhere = where,
                actionType = actionType
            )
        )
    }

    private fun proceedSecondAnalytic(actionType: AmplitudePropertyRatingActionType) {
        val where = amplitudeWhere ?: return

        viewModel.rateUsAnalytic(
            RateUsAnalyticsRating.Second(
                rating = rating,
                rawReviewText = getReviewText(),
                amplitudeRatingChange = amplitudeRatingChange,
                amplitudeWhere = where,
                actionType = actionType
            )
        )
    }

    private fun proceedThirdAnalytic(actionType: AmplitudePropertyRatingActionType) {
        val where = amplitudeWhere ?: return

        viewModel.rateUsAnalytic(
            RateUsAnalyticsRating.Third(
                amplitudeWhere = where,
                actionType = actionType
            )
        )
    }

    private fun showMarketDialog() {
        ivRate.gone()
        textInputLayout.gone()
        rateBar.gone()
        tvTitle.text = getString(R.string.rate_at_google)
        tvDescription.text = getString(R.string.rate_at_google_desc)
        rateBtn.text = getText(R.string.rate)
        cancelBtn.setOnClickListener {
            onDismissListener.invoke()
            dismiss()
        }
        rateBtn.setOnClickListener {
            when (ratingPage) {
                RatingPage.THIRD -> {
                    proceedThirdAnalytic(actionType = AmplitudePropertyRatingActionType.SEND)
                }
                else -> Unit
            }
            isRatingCompleted = true
            onDismissListener.invoke()
            dismiss()
            sendToMarket()
        }
    }

    companion object {
        fun show(
            manager: FragmentManager,
            tag: String?,
            where: AmplitudePropertyRatingWhere
        ) {
            if (manager.findFragmentByTag(tag) != null) return
            PopUpRateUsDialogFragment().apply {
                setAmplitudePropertyWhere(where)
                show(manager = manager, tag = tag)
            }
        }
    }
}
