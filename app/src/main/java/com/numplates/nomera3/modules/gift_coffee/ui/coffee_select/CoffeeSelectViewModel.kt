package com.numplates.nomera3.modules.gift_coffee.ui.coffee_select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.meera.db.models.userprofile.GiftEntity
import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.domain.usecase.ToGetCoffeeCodeParams
import com.numplates.nomera3.modules.gift_coffee.domain.usecase.ToGetCoffeeCodeUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ILLEGAL_STATE_COFFEE_TYPE_UNDEFINED = "Coffee type should be set"

class CoffeeSelectViewModel : ViewModel() {

    @Inject
    lateinit var getCoffeeCodeUseCase: ToGetCoffeeCodeUseCase

    val state: LiveData<CoffeeSelectState> = MutableLiveData()

    private var type: CoffeeType? = null

    init {
        App.component.inject(this)

        getStateLiveData().value = CoffeeSelectState.Init
    }

    private fun getStateLiveData(): MutableLiveData<CoffeeSelectState> =
            state as MutableLiveData<CoffeeSelectState>

    fun applySelect(gift: GiftEntity) {
        viewModelScope.launch {
            if (type == null) {
                error(ILLEGAL_STATE_COFFEE_TYPE_UNDEFINED)
            }

            getStateLiveData().value = CoffeeSelectState.Loading

            getCoffeeCodeUseCase.execute(
                    params = ToGetCoffeeCodeParams(gift.giftId, type!!),
                    success = { getStateLiveData().value = CoffeeSelectState.Success(type!!, it) },
                    fail = { getStateLiveData().value = CoffeeSelectState.Error(it) }
            )
        }
    }

    fun selectCoffee(newType: CoffeeType) {
        type = newType
        getStateLiveData().value = CoffeeSelectState.Selected(newType)
    }
}
