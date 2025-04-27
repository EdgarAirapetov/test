package com.numplates.nomera3.modules.gift_coffee.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.gift_coffee.domain.mapper.GiftPlaceEntityResponseMapper
import com.numplates.nomera3.modules.gift_coffee.domain.usecase.GetCoffeeAddressParams
import com.numplates.nomera3.modules.gift_coffee.domain.usecase.GetCoffeeAddressUseCase
import com.numplates.nomera3.modules.gift_coffee.ui.entity.GiftPlaceEntity
import com.numplates.nomera3.modules.gift_coffee.ui.viewevent.GiftListPlacesViewEvent
import com.meera.core.extensions.empty
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class GiftListPlacesViewModel : BaseViewModel() {

    @Inject
    lateinit var getCoffeeAddressUseCase: GetCoffeeAddressUseCase

    val mapper = GiftPlaceEntityResponseMapper()

    val liveCoffeePlaces = MutableLiveData<List<GiftPlaceEntity?>?>()
    val liveViewEvents = MutableLiveData<GiftListPlacesViewEvent>()

    var isLoading = false
    var isLastPage = false

    init {
        App.component.inject(this)
    }

    fun getCoffeeAddress(query: String = String.empty(),
                         limit: Int = DEFAULT_GIFT_LIST_PLACES_PAGE_SIZE,
                         offset: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            isLastPage = false
            isLoading = true

            val params = GetCoffeeAddressParams(query, limit, offset)
            getCoffeeAddressUseCase.execute(params,
                    { response ->
                        val places = mapper.mapToGiftPlaceEntity(response) ?: emptyList()
                        if(places.isEmpty()){
                            isLastPage = true
                        }
                        isLoading = false
                        liveCoffeePlaces.postValue(places)
                    },
                    { exception ->
                        Timber.e(exception)
                        liveViewEvents.postValue(GiftListPlacesViewEvent
                                .OnErrorGetCoffeeAddress(R.string.gifts_get_places_error))
                    })
        }
    }

    companion object {
        const val DEFAULT_GIFT_LIST_PLACES_PAGE_SIZE = 20
    }
}