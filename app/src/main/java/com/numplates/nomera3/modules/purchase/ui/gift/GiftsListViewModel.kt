package com.numplates.nomera3.modules.purchase.ui.gift

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.purchase.domain.usecase.GetGiftsWithDataUseCase
import com.numplates.nomera3.modules.purchase.ui.mapper.GiftUiModelMapper
import com.numplates.nomera3.modules.purchase.ui.model.GiftCategoryUiModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.GiftListEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class GiftsListViewModel @Inject constructor(
    private val giftsWithData: GetGiftsWithDataUseCase,
    private val giftUiModelMapper: GiftUiModelMapper,
    private val getUserUidUseCase: GetUserUidUseCase
) : ViewModel() {

    private val _liveEvents = MutableSharedFlow<GiftListEvents>()
    val liveEvents = _liveEvents.asSharedFlow()

    private val _liveGiftList = MutableLiveData<List<GiftCategoryUiModel>>()
    val liveGiftList: LiveData<List<GiftCategoryUiModel>> = _liveGiftList.distinctUntilChanged()

    fun init() {
        loadGiftList()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun loadGiftList() {
        viewModelScope.launch {
            runCatching {
                val result = giftsWithData.invoke()
                val items = giftUiModelMapper.convertGiftCategory(result)
                _liveGiftList.postValue(items)
            }.onFailure { error ->
                Timber.e(error)
                _liveEvents.emit(GiftListEvents.OnMarketError)
            }
        }
    }
}
