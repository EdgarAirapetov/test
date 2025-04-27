package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.tryCatch
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.db.models.userprofile.GiftEntity
import com.numplates.nomera3.App
import com.numplates.nomera3.data.newmessenger.response.GiftResponse
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.domain.interactornew.DeleteGiftUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.util.UserGiftsMapper
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.rateus.domain.IsNeedToGetRateUseCase
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserGiftEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserGiftsViewModel : BaseViewModel() {

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var deleteGiftUseCase: DeleteGiftUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var birthdayTextUtil: BirthdayTextUtil

    @Inject
    lateinit var userBirthdayUtils: UserBirthdayUtils

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var isNeedToGetRateUseCase: IsNeedToGetRateUseCase

    @Inject
    lateinit var getOwnLocalProfileUseCase: GetOwnLocalProfileUseCase

    val disposables = CompositeDisposable()

    private val _liveGetGifts = MutableLiveData<List<UserGiftsUiEntity>>()
    val liveGetGifts: LiveData<List<UserGiftsUiEntity>> = _liveGetGifts
    val liveEvents = MutableLiveData<UserGiftEvents>()

    private var userGiftsMapper: UserGiftsMapper? = null
    private val currentGiftsList = mutableListOf<UserGiftsUiEntity>()

    private var isLastPage = false
    private var isLoading = false
    private var userId: Long? = null
    var dateOfBirth: Long? = null

    init {
        App.component.inject(this)
        userGiftsMapper = UserGiftsMapper(birthdayTextUtil, userBirthdayUtils)
    }

    fun getHolidayVisits(): Int? {
        return appSettings.holidayCalendarDaysCount?.toIntOrNull()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun isWorthToShowDialog() = isNeedToGetRateUseCase.invoke()

    fun getUserGifts(userId: Long?, limit: Int = 200, offset: Int = 0) {
        isLoading = true
        userId?.let { id ->
            val payload = hashMapOf(
                    "id" to id,
                    "limit" to limit,
                    "offset" to offset
            )
            disposables.add(
                    webSocketMainChannel.pushGetUserGifts(payload)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                isLoading = false

                                val listGifts = gson.fromJson<ResponseWrapperWebSock<GiftResponse>>(response.payload)
                                        ?.response
                                        ?.gifts
                                        ?.filter { it.addedAt != null }
                                        ?.sortedByDescending { it.addedAt }
                                if (listGifts?.isEmpty() == true)
                                    isLastPage = true
                                listGifts?.let { gifts ->
                                    handleGiftsResult(
                                        giftsList = gifts,
                                        offset = offset
                                    )
                                }
                            }, { error ->
                                Timber.e("ERROR: Get gifts: $error")
                                isLoading = false
                                liveEvents.value = UserGiftEvents.ErrorRequestEvent
                            })
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun onLast() = isLastPage

    fun isLoading() = isLoading

    fun loadMore(itemCount: Int) {
        userId?.let {
            getUserGifts(it, offset = itemCount)
        }
    }

    fun setUserId(userId: Long?) {
        this.userId = userId
    }

    fun refreshGifts() {
        isLastPage = false
        liveEvents.value = UserGiftEvents.UserClearAdapterEvent
        getUserGifts(userId)
    }

    fun getGiftsFromPush(userId: Long?) {
        Timber.d("getGiftsFromPush called")
        userId?.let { id ->
            val isChannelJoined = webSocketMainChannel.isChannelJoined()
            val isSockedConnected = webSocketMainChannel.isConnected()

            if (isChannelJoined && isSockedConnected)
                getUserGifts(id)
            else {
                val d = webSocketMainChannel.publishSocketConnection
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter { it && webSocketMainChannel.isConnected() }
                        .take(1)
                        .subscribe({ isConnected ->
                            if (isConnected) {
                                getUserGifts(id)
                            }
                        }, {
                            Timber.e(it)
                        })
                disposables.add(d)
            }
        }
    }

    fun deleteGift(position: Int,
                   gift: GiftEntity?,
                   shouldRefreshItem: Boolean) {
        gift?.let {
            viewModelScope.launch(Dispatchers.IO) {
                tryCatch({
                    val response = deleteGiftUseCase.deleteGift(gift.giftId)
                    if (response.data != null) {
                        currentGiftsList.removeAt(position)
                        _liveGetGifts.postValue(currentGiftsList)
                        liveEvents.postValue(UserGiftEvents.SuccessDeleteGift(position))
                    } else {
                        liveEvents.postValue(UserGiftEvents.FailDeleteGift(position, shouldRefreshItem))
                    }
                }, {
                    liveEvents.postValue(UserGiftEvents.FailDeleteGift(position, shouldRefreshItem))
                })
            }
        }
    }

    fun updateCoffeeSelected(entity: GiftEntity) {
        val giftUiEntity = userGiftsMapper?.mapUserGiftToUiEntity(
            giftEntity = entity,
            viewType = 1
        )
        giftUiEntity?.let { uiEntity ->
            val index = currentGiftsList.indexOfFirst {
                it.giftEntity.giftId == giftUiEntity.giftEntity.giftId
            }
            if (index == -1) return

            currentGiftsList.removeAt(index)
            currentGiftsList.add(index, giftUiEntity)
            _liveGetGifts.value = currentGiftsList
        }
    }

    fun requestOwnProfileDao() {
        viewModelScope.launch {
            runCatching {
                getOwnLocalProfileUseCase.invoke()
            }.onSuccess {
                liveEvents.postValue(UserGiftEvents.OwnUserProfileEvent(it?.name))
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun handleGiftsResult(giftsList: List<GiftEntity>, offset: Int) {
        val giftsUiList = userGiftsMapper?.mapUserGiftsToUiEntity(
            userGiftsList = giftsList,
            viewType = 1,
            dateOfBirth = dateOfBirth ?: 0
        )
        giftsUiList?.let { data ->
            if (offset == 0) {
                this.currentGiftsList.clear()
            }
            currentGiftsList.addAll(data)
            _liveGetGifts.postValue(currentGiftsList)
        }
    }
}
