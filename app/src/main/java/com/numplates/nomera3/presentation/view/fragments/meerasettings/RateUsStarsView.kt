package com.numplates.nomera3.presentation.view.fragments.meerasettings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraRateUsStarsBinding

enum class RateUsStarsButtons constructor(val rate: Int){
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5)
}

class RateUsStarsView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): ConstraintLayout(context, attrs, defStyle) {

    var rating: Int = 0
        private set(value) {
            field = value
        }

    private var rateUsStarsListener: ((RateUsStarsButtons) -> Unit)? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_rate_us_stars, this)
        .let(MeeraRateUsStarsBinding::bind)

    init {
        with(binding){
            rateUsStar1.setOnClickListener { handleFirstBtn() }
            rateUsStar2.setOnClickListener { handleSecondBtn() }
            rateUsStar3.setOnClickListener { handleThirdBtn() }
            rateUsStar4.setOnClickListener { handleFourthBtn() }
            rateUsStar5.setOnClickListener { handleFifthBtn() }
        }
    }

    fun setStarsListener(listener: (RateUsStarsButtons)->Unit){
        rateUsStarsListener = listener
    }

   fun skipStars() {
       with(binding) {
           rating = 0
           rateUsStar5.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
           rateUsStar4.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
           rateUsStar3.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
           rateUsStar2.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
           rateUsStar1.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
       }
   }

    private fun handleFifthBtn() {
        println(RateUsStarsButtons.FIVE.rate)
        with(binding){
            rateUsStar1.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar2.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar3.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar4.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar5.setBackgroundResource(R.drawable.meera_rate_us_star_green)
        }
        rating = RateUsStarsButtons.FIVE.rate
        rateUsStarsListener?.invoke(RateUsStarsButtons.FIVE)
    }

    private fun handleFourthBtn() {
        with(binding){
            rateUsStar1.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar2.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar3.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar4.setBackgroundResource(R.drawable.meera_rate_us_star_green)

            rateUsStar5.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
        }
        rating = RateUsStarsButtons.FOUR.rate
        rateUsStarsListener?.invoke(RateUsStarsButtons.FOUR)
    }

    private fun handleThirdBtn() {
        with(binding){
            rateUsStar1.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar2.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar3.setBackgroundResource(R.drawable.meera_rate_us_star_green)

            rateUsStar5.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
            rateUsStar4.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
        }
        rating = RateUsStarsButtons.THREE.rate
        rateUsStarsListener?.invoke(RateUsStarsButtons.THREE)
    }

    private fun handleSecondBtn() {
        with(binding){
            rateUsStar1.setBackgroundResource(R.drawable.meera_rate_us_star_green)
            rateUsStar2.setBackgroundResource(R.drawable.meera_rate_us_star_green)

            rateUsStar5.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
            rateUsStar4.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
            rateUsStar3.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
        }
        rating = RateUsStarsButtons.TWO.rate
        rateUsStarsListener?.invoke(RateUsStarsButtons.TWO)
    }

    private fun handleFirstBtn() {
        with(binding){
            rateUsStar1.setBackgroundResource(R.drawable.meera_rate_us_star_green)

            rateUsStar5.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
            rateUsStar4.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
            rateUsStar3.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
            rateUsStar2.setBackgroundResource(R.drawable.meera_rate_us_star_gray)
        }
        rating = RateUsStarsButtons.ONE.rate
        rateUsStarsListener?.invoke(RateUsStarsButtons.ONE)
    }

}
