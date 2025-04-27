package com.numplates.nomera3.modules.peoples.ui.entity

import android.view.View
import androidx.annotation.StringRes
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters

sealed class PeopleUiEffect {
    object OpenSearch : PeopleUiEffect()
    object OpenReferralScreen : PeopleUiEffect()
    object ShowShakeDialog : PeopleUiEffect()
    data class ShowContactsHasBeenSyncDialogUiEffect(val syncCount:Int) : PeopleUiEffect()
    object ShowSyncContactsDialogUiEffect : PeopleUiEffect()
    object RequestReadContactsPermissionUiEffect : PeopleUiEffect()
    object ShowSyncDialogPermissionDenied : PeopleUiEffect()
    data class ShowErrorToast(@StringRes val message: Int) : PeopleUiEffect()
    data class ShowSuccessToast(@StringRes val message: Int) : PeopleUiEffect()
    data class OpenUserProfile(
        val userId: Long,
        val postId: Long? = null,
        val where: AmplitudePropertyWhere) : PeopleUiEffect()
    data class OpenMomentsProfile(
        val userId: Long,
        val postId: Long? = null,
        val view: View?,
        val hasNewMoments: Boolean?
    ) : PeopleUiEffect()
    data class ShowOnboardingEffect(val isShowOnboardingFirstTime: Boolean) : PeopleUiEffect()
    data class ScrollToPositionEffect(val position: Int) : PeopleUiEffect()
    data class ClearRelatedUserPageUiEffect(val position: Int) : PeopleUiEffect()
    data class AddUserFromSearch(val user: UserSearchResultUiEntity) : PeopleUiEffect()
    data class ShowClearRecentSnackBar(val delaySec: Int) : PeopleUiEffect()
    data class ScrollToRecommendedUser(val userId: Long, val position: Int) : PeopleUiEffect()
    data class ApplyNumberSearchParams(val numberSearchParams: NumberSearchParameters) : PeopleUiEffect()
}
