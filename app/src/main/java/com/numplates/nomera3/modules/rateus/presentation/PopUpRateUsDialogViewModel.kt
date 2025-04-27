package com.numplates.nomera3.modules.rateus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.rateus.domain.RateUsAnalyticUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsSaveLastShowUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsWriteIsRatedUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class PopUpRateUsDialogViewModel @Inject constructor(
    private val rateUsUseCase: RateUsUseCase,
    private val rateUsAnalyticUseCase: RateUsAnalyticUseCase,
    private val rateUsWriteIsRatedUseCase: RateUsWriteIsRatedUseCase,
    private val rateUsSaveLastShowUseCase: RateUsSaveLastShowUseCase
) : ViewModel() {

    fun rateUs(rating: Int, comment: String): Job {
        return viewModelScope.launch {
            rateUsUseCase.invoke(rating = rating, comment = comment)
        }
    }

    fun rateUsAnalytic(rateUsAnalyticsRating: RateUsAnalyticsRating) {
        viewModelScope.launch {
            rateUsAnalyticUseCase.invoke(rateUsAnalyticsRating)
        }
    }

    fun saveLastShow() {
        rateUsSaveLastShowUseCase.invoke()
    }

    fun writeIsRated() {
        rateUsWriteIsRatedUseCase.invoke(isRated = true)
    }

    fun writeIsNotRated() {
        rateUsWriteIsRatedUseCase.invoke(isRated = false)
    }
}
