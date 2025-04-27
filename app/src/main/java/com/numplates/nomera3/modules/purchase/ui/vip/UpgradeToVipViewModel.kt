package com.numplates.nomera3.modules.purchase.ui.vip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseOptions
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseStatus
import com.numplates.nomera3.modules.purchase.domain.usecase.GetSubscriptionUseCase
import com.numplates.nomera3.modules.purchase.domain.usecase.PurchaseProductUseCase
import com.numplates.nomera3.modules.purchase.ui.mapper.PurchaseUiModelStateMapper
import com.numplates.nomera3.modules.purchase.ui.model.SimplePurchaseUiModel
import com.numplates.nomera3.modules.purchase.ui.model.UpgradeStatusUIState
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.tracker.ScreenNamesEnum
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.presentation.viewmodel.viewevents.UpgradeToVipEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UpgradeToVipViewModel @Inject constructor(
    private val purchaseUiModelStateMapper: PurchaseUiModelStateMapper,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val getSubscriptionUseCase: GetSubscriptionUseCase,
    private val logger: UpgradeToVipLogger,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getUserProfileUseCase: GetProfileUseCase,
    private val fbAnalytic: FireBaseAnalytics
) : ViewModel() {

    private var accountType: Int? = null
    private var userID = -1L

    private val _liveProducts = MutableLiveData<List<SimplePurchaseUiModel>>()
    val liveProducts: LiveData<List<SimplePurchaseUiModel>> = _liveProducts

    private val _liveUserProfile = MutableLiveData<UpgradeStatusUIState>()
    val liveUserProfile: LiveData<UpgradeStatusUIState> = _liveUserProfile.distinctUntilChanged()

    private val _liveEvent = MutableSharedFlow<UpgradeToVipEvent>()
    val liveEvent: SharedFlow<UpgradeToVipEvent> = _liveEvent.asSharedFlow()

    private val _purchaseEvent = MutableSharedFlow<BillingFlowParams>()
    val purchaseEvent: SharedFlow<BillingFlowParams> = _purchaseEvent.asSharedFlow()

    fun init(userID: Long) {
        this.userID = userID
        pushGetProfile()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun logScreenForFragment(screenName: ScreenNamesEnum) = fbAnalytic.logScreenForFragment(screenName.value)

    fun querySubscriptionSkuList(isPremiumSubscription: Boolean) {
        viewModelScope.launch {
            runCatching {
                val result = getSubscriptionUseCase.invoke(isPremiumSubscription)
                val models = purchaseUiModelStateMapper.mapSimplePurchasesToSimplePurchaseUIModel(result)
                _liveProducts.postValue(models)
            }.onFailure { error ->
                Timber.e(error)
                _liveEvent.emit(UpgradeToVipEvent.ErrorMarketEvent())
            }
        }
    }

    fun buyPrivilegeStatus(productId: String, selectedColor: Int? = null) {
        val purchaseOptions = PurchaseOptions(
            productId = productId,
            userId = userID,
            comment = null,
            accountColor = selectedColor,
            showSender = null,
        )
        purchaseProductUseCase.invoke(purchaseOptions)
            .onEach { status ->
                when (status) {
                    is PurchaseStatus.Purchased -> {
                        Timber.i("Purchase was successful. Purchase: ${status.purchases}.")
                        logger.logVipBuying(
                            accountColor = selectedColor,
                            productId = productId,
                            accountType = accountType,
                        )
                        _liveEvent.emit(UpgradeToVipEvent.SuccessPurchaseEvent)
                    }
                    is PurchaseStatus.ClientPrepared -> {
                        _purchaseEvent.emit(status.params)
                    }
                }
            }
            .catch { error ->
                Timber.e("Purchase was not successful. Error: $error;")
                _liveEvent.emit(UpgradeToVipEvent.ErrorEvent)
            }
            .launchIn(viewModelScope)
    }

    private fun pushGetProfile() {
        viewModelScope.launch {
            runCatching {
                getUserProfileUseCase(userID).apply {
                    this@UpgradeToVipViewModel.accountType = accountType
                }
            }.onSuccess { profile ->
                _liveUserProfile.postValue(purchaseUiModelStateMapper.mapUserProfileToUpgradeStatusUIState(profile))
            }.onFailure { error ->
                Timber.e(error)
                _liveEvent.emit(UpgradeToVipEvent.ErrorEvent)
            }
        }
    }
}
