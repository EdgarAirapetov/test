package com.numplates.nomera3.modules.gift_coffee.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.gift_coffee.domain.usecase.ToMarkGiftAsViewedUseCase
import com.numplates.nomera3.modules.gift_coffee.domain.usecase.ToMarkGiftViewedParams
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.exception.Failure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CoffeeLikePromoCodeViewModel : BaseViewModel() {

    @Inject
    lateinit var toMarkGiftAsViewedUseCase: ToMarkGiftAsViewedUseCase

    val event = MutableLiveData<String>()

    init {
        App.component.inject(this)
    }

    fun markGiftAsViewed(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            toMarkGiftAsViewedUseCase.execute(ToMarkGiftViewedParams(id),
                    success = {},
                    fail = { failure.postValue(Failure.ServerError("")) }
            )
        }
    }
}
