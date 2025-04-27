package com.numplates.nomera3.modules.purchase.ui.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyGiftSendBack
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.purchase.domain.error.PurchaseException
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseOptions
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseStatus
import com.numplates.nomera3.modules.purchase.domain.usecase.PurchaseProductUseCase
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class SendGiftViewModel @Inject constructor(
    val appSettings: AppSettings,
    private val tracker: AnalyticsInteractor,
    private val birthdayUtils: UserBirthdayUtils,
    private val birthdayTextUtil: BirthdayTextUtil,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val getUserUidUseCase: GetUserUidUseCase
) : BaseViewModel() {

    var isFromSendBackClick = false
    var where = AmplitudePropertyWhere.OTHER

    val events: SingleLiveEvent<SendGiftEvent> = SingleLiveEvent()
    val giftViewEvent = MutableLiveData<GiftViewEvent>()
    var giftComment: String? = null
    var userId: Long? = null

    var inputGiftText = ""
    var dateOfBirth: Long? = null
    var cachedProducts: List<ProductDetails>? = null

    private val _birthdayRangesLiveData = MutableLiveData<List<IntRange>>()
    val birthdayRangesLiveData: LiveData<List<IntRange>> = _birthdayRangesLiveData

    private val _purchaseEvent = MutableSharedFlow<BillingFlowParams>()
    val purchaseEvent: SharedFlow<BillingFlowParams> = _purchaseEvent.asSharedFlow()

    var isSenderVisible: Boolean
        get() = _showSender
        set(value) {
            _showSender = value
        }

    private var _showSender = true

    fun getUserUid() = getUserUidUseCase.invoke()

    fun reportLog(gift: PurchaseOptions) {
        tracker.logSendGift(
            productId = gift.productId,
            fromId = appSettings.readUID().toString(),
            toId = userId.toString(),
            sendBack =
            if (isFromSendBackClick) AmplitudePropertyGiftSendBack.TRUE
            else AmplitudePropertyGiftSendBack.FALSE,
            where = where
        )
    }

    fun setGiftTextChanged(text: String) {
        val hasBirthday = birthdayUtils.isBirthdayToday(dateOfBirth)
        if (text == inputGiftText || !hasBirthday) return
        this.inputGiftText = text
        val birthdayRanges = birthdayTextUtil.getBirthdayTextListRanges(
            birthdayText = text
        )
        _birthdayRangesLiveData.postValue(birthdayRanges)
    }

    fun purchaseGiftForAnotherUser(giftItem: GiftItemUiModel) {
        val purchaseOptions = PurchaseOptions(
            productId = giftItem.marketProductId,
            userId = userId,
            comment = giftComment,
            accountColor = null,
            showSender = _showSender,
        )
        purchaseProductUseCase.invoke(purchaseOptions)
            .onEach { status ->
                when (status) {
                    is PurchaseStatus.Purchased -> {
                        Timber.i("Purchase was successful. Purchase: ${status.purchases}.")
                        reportLog(purchaseOptions)
                        events.postValue(SendGiftEvent.GiftSuccess)
                    }
                    is PurchaseStatus.ClientPrepared -> {
                        _purchaseEvent.emit(status.params)
                    }
                }
            }
            .catch { error ->
                Timber.e("Purchase was not successful. Error: $error;")
                when (error.cause) {
                    PurchaseException.CancelledByUser -> events.postValue(SendGiftEvent.CancelledByUser)
                    else -> events.postValue(SendGiftEvent.GiftError)
                }
            }
            .launchIn(viewModelScope)
    }
}

sealed class GiftViewEvent {
    data class OnSkuDetailsLoaded(val giftItem: GiftItemUiModel) : GiftViewEvent()
}
