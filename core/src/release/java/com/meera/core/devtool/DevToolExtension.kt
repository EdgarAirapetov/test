package com.meera.core.devtool

import android.app.Application
import androidx.annotation.StyleRes
import okhttp3.OkHttpClient

/**
 * Все функции ниже при debug сборке имеют тело
 */
fun OkHttpClient.Builder.applyDevToolInterceptor() {
    // do nothing for release
}

fun Application.initDevTools(
    uid: Long,
    serverName: String,
    @StyleRes styleRes: Int,
    envChanged: (result: String) -> Unit,
    localeChanged: (result: String) -> Unit,
    stickerSuggestionsEnabledToggled: (enabled: Boolean) -> Unit,
    detailedReactionsForPostEnabledToggled: (enabled: Boolean) -> Unit,
    detailedReactionsForCommentEnabledToggled: (enabled: Boolean) -> Unit,
    avatarCarouselEnabledToggled: (enabled: Boolean) -> Unit,
    leakCanaryChanged: (enabled: Boolean) -> Unit,
    isLeakCanaryEnabled: Boolean,
    mapEventsToggleChanged: (enabled: Boolean?) -> Unit,
    chatSearchToggleChanged: (enabled: Boolean?) -> Unit,
    videoEditorResizeChanged: (enabled: Boolean) -> Unit,
    roadMaxVideoDurationEnabled: (enabled: Boolean) -> Unit,
    isChuckerEnabled: (enabled: Boolean) -> Unit,
    isTimeOfDayReactionsEnabled: (enabled: Boolean) -> Unit,
    momentsToggled: (enabled: Boolean) -> Unit,
    editPostFeatureToggleEnabled: (enabled: Boolean) -> Unit,
    onClickRedesignSection: (enabled: Boolean) -> Unit,
    hiddenAgeAndSexEnabledToggled: (enabled: Boolean) -> Unit,
    momentViewsFeatureToggleEnabled: (enabled: Boolean) -> Unit,
    mapFriendsToggleChanged: (enabled: Boolean) -> Unit,
) {
    // do nothing for release
}

const val IS_APP_REDESIGNED: Boolean = false
fun checkAppRedesigned(
    isRedesigned: () -> Unit = {},
    isNotRedesigned: () -> Unit = {}
) {
    if (IS_APP_REDESIGNED) {
        isRedesigned.invoke()
    } else {
        isNotRedesigned.invoke()
    }
}
