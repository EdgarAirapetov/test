package com.numplates.nomera3.presentation.viewmodel.viewevents

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FriendsHostViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudePeople: AmplitudePeopleAnalytics,
    private val getUserProfileUseCase: GetProfileUseCase,
) : BaseViewModel() {

    val friendsCountLiveData = MutableLiveData<Int>()

    fun getUserInfo(userId: Long?) {
        viewModelScope.launch {
            runCatching {
                userId?.let {
                    getUserProfileUseCase(userId)
                }
            }.onSuccess {
                friendsCountLiveData.value = if (isMe(userId = userId)) {
                    it?.friendsCount ?: 0
                } else {
                    it?.mutualUsers?.moreCount?.plus(it?.mutualUsers?.userIds?.size ?: 0) ?: 0
                }

            }.onFailure {
                Timber.e(it)
            }

        }
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun isMe(userId: Long?) = appSettings.readUID() == userId

    fun isNeedToShowTooltip(): Boolean {
        return appSettings.isCreateFriendsReferralToolTipWasShownTimes < TooltipDuration.DEFAULT_TIMES && appSettings.isShownTooltipSession(
            AppSettings.KEY_IS_CREATE_FRIENDS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES
        )
    }

    fun toolTipShowed() {
        val shownTimes = appSettings.isCreateFriendsReferralToolTipWasShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isCreateFriendsReferralToolTipWasShownTimes = shownTimes + 1
        appSettings.markTooltipAsShownSession(AppSettings.KEY_IS_CREATE_FRIENDS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES)
    }

    fun logPeopleSelected() {
        amplitudePeople.setPeopleSelected(
            where = AmplitudePeopleWhereProperty.ICON_FRIEND, which = AmplitudePeopleWhich.PEOPLE
        )
    }
}
